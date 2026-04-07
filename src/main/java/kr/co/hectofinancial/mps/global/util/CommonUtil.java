package kr.co.hectofinancial.mps.global.util;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.conn.ssl.TrustSelfSignedStrategy;
import org.apache.http.conn.ssl.TrustStrategy;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import javax.net.ssl.SSLContext;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.ByteBuffer;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.text.SimpleDateFormat;
import java.text.StringCharacterIterator;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Base64;
import java.util.Date;
import java.util.UUID;


/**
 * 웹관련된 유틸 클래스
 *
 */

@Component
@Slf4j
public class CommonUtil {

    /**
     * 고객명 마스킹 처리
     * 한글 3글자 이하인 경우 두번째 자리 마스킹
     * 3글자 초과인 경우 첫번째, 마지막 자리 제외한 가운데 글자 자리수만큼 마스킹
     *
     * @param userNm : 홍
     *               길동
     *               홍길동
     *               홍길동님
     * @return 홍
     * 길*
     * 홍*동
     * 홍**님
     */
    public static String maskingUserNm(String userNm) {
        String maskingNm = "";
        if (StringUtils.isEmpty(userNm)) {
            return maskingNm;
        }

        if (StringUtils.equals(userNm, "_EMPTY_")) {
            return maskingNm;
        }

        if (userNm.length() == 1) {
            maskingNm = userNm;
        } else if (userNm.length() == 2) {
            maskingNm = maskingString(userNm, 1, 0);
        } else {
            maskingNm = maskingString(userNm, 1, 1);
        }
        return maskingNm;
    }

    /**
     * 마스킹 처리
     * @param str   : 123456789
     * @param start : 3
     * @param end   : 2
     * @return 123****89
     */
    public static String maskingString(Object... args) {
        String str = (String) args[0];
        int start = (1 < args.length ? (int) args[1] : 3);
        int end = (2 < args.length ? (int) args[2] : 2);

        StringBuilder sb = new StringBuilder();

        for (int i = 0; i < str.length(); i++) {
            char temp = str.charAt(i);
            if (i < start) {
                sb.append(temp);
            } else if (i >= str.length() - end) {
                sb.append(temp);
            } else {
                sb.append("*");
            }
        }
        return sb.toString();
    }

    /**
     * 파라미터로 넘겨주는 String 이 한글인지 true, false 로 return
     * @param text
     * @return
     */
    public static boolean isKorean(String text) {
        for (char c : text.toCharArray()) {
            Character.UnicodeBlock block = Character.UnicodeBlock.of(c);
            if (block != Character.UnicodeBlock.HANGUL_SYLLABLES &&
                    block != Character.UnicodeBlock.HANGUL_JAMO &&
                    block != Character.UnicodeBlock.HANGUL_COMPATIBILITY_JAMO &&
                    block != Character.UnicodeBlock.HANGUL_JAMO_EXTENDED_A &&
                    block != Character.UnicodeBlock.HANGUL_JAMO_EXTENDED_B) {
                return false;
            }
        }
        return true;
    }

    /**
     * 파라미터로 넘겨주는 String 이 영어인지 true, false 로 return
     * @param text
     * @return
     */
    public static boolean isEnglish(String text) {
        for (char c : text.toCharArray()) {
            if ((c >= 'A' && c <= 'Z') || (c >= 'a' && c <= 'z')) {
                return true;
            }
        }
        return false;
    }

    public static String getDateTimeStr() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyMMddHHmmss");
        return sdf.format(new Date());
    }

    public static String getDateStr() {
        return LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
    }

    /**
     * 파라미터의 값이 null일때 초기화, null이 아닌경우 trim
     * @param param 대상 문자열
     * @return String
     */
    public static String nullTrim(String param) {
        if (param == null || "null".equalsIgnoreCase(param)) {
            return "";
        }
        // 전각 공백(\u3000)도 포함해서 앞뒤 공백 제거
        return param.replaceAll("^[\\s\\u3000]+|[\\s\\u3000]+$", "");
    }

    /**
     * 파라미터의 값이 null일때 초기화, null이 아닌경우 trim
     * @param param 대상 문자열
     * @return String
     */
    public static String nullTrim(Long param) {

        String retrunStr = "";
        if (param == null) {
            retrunStr = "";
            return retrunStr;
        } else {
            return Long.toString(param).trim();
        }
    }

    /**
     * 앞뒤 공백 모두 제거
     * @param param 대상 문자열
     * @return String
     */
    public static String removeAllSpaces(String param) {
        return param.replaceAll("\\s+", "");
    }

    /* 거래처리고유값 생성 */
    public static String makeTrPrcsSeq(String mthNm) {
        /* 현재일시 */
        CustomDateTimeUtil customDateTimeUtil = new CustomDateTimeUtil();
        String current = customDateTimeUtil.getDateTime();

        /* uuid */
        UUID uuid = UUID.randomUUID();
        ByteBuffer uuidBytes = ByteBuffer.wrap(new byte[16]);
        uuidBytes.putLong(uuid.getMostSignificantBits());
        uuidBytes.putLong(uuid.getLeastSignificantBits());
        String uuidB64Str = Base64.getEncoder().encodeToString(uuidBytes.array());

        /* TrPrcsSeq 생성 */
        StringBuffer trPrcsSeqBuf = new StringBuffer();
        trPrcsSeqBuf.append(mthNm);
        trPrcsSeqBuf.append(current.substring(2));
        trPrcsSeqBuf.append(uuidB64Str);
        trPrcsSeqBuf.append(current.substring(12, 14));

        return trPrcsSeqBuf.toString().toUpperCase();
    }

    public static RestTemplate getRestTemplateInstance(int connect_timeout, int read_timeout) throws Exception {
        SSLContext context = SSLContext.getInstance("TLSv1.2");
        context.init(null, null, null);

//		CloseableHttpClient httpClient = HttpClientBuilder.create().setSSLContext(context).build();

        TrustStrategy acceptingTrustStrategy = new TrustSelfSignedStrategy();
        SSLContext sslContext = org.apache.http.ssl.SSLContexts.custom().loadTrustMaterial(null, acceptingTrustStrategy).build();
        SSLConnectionSocketFactory scsf = new SSLConnectionSocketFactory(sslContext, NoopHostnameVerifier.INSTANCE);
        CloseableHttpClient httpClient = HttpClients.custom().setSSLSocketFactory(scsf).build();
        HttpComponentsClientHttpRequestFactory factory = new HttpComponentsClientHttpRequestFactory(httpClient);
        factory.setReadTimeout(read_timeout);
        factory.setConnectTimeout(connect_timeout);

        RestTemplate restTemplate = new RestTemplate(factory);

        for (HttpMessageConverter<?> converter : restTemplate.getMessageConverters()) {
            if (converter instanceof StringHttpMessageConverter) {
                ((StringHttpMessageConverter) converter).setWriteAcceptCharset(false);
            }
        }
        return restTemplate;
    }

    /**
     * 스트링 크기 비교
     * param1, param2를 비교하여
     * 1이 큰 경우 0보다 큰 값, 2가 큰경우 0보다 작은 값, 같을 경우 0을 리턴
     * @param s1
     * @param s2
     * @return
     */
    public static int compareStrings(String s1, String s2) {
        int compareResult = s1.compareTo(s2);
        return compareResult;
    }

    /**
     *
     * @param target: 제거할 문자열 ex) "0"
     * @param input:  ex)0001000, 0020000
     * @return ex) 1000, 20000
     */
    public static String tgRemoveStr(String input, String target) {

        int length = input.length();
        int startIndex = -1;

        for (int a = 0; a < length; a++) {
            char c = input.charAt(a);
            if (target.indexOf(c) == -1) {
                startIndex = a;
                break;
            }
        }

        if (startIndex != -1) {
            return input.substring(startIndex);
        } else {
            return "0";
        }
    }

    public static String cutString(String input, int byteStart, int byteEnd, Charset charset) throws CharacterCodingException {
        byte[] bytes = input.getBytes(charset);

        // 범위 초과 방지
        if (byteStart >= bytes.length) return "";
        if (byteEnd > bytes.length) byteEnd = bytes.length;

        byte[] subBytes = new byte[byteEnd - byteStart];
        System.arraycopy(bytes, byteStart, subBytes, 0, subBytes.length);

        // EUC-KR 디코딩 (깨진 문자 방지)
        CharsetDecoder decoder = charset.newDecoder();
        ByteBuffer buffer = ByteBuffer.wrap(subBytes);
        String result = decoder.decode(buffer).toString();

        return result;
    }

    /**
     * char ch 타입이 전각문자이면 return true
     */
    private static boolean isFullWidth(char ch) {
        // 한글 또는 전각 문자 범위 (가~힣 또는 기타 필요 시 확장 가능)
        return (ch >= 0xAC00 && ch <= 0xD7A3) // 한글
                || (ch >= 0xFF01 && ch <= 0xFF60) // 전각 영문/기호
                || (ch >= 0xFFE0 && ch <= 0xFFE6); // 기타 전각 기호
    }

    /**
     * limit 자리수까지만 잘라냄
     */
    public static String limitStringLength(String input, int limit) {
        if (input == null || limit <= 0) return "";

        int maxLength = limit * 2; // 전각 문자는 2자리로 계산
        int currentLength = 0;

        StringBuilder sb = new StringBuilder();

        for (char ch : input.toCharArray()) {
            int charLength = isFullWidth(ch) ? 2 : 1;

            if (currentLength + charLength > maxLength) {
                break;
            }

            sb.append(ch);
            currentLength += charLength;
        }
        return sb.toString();
    }

    public static String escapeJSON(String aText) {
        final StringBuilder result = new StringBuilder();
        StringCharacterIterator iterator = new StringCharacterIterator(aText);
        char character = iterator.current();
        while (character != StringCharacterIterator.DONE) {
            if (character == '\"') {
                result.append("\\\"");
            } else if (character == '\\') {
                result.append("\\\\");
            } else if (character == '/') {
                result.append("\\/");
            } else if (character == '\b') {
                result.append("\\b");
            } else if (character == '\f') {
                result.append("\\f");
            } else if (character == '\n') {
                result.append("\\n");
            } else if (character == '\r') {
                result.append("\\r");
            } else if (character == '\t') {
                result.append("\\t");
            } else {
                //the char is not a special one
                //add it to the result as is
                result.append(character);
            }
            character = iterator.next();
        }
        return result.toString();
    }

    /*
     * 영문(대문자/소문자) + 숫자 랜덤 키 생성
     */
    public static String randomKey(int length) {
        int idx = 0;
        char[] charSet = new char[]{
                '0', '1', '2', '3', '4', '5', '6', '7', '8', '9',
                'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M', 'N', 'O', 'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z',
                'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l', 'm', 'n', 'o', 'p', 'q', 'r', 's', 't', 'u', 'v', 'w', 'x', 'y', 'z'
        };

        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < length; i++) {
            idx = (int) ((Math.random() * 10) * (Math.random() * 10));

            if (idx > charSet.length) idx = idx % charSet.length;

            sb.append(charSet[idx == charSet.length ? idx -= 1 : idx]);
        }
        return sb.toString();
    }

    /**
     * 금액 변환 천단위 콤마 표시
     * @param tgtAmt
     * @return
     */
    public static String formatMoney(long tgtAmt) {
        return String.format("%,d", tgtAmt);
    }

    /**
     * @param reqAmount  충전하려는 금액
     * @param minAmount  최소 충전 가능 금액
     * @param unitAmount 충전 단위 금액
     * @return 실제로 충전 가능한 금액 (단위 올림 적용)
     */
    public static long calculateChargeAmout (long reqAmount, long minAmount, long unitAmount) throws IllegalArgumentException {
        //최소 충전 가능 금액보다 작으면, 최소 충전 가능 금액으로 설정
        long baseAmount = Math.max(reqAmount, minAmount);

        //단위금액으로 올림처리
        long remainder = baseAmount % unitAmount;
        if (remainder == 0) {
            return baseAmount;
        }
        return baseAmount + (unitAmount - remainder);
    }

    /**
     * 금액과 퍼센티지(예:5.5), 반올림여부를 넣으면 계산된 금액 return
     *
     * @param amount
     * @param ratePercent
     * @param rounding    반올림여부 (default false)
     * @return
     */
    public static long calculateFee(long amount, double ratePercent, boolean rounding) {
        BigDecimal amt = BigDecimal.valueOf(amount);
        BigDecimal rate = BigDecimal.valueOf(ratePercent).divide(BigDecimal.valueOf(100));

        BigDecimal fee = amt.multiply(rate);

        if (rounding) {
            return fee.setScale(0, RoundingMode.HALF_UP).longValue();
        }
        return fee.setScale(0, RoundingMode.DOWN).longValue();
    }

    /**
     * 수수료 상한가, 하한가가 존재하여, 계산된 수수료에 적용한뒤 return
     *
     * @param amount
     * @param ratePercent
     * @param rounding
     * @param feeMin
     * @param feeMax
     * @return
     */
    public static long calculateFee(long amount, double ratePercent, boolean rounding, long feeMin, long feeMax) {

        if (amount < 0) {
            throw new IllegalArgumentException("amount must be non-negative");
        }
        if (ratePercent < 0) {
            throw new IllegalArgumentException("ratePercent must be non-negative");
        }
        if (feeMin < 0 || feeMax < 0) {
            throw new IllegalArgumentException("feeMin/feeMax must be non-negative");
        }
        if (feeMin > feeMax) {
            throw new IllegalArgumentException("feeMin must be <= feeMax");
        }

        long fee = calculateFee(amount, ratePercent, rounding);

        // 상한·하한 보정
        if (fee < feeMin) {
            return feeMin;
        }
        if (fee > feeMax) {
            return feeMax;
        }
        return fee;
    }

    /**
     * 은행 점검시간 인지 확인 하는 메서드
     * @return
     */
    public static boolean isBankMaintenanceHour() {
        //은행 점검시간 확인
        String currentTime = new CustomDateTimeUtil().getTime();
        if (CommonUtil.compareStrings(currentTime, "002000") <= 0 || CommonUtil.compareStrings(currentTime, "235000") >= 0) {
            return true;
        }
        return false;
    }
}
