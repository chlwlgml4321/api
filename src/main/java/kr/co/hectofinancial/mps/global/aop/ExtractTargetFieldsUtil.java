package kr.co.hectofinancial.mps.global.aop;

import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.Field;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.regex.Pattern;

@Slf4j
public class ExtractTargetFieldsUtil {
    // 파라미터에서 요청 목록(target fields)만 추출
    private static final List<String> TARGET_FIELDS_ORDER = Arrays.asList(
            "custNo", "trdAmt", "mnyAmt", "pntAmt", "custBdnFeeAmt",
            "waitMnyAmt", "cnclTrdAmt", "cnclMnyAmt", "cnclPntAmt"
    );
    private static final Set<String> TARGET_FIELDS = new LinkedHashSet<>(TARGET_FIELDS_ORDER);

    // ~RequestDto 명칭의 클래스에서만 target field 추출
    private static final Pattern REQUEST_DTO_NAME = Pattern.compile("(?i).*RequestDto$");

    // 클래스별 타깃 필드 캐시 (필드명이 TARGET_FIELDS에 포함된 것만)
    private static final ConcurrentMap<Class<?>, Map<String, Field>> FIELD_CACHE = new ConcurrentHashMap<>();

    /**
     * JoinPoint.getArgs()에서
     * - Map: key ∈ TARGET_FIELDS 만 추출
     * - Bean/DTO: 클래스명이 *RequestDto 일 때만, TARGET_FIELDS 에 해당하는 필드만 추출 (custNo 외에 ~Amt 필드가 하나이상 있어야함)
     * 한 줄 KV("key=val|...")로 반환. 값은 개행/파이프 제거.
     */
    public static String extractTargetKVFromArgs(Object[] args) {
        if (args == null || args.length == 0) return "";

        // key → value (첫 값 우선)
        Map<String, String> found = new LinkedHashMap<>();

        for (Object arg : args) {
            if (arg == null) continue;

            // Map 계열 처리 (키가 정확히 TARGET_FIELDS 에 속하는 것만)
            if (arg instanceof Map) {
                Map<?, ?> map = (Map<?, ?>) arg;
                for (String k : TARGET_FIELDS_ORDER) {
                    Object v = map.get(k);
                    if (v != null && !found.containsKey(k)) {
                        found.put(k, sanitize(String.valueOf(v)));
                    }
                }
                continue;
            }

            // 클래스명이 *RequestDto 인 경우만
            String simple = arg.getClass().getSimpleName();
            if (!REQUEST_DTO_NAME.matcher(simple).matches()) continue;

            Map<String, Field> fieldMap = FIELD_CACHE.computeIfAbsent(arg.getClass(), ExtractTargetFieldsUtil::indexTargetFields);
            for (String name : TARGET_FIELDS_ORDER) {
                if (found.containsKey(name)) continue;
                Field f = fieldMap.get(name);
                if (f == null) continue;

                try {
                    f.setAccessible(true);
                    Object v = f.get(arg);
                    if (v != null) {
                        found.put(name, sanitize(String.valueOf(v)));
                    }
                } catch (IllegalAccessException e) {
                    log.warn("Unable to access field '{}' on class '{}': {}", f.getName(), arg.getClass().getName(), e.getMessage());
                }
            }
        }

        // 조건: custNo 외 ~amt 필드가 최소 1개 이상 있어야 의미 있음
        boolean hasAmtField = found.keySet().stream()
                .anyMatch(k -> !k.equals("custNo") && k.toLowerCase().endsWith("amt"));

        if (!hasAmtField) return ""; // custNo만 있거나 아무것도 없으면 "" 반환

        // 고정 순서로 출력
        StringBuilder sb = new StringBuilder();
        for (String k : TARGET_FIELDS_ORDER) {
            String v = found.get(k);
            if (v != null) sb.append(k).append('=').append(v).append('|');
        }
        return sb.toString();
    }

    // 클래스의 모든 상위 클래스까지 탐색하여 TARGET_FIELDS 에 해당하는 필드만 인덱싱
    private static Map<String, Field> indexTargetFields(Class<?> clazz) {
        Map<String, Field> map = new HashMap<>();
        Class<?> c = clazz;
        while (c != null && c != Object.class) {
            for (Field f : c.getDeclaredFields()) {
                String name = f.getName();
                if (TARGET_FIELDS.contains(name) && !map.containsKey(name)) {
                    map.put(name, f);
                }
            }
            c = c.getSuperclass();
        }
        return map;
    }

    private static String sanitize(String s) {
        return s.replace('\n', ' ').replace('\r', ' ').replace('|', ' ');
    }

}
