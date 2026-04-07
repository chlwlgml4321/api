package kr.co.hectofinancial.mps.global.aop;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import kr.co.hectofinancial.mps.api.v1.common.dto.CommonInfoByCardRequestDto;
import kr.co.hectofinancial.mps.api.v1.common.dto.CommonInfoByMidRequestDto;
import kr.co.hectofinancial.mps.api.v1.common.dto.CommonInfoRequestDto;
import kr.co.hectofinancial.mps.api.v1.common.dto.CommonLogicalRequestDto;
import kr.co.hectofinancial.mps.api.v1.common.service.CommonService;
import kr.co.hectofinancial.mps.api.v1.customer.dto.CustomerDto;
import kr.co.hectofinancial.mps.api.v1.customer.dto.CustomerRequestDto;
import kr.co.hectofinancial.mps.api.v1.giftcard.bundle.constants.DstbPrdtCd;
import kr.co.hectofinancial.mps.api.v1.giftcard.single.dto.GiftCardIssueRequestDto;
import kr.co.hectofinancial.mps.api.v1.giftcard.single.dto.GiftCardReissueRequestDto;
import kr.co.hectofinancial.mps.api.v1.giftcard.single.dto.GiftCardSearchRequestDto;
import kr.co.hectofinancial.mps.api.v1.giftcard.single.dto.GiftCardUseRequestDto;
import kr.co.hectofinancial.mps.api.v1.market.domain.MarketAddInfo;
import kr.co.hectofinancial.mps.api.v1.market.dto.MarketAddInfoDto;
import kr.co.hectofinancial.mps.api.v1.trade.dto.charge.ChargeApprovalRequestDto;
import kr.co.hectofinancial.mps.global.annotation.DateFormat;
import kr.co.hectofinancial.mps.global.annotation.EncField;
import kr.co.hectofinancial.mps.global.annotation.HashField;
import kr.co.hectofinancial.mps.global.annotation.NotLoggableParam;
import kr.co.hectofinancial.mps.global.constant.CustStatCd;
import kr.co.hectofinancial.mps.global.constant.MpsPrdtCd;
import kr.co.hectofinancial.mps.global.constant.TrdDivCd;
import kr.co.hectofinancial.mps.global.error.ErrorCode;
import kr.co.hectofinancial.mps.global.error.exception.RequestValidationException;
import kr.co.hectofinancial.mps.global.mtms.MonitAgent;
import kr.co.hectofinancial.mps.global.util.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.util.ContentCachingRequestWrapper;

import javax.servlet.http.HttpServletRequest;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * Controller 진입 전, ~RequestDto 값 검증
 * 모든 RequestDto 내 custNo 값 필수! -> 조회성은 commonInfoRequestDto 상속, 로직성은 commonLogicalRequestDto 상속받으면 된다.
 */

@Aspect
@Component
@RequiredArgsConstructor
@Slf4j
public class RequestValidationAspect {
    private final Environment env;
    private final CommonService commonService;

    /**
     * controller 진입 전 모든 요청의 필수 파라미터인 custNo 로 회원 검증, 상점 검증 및
     * Dto 내의 @EncField 붙은 값 복호화
     * <p>
     * 회원조회API 는 custNo or custId 둘중 하나와 ci값만 넘겨주어도 회원조회 가능
     */
    @Before("(execution(* kr.co.hectofinancial.mps.api.v1.*.controller..*(..)) || execution(* kr.co.hectofinancial.mps.api.v1.*.*.controller..*(..))) && " +
            "@within(org.springframework.web.bind.annotation.RestController)")
    public void validateRequest(JoinPoint joinPoint) throws Exception {

        long requestStartTime = System.currentTimeMillis(); //소요시간 계산용 시작시간

        MethodSignature methodSignature = (MethodSignature) joinPoint.getSignature();
        Method method = methodSignature.getMethod();
        String classNm = methodSignature.getDeclaringTypeName();
        String controllerNm = classNm.substring(classNm.lastIndexOf(".") + 1);
        boolean isSoftValidation = false;
        boolean isCardRequest = false;

        //API 요청 URL 에 따라 prdtCd 가 다르기때문에 HttpServletRequest 사용하여 분기처리 (PrdtCd로 상점오픈정보 조회),
        //회원정보조회는 PrdtCd로 상점오픈정보를 확인하지는 않으나, 요청 파라미터 내 custNo, custId 둘 중 하나만 있어도 되므로 이부분 분기처리 용이하게 하기위해 PrdtCd = CI 로 줌
        //사용취소, 포인트회수, 관리자출금은 stat_cd 검증 유연하게함 (isSoftValidation=true);
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        String prdtCd = null;
        String clientIP = null;
        HttpServletRequest request = null;
        if (attributes != null) {
            request = attributes.getRequest();
            clientIP = getRemoteAddr(request);
            String url = request.getRequestURI().toLowerCase();
            switch (url) {
                case "/v1/approval/charge":
                    prdtCd = MpsPrdtCd.charge.getPrdtCd();
                    break;
                case "/v1/wallet/use":
                case "/v1/wallet/use/each":
                case "/v1/money/gift":
                case "/v1/wallet/transfer":
                case "/v1/card/use":
                case "/v1/giftcard/issue":
                    prdtCd = MpsPrdtCd.use.getPrdtCd();
                    break;
                case "/v1/money/withdrawal":
                case "/v1/money/wait/withdrawal":
                    prdtCd = MpsPrdtCd.withdrawal.getPrdtCd();
                    break;
                case "/v1/customer/info":
                    prdtCd = "CI";
                    break;
                case "/v1/wallet/use/cancel":
                case "/v1/point/revoke"://TODO 상품볼지말지
                case "/v1/money/admin/withdrawal": //TODO 상품볼지말지
                case "/v1/approval/charge/cancel": //TODO 상품볼지말지
                case  "/v1/card/use/cancel":
                    isSoftValidation = true;
                    break;
                case "/v1/card/use/param":
                case "/v1/card/use/cancel/param":
                    isCardRequest = true;
                    break;
            }
            log.info("------------- REQUEST URI: [{}], isSoftCustomerStatusCheck: [{}]", url, isSoftValidation);
        }
        log.info("------------- 거래 시작 METHOD: [{}], CONTROLLER: [{}]", method.getName(), controllerNm);

        Object[] args = joinPoint.getArgs();

        log.info("------------- 요청 파라미터 시작");
        for (Object arg : args) {
            Arrays.stream(arg.getClass().getSuperclass().getDeclaredFields()).forEach(field -> {
                extractRequestParameter(arg, field);
            });
            Arrays.stream(arg.getClass().getDeclaredFields()).forEach(field -> {
                extractRequestParameter(arg, field);
            });
        }

        if (attributes != null) {
            // args에서 DTO 하나 선택 (규칙: 클래스명이 *RequestDto 로 끝나는 첫 번째 객체)
            Object dtoCandidate = Arrays.stream(args)
                    .filter(o -> o != null && o.getClass().getSimpleName().endsWith("RequestDto"))
                    .findFirst()
                    .orElse(null);

            if (dtoCandidate != null) {
                logUnexpectedParameters(request, dtoCandidate);
            }
        }
        log.info("------------- 요청 파라미터 종료");


        //상점 AES 암호화 키 담기위한 변수 선언
        MarketAddInfoDto marketAddInfo = null;
        Object dto = null;

        //요청 dto 가 조회성 dto 일 경우 (회원정보조회, 거래내역조회 등)
        for (Object arg : args) {
            if (arg instanceof CommonInfoRequestDto) {
//                CustomerDto customerByMCustId = null;
                CommonInfoRequestDto commonInfoRequestDto = (CommonInfoRequestDto) arg;

                if (StringUtils.isNotBlank(prdtCd) && "CI".equals(prdtCd)) {
                    String mid = ((CustomerRequestDto) commonInfoRequestDto).getMid();
                    String custNo = commonInfoRequestDto.getCustNo();
                    String custIdEnc = ((CustomerRequestDto) commonInfoRequestDto).getCustId();

                    //상점이 있는지 먼저 조회
                    marketAddInfo = commonService.getMarketAddInfoByMId(mid);
                    if (StringUtils.isNotBlank(custNo)) {
                        //회원번호 있는 경우
                        CustomerDto customerDto = getValidCustOmerDtoForCustomerInfo(custNo, mid);
                        commonInfoRequestDto.setCustomerDto(customerDto);
                    } else if (StringUtils.isNotBlank(custIdEnc)) {
                        //회원번호 없음 => 무조건 custId 있음
                        CustomerDto customerByMCustId = commonService.getCustomerByMCustId(custIdEnc, marketAddInfo);
                        commonInfoRequestDto.setCustomerDto(customerByMCustId);
                    } else {
                        //회원번호 , 회원아이디 둘다 없음
                        throw new RequestValidationException(ErrorCode.CUSTOMER_NOT_FOUND, " (선불 회원 번호 혹은 선불 회원 아이디 (= 상점 고객 아이디) 중 하나는 필수 입니다.)");
                    }
                    commonInfoRequestDto.setRequestIp(clientIP);
                    commonInfoRequestDto.setRequestStartTime(requestStartTime);
                    dto = commonInfoRequestDto;

                } else {
                    //거래내역조회/거래상세조회
                    CustomerDto customerDto = getValidCustomerDtoForTradeList(commonInfoRequestDto.getCustNo());

                    commonInfoRequestDto.setCustomerDto(customerDto);
                    commonInfoRequestDto.setRequestIp(clientIP);
                    commonInfoRequestDto.setRequestStartTime(requestStartTime);
                    dto = commonInfoRequestDto;

                    //가맹점 open info
                    marketAddInfo = commonService.getMarketAddInfoByMId(customerDto.getMid());
                }
            }
            //요청 dto 가 로직성 dto 일 경우 (사용, 충전 등)
            if (arg instanceof CommonLogicalRequestDto) {

                String url = request.getRequestURI().toLowerCase();
                String trdDivCd = null;
                CommonLogicalRequestDto commonLogicalRequestDto = (CommonLogicalRequestDto) arg;
                commonLogicalRequestDto.setRequestIp(clientIP);
                commonLogicalRequestDto.setRequestStartTime(requestStartTime);
                if (arg instanceof ChargeApprovalRequestDto) {
                    trdDivCd = ((ChargeApprovalRequestDto) arg).getDivCd();
                }

                commonLogicalRequestDto.setCustomerDto(isSoftValidation ? getValidCustomerDtoWithSoftCheck(commonLogicalRequestDto.getCustNo()) : getValidCustomerDto(commonLogicalRequestDto.getCustNo(), url, trdDivCd));
                dto = commonLogicalRequestDto;

                //가맹점 open info
                String mid = commonLogicalRequestDto.getCustomerDto().getMid();
                if (prdtCd != null) {
                    commonService.validateMarketOpenInfoByMIdAndPrdtCd(mid, null, prdtCd);
                }
                marketAddInfo = commonService.getMarketAddInfoByMId(mid);
            }

            // 요청 dto에 회원번호 없이 mid 만 들어올 경우
            if (arg instanceof CommonInfoByMidRequestDto) {
                CommonInfoByMidRequestDto commonInfoByMidRequestDto = (CommonInfoByMidRequestDto) arg;
                commonInfoByMidRequestDto.setRequestIp(clientIP);
                commonInfoByMidRequestDto.setRequestStartTime(requestStartTime);
                dto = commonInfoByMidRequestDto;

                marketAddInfo = commonService.getMarketAddInfoByMId(commonInfoByMidRequestDto.getMid());
            }

            // BPO카드 승인/승인취소 DTO 세팅
            if (arg instanceof CommonInfoByCardRequestDto) {
                CommonInfoByCardRequestDto commonInfoByCardRequestDto = (CommonInfoByCardRequestDto) arg;
                commonInfoByCardRequestDto.setRequestIp(clientIP);
                commonInfoByCardRequestDto.setRequestStartTime(requestStartTime);
                dto = commonInfoByCardRequestDto;
            }
        }
        String requestURI = attributes.getRequest().getRequestURI();
        boolean giftcardPktHashCheckFlag = false;

        /* 묶음상품권 API 공통 처리 */
        if (requestURI.startsWith("/v1/giftcard/bundle")) {
            giftcardPktHashCheckFlag = true;
            dto = args[0];

            Field mIdField = dto.getClass().getDeclaredField("useMid");
            mIdField.setAccessible(true);
            String mid = (String) mIdField.get(dto);
            if (StringUtils.isBlank(mid)) {
                throw new RequestValidationException(ErrorCode.ESSENTIAL_PARAM_EMPTY_FIELD_NAME, " 사용처 상점 아이디");
            }

            /* 상점 정보 조회 */
            Optional<MarketAddInfo> optionalMarketAddInfo = commonService.getOnlyMarketAddInfoByMId(mid);
            if (!optionalMarketAddInfo.isPresent()) {
                throw new RequestValidationException(ErrorCode.MARKET_ADD_INFO_NOT_FOUND);
            }

            marketAddInfo = MarketAddInfoDto.of(optionalMarketAddInfo.get());

            switch (requestURI) { // 줄이는 방향으로 TODO: 탈퇴 시 유통잔액 처리
                case "/v1/giftcard/bundle/balance/use": // 유통잔액 사용 (선불 PIN 충전)
                case "/v1/giftcard/bundle/balance/use/cancel": // 유통잔액 사용취소
                case "/v1/giftcard/bundle/issue": // 유통잔액 사용 - 묶음상품권 발행
                    commonService.validateMarketOpenInfoByMIdAndPrdtCd(mid, null, DstbPrdtCd.USE.getPrdtCd());
                    break;
                case "/v1/giftcard/bundle/charge/etc": // 유통잔액충전
                case "/v1/giftcard/bundle/transfer": // 묶음상품권 양도
                    commonService.validateMarketOpenInfoByMIdAndPrdtCd(mid, null, DstbPrdtCd.CHARGE.getPrdtCd());
                    break;
                case "/v1/giftcard/bundle/info": // 묶음상품권 조회
                case "/v1/giftcard/bundle/balance": // 유통 잔액 조회
                case "/v1/giftcard/bundle/list": // 묶음상품권 목록 조회
                    commonService.validateMarketOpenInfoByMIdAndPrdtCd(mid, null, DstbPrdtCd.CHARGE.getPrdtCd());
                    giftcardPktHashCheckFlag = false;
                    break;
                case "/v1/giftcard/bundle/use": // 묶음상품권 사용
                    commonService.validateMarketOpenInfoByMIdAndPrdtCd(mid, null, DstbPrdtCd.GIFTCARD_USE.getPrdtCd());
                    break;
            }

            /* TODO: 추후 삭제 */
            if (requestURI.equals("/v1/giftcard/bundle/charge")) {
                /* 복호화 */
                Field gcDstbNoField = dto.getClass().getDeclaredField("gcDstbNo");
                gcDstbNoField.setAccessible(true);
                String gcDstbNo = (String) gcDstbNoField.get(dto);
                if (StringUtils.isBlank(gcDstbNo)) {
                    throw new RequestValidationException(ErrorCode.ESSENTIAL_PARAM_EMPTY_FIELD_NAME, " 유통관리번호");
                }

                String custNo;
                try {
                    custNo = CipherUtil.decrypt(gcDstbNo, marketAddInfo.getEncKey());
                } catch (Exception ex) {
                    throw new RequestValidationException(ErrorCode.DECRYPT_ERROR, " 유통관리번호");
                }

                CustomerDto customerDto = getValidCustomerDto(custNo, requestURI, TrdDivCd.GIFT_CARD_ISSUE.getTrdDivCd());

                Field customerDtoField = dto.getClass().getDeclaredField("customerDto");
                customerDtoField.setAccessible(true);
                customerDtoField.set(dto, customerDto);

                /* 상품코드 유효성 확인 */
                commonService.validateMarketOpenInfoByMIdAndPrdtCd(mid, null, DstbPrdtCd.CHARGE.getPrdtCd());
            }

            Field clientIp1 = dto.getClass().getDeclaredField("clientIp");
            clientIp1.setAccessible(true);
            clientIp1.set(dto, clientIP);

            Field requestStartTime1 = dto.getClass().getDeclaredField("requestStartTime");
            requestStartTime1.setAccessible(true);
            requestStartTime1.set(dto, requestStartTime);
        } else if (requestURI.startsWith("/v1/giftcard")) {
            Object obj = args[0];
            String mid = "";
            String useMid = "";

            if ("/v1/giftcard/issue".equals(requestURI)) {
                GiftCardIssueRequestDto giftCardIssueRequestDto = (GiftCardIssueRequestDto) obj;
                dto = giftCardIssueRequestDto;

                CustomerDto customerDto = getValidCustomerDto(giftCardIssueRequestDto.getCustNo(), requestURI, TrdDivCd.GIFT_CARD_ISSUE.getTrdDivCd());
                ((GiftCardIssueRequestDto) dto).setCustomerDto(customerDto);

                mid = customerDto.getMid();
                useMid = giftCardIssueRequestDto.getUseMid();
                marketAddInfo = commonService.getMarketAddInfoByMId(mid);

                commonService.validateMarketOpenInfoByMIdAndPrdtCd(mid, null, prdtCd);  //발행시 mid로 PUSE 확인
            } else {
                switch (requestURI) {
                    case "/v1/giftcard/use":
                        GiftCardUseRequestDto giftCardUseRequestDto = (GiftCardUseRequestDto) obj;
                        dto = giftCardUseRequestDto;
                        giftcardPktHashCheckFlag = true;
                        break;
                    case "/v1/giftcard/reissue":
                        GiftCardReissueRequestDto giftCardReissueRequestDto = (GiftCardReissueRequestDto) obj;
                        dto = giftCardReissueRequestDto;
                        giftcardPktHashCheckFlag = true;
                        break;
                    case "/v1/giftcard/search":
                        GiftCardSearchRequestDto giftCardSearchRequestDto = (GiftCardSearchRequestDto) obj;
                        dto = giftCardSearchRequestDto;
                        break;

                }
                Field useMidField = dto.getClass().getDeclaredField("useMid");
                useMidField.setAccessible(true);
                useMid = (String) useMidField.get(dto);
                mid = useMid;

                marketAddInfo = commonService.getMarketAddInfoByMId(mid);
            }

            commonService.validateMarketOpenInfoByMIdAndPrdtCd(useMid, null, DstbPrdtCd.GIFTCARD_USE.getPrdtCd()); //발행 포함 전부 useMid 로 UPIN 확인

            Field clientIp1 = dto.getClass().getDeclaredField("clientIp");
            clientIp1.setAccessible(true);
            clientIp1.set(dto, clientIP);

            Field requestStartTime1 = dto.getClass().getDeclaredField("requestStartTime");
            requestStartTime1.setAccessible(true);
            requestStartTime1.set(dto, requestStartTime);
        }

        if (dto == null) {
            throw new RequestValidationException(ErrorCode.REQUEST_DTO_CANNOT_BE_NULL);
        }
        if (!isCardRequest && marketAddInfo == null) {
            throw new RequestValidationException(ErrorCode.MARKET_ADD_INFO_NOT_FOUND);
        }
        //local, dev 제외
        if (Arrays.stream(env.getActiveProfiles()).anyMatch(s -> s.toLowerCase().equals("local") || s.toLowerCase().equals("dev"))) {
            return;
        }

        getValidMinMax(dto);

        getValidDateFormat(dto);

        /* Check NotBlank */
        getValidNotBlank(dto);

        if (!isCardRequest) {
            getDecryptedDto(marketAddInfo.getEncMthdCd(), marketAddInfo.getEncKey(), marketAddInfo.getEncIv(), dto);
        }

        //Check pktHash In CommonLogicalRequestDto
        if (dto instanceof CommonLogicalRequestDto) {
            checkPktHash(marketAddInfo.getPktHashKey(), (CommonLogicalRequestDto) dto);
        } else if (giftcardPktHashCheckFlag) {
            checkPktHashForGiftCard(marketAddInfo, dto);
        }
    }

    /**
     * json 데이터를 읽은 후 , dto 에 선언되어 있지 않은 파라미터 key, valu 를 로그에 찍는 메서드
     *
     * @param request
     * @param dto
     */
    private void logUnexpectedParameters(HttpServletRequest request, Object dto) {
        if (!(request instanceof ContentCachingRequestWrapper) || dto == null) {
            return;
        }
        try {
            String jsonBody = new String(((ContentCachingRequestWrapper) request).getContentAsByteArray(), "UTF-8");
            if (jsonBody.isEmpty()) {
                return;
            }
            ObjectMapper om = new ObjectMapper();
            Map<String, Object> map = om.readValue(jsonBody, new TypeReference<Map<String, Object>>() {
            });

            Set<String> acceptedDtoKeys = extractAcceptedDtoKeys(dto.getClass());
            for (Map.Entry<String, Object> entry : map.entrySet()) {
                if (!acceptedDtoKeys.contains(entry.getKey())) {
                    //dto 에 매핑되지 않은 요청 파라미터 key, value 로깅
                    log.info("** NOT_DTO_PARAM => {} :[{}]", entry.getKey(), entry.getValue());
                }
            }
        }   // === 상세 예외 분기 ===
        catch (JsonMappingException e) {
            // JSON 구조/타입 매핑 이슈 (루트가 배열, 타입 불일치, 중첩 구조 문제 등)
            log.warn("JSON mapping error while reading body: {}", e.getOriginalMessage());
        } catch (JsonProcessingException e) {
            // Jackson 처리 전반(파싱 포함)의 상위 예외. 문법 오류 등도 여기로 들어올 수 있음.
            log.warn("JSON processing error: {}", e.getOriginalMessage());
        } catch (UnsupportedEncodingException e) {
            // 문자셋 이름이 잘못되었거나 미지원
            log.warn("Unsupported request charset [{}]: {}", request.getCharacterEncoding(), e.getMessage());
        } catch (IOException e) {
            // 그 외 I/O 문제
            log.warn("I/O error while reading request body: {}", e.getMessage());
        } catch (Exception e) {
            // 방어적 최종 캐치
            log.warn("Unexpected error while logging unmapped JSON fields: {}", e.getMessage());
        }
    }

    /**
     * dto에 선언된 필드 + JsonProperty 가 붙어있는 필드만 담아서 return 하는 메서드
     *
     * @param dtoClass
     * @return
     */
    private Set<String> extractAcceptedDtoKeys(Class<?> dtoClass) {
        Set<String> names = new HashSet<>();
        Class<?> cur = dtoClass;

        while (cur != null && cur != Object.class) {
            for (Field f : cur.getDeclaredFields()) {
                if (f.isAnnotationPresent(NotLoggableParam.class)) {
                    continue; // 로그 제외 어노테이션
                }
                names.add(f.getName());
                JsonProperty jp = f.getAnnotation(JsonProperty.class);
                if (jp != null && !jp.value().isEmpty()) {
                    names.add(jp.value());
                }
            }
            cur = cur.getSuperclass();
        }
        return names;
    }


    private static void extractRequestParameter(Object arg, Field field) {
        field.setAccessible(true);
        if (field.isAnnotationPresent(NotLoggableParam.class)) {
            return;
        }
        try {
            Object value = field.get(arg);
            if (value != null) {
                log.info("{} :[{}]", field.getName(), value);
            }
        } catch (IllegalAccessException e) {
            log.error("{} :[{}]", field.getName(), "값 추출 실패");
        }
    }

    /**
     * 요청 파라미터가 CommonLogicalRequestDto 일 경우, pktHash 조합하여 검증 한다
     *
     * @param hashFirstStr (custNo + mId 조합)
     * @param hashLastStr  (hashKey)
     * @param dto
     */
    private void checkPktHash(String hashLastStr, CommonLogicalRequestDto dto) {
        String clientPktHash = dto.getPktHash(); //요청단에 들어있는 pktHash
        String serverPktHash = ""; //TOBE pktHash
        StringBuilder builder = new StringBuilder().append(dto.getCustomerDto().getMpsCustNo() + dto.getCustomerDto().getMid()); //TOBE serverPktHash
        StringBuilder builder1 = new StringBuilder().append("CUST_NO , M_ID");
        //@HashField 담을 array
        List<Field> allFields = new ArrayList<>();
        //SuperClass 인 CommonLogicalRequestDto
        allFields.addAll(Arrays.asList(dto.getClass().getSuperclass().getDeclaredFields()));
        // 자식 클래스 인 ~RequestDto
        allFields.addAll(Arrays.asList(dto.getClass().getDeclaredFields()));

        List<Field> sortedFields = new ArrayList<>();
        allFields.stream().filter(field -> field.isAnnotationPresent(HashField.class))
                .sorted(Comparator.comparingInt(field -> field.getAnnotation(HashField.class).order()))
                .forEach(sortedFields::add);
        try {
            for (Field field : sortedFields) {
                if (field.getAnnotation(HashField.class).order() > 1) {
                    field.setAccessible(true);
                    Object value = field.get(dto);
                    builder.append(value);
                    builder1.append((", " + field.getName()));
                }
            }
            String plain = builder.append(hashLastStr).toString();
            serverPktHash = CipherSha256Util.digestSHA256(plain);
        } catch (Exception e) {
            throw new RequestValidationException(ErrorCode.PKT_HASH_MAKING_EXCEPTION);
        }
        if (!serverPktHash.equals(clientPktHash)) {
            log.info("*** pktHash plain text without hashkey [{}] ", builder1.toString().split(hashLastStr)[0]);
            log.info("*** pktHash 불일치로 검증 실패! 생성값[{}]", serverPktHash);
            throw new RequestValidationException(ErrorCode.PKT_HASH_NOT_MATCHED);
        }
    }

    /**
     * 요청 파라미터가 상품권(voucher) 관련 requestDto 일 경우, pktHash 조합하여 검증 한다
     *
     * @param pktHashKey
     * @param dto
     */
    private void checkPktHashForGiftCard(MarketAddInfoDto marketAddInfo, Object dto) {
        StringBuilder serverPktHashBuilder = new StringBuilder();
        String pktHashKey = marketAddInfo.getPktHashKey();
        String serverPktHash = "";
        String clientPktHash = "";
        try {
            Field pktHash = dto.getClass().getDeclaredField("pktHash");
            pktHash.setAccessible(true);
            clientPktHash = (String) pktHash.get(dto);
        } catch (NoSuchFieldException e) {
            log.error("pktHash 필드 없음, requestDto[{}] 변수명 확인 필요 ", dto.getClass().getSimpleName());
            throw new RequestValidationException(ErrorCode.PKT_HASH_MAKING_EXCEPTION);
        } catch (IllegalAccessException e) {
            log.error("pktHash 추출 실패, requestDto[{}] 확인 필요 ", dto.getClass().getSimpleName());
            throw new RequestValidationException(ErrorCode.PKT_HASH_MAKING_EXCEPTION);
        }

        Arrays.stream(dto.getClass().getDeclaredFields())
                .filter(field -> field.isAnnotationPresent(HashField.class))
                .sorted(Comparator.comparingInt(field -> field.getAnnotation(HashField.class).order()))
                .forEach(field -> {
                    field.setAccessible(true);
                    try {
                        HashField hashField = field.getAnnotation(HashField.class);
                        Object value = field.get(dto);
                        if (hashField.order() == 1 && dto instanceof GiftCardIssueRequestDto) {
                            value += marketAddInfo.getMid();
                        }
                        serverPktHashBuilder.append(value);
                    } catch (IllegalAccessException e) {
                        log.error("serverPktHash 생성 도중 {} value 추출 실패 ", field.getName());
                        throw new RequestValidationException(ErrorCode.PKT_HASH_MAKING_EXCEPTION);
                    }
                });
        try {
            serverPktHash = CipherSha256Util.digestSHA256(serverPktHashBuilder.append(pktHashKey).toString());
        } catch (NoSuchAlgorithmException e) {
            log.error("serverPktHash 생성 실패", e);
            throw new RequestValidationException(ErrorCode.PKT_HASH_MAKING_EXCEPTION);
        }
        if (StringUtils.isBlank(clientPktHash) || StringUtils.isBlank(serverPktHash)) {
            log.error("*** clientPktHash [{}] serverPktHash [{}] 값 확인 필요", clientPktHash, serverPktHash);
            throw new RequestValidationException(ErrorCode.PKT_HASH_MAKING_EXCEPTION);
        }
        if (!serverPktHash.equals(clientPktHash)) {
            log.info("*** pktHash plain text without hashkey [{}]", serverPktHashBuilder.toString().split(pktHashKey)[0]);
            log.info("*** pktHash 불일치로 검증 실패! serverPktHash[{}]", serverPktHash);
            throw new RequestValidationException(ErrorCode.PKT_HASH_NOT_MATCHED);
        }
    }

    /**
     * 넘겨받은 암호화 정보와 DTO를 가지고 DTO 내 @EncField 값을 복호화 한다
     *
     * @param encMthdCd
     * @param encKey
     * @param encIv
     * @param dto
     */
    private void getDecryptedDto(String encMthdCd, String encKey, String encIv, Object dto) {
        Field[] fields = dto.getClass().getDeclaredFields();
        for (Field field : fields) {
            if (field.isAnnotationPresent(EncField.class)) {
                field.setAccessible(true);
                String fieldName = field.getName();
                Object value = null;
                try {
                    if (field.getAnnotation(EncField.class).nullable() && field.get(dto) == null) {
                        continue;
                    }

                    value = field.get(dto);

                    /* 암호화 필드 값이 공백인 경우 */
                    if (CommonUtil.nullTrim(String.valueOf(value)).equals("")) {
                        if (field.getAnnotation(EncField.class).nullable()) {
                            throw new RequestValidationException(ErrorCode.ESSENTIAL_PARAM_EMPTY_FIELD_NAME, fieldName);
                        }
                    }

                    String decryptedValue = CipherUtil.decrypt(String.valueOf(value), encKey);
                    field.set(dto, decryptedValue);
                    if (CommonUtil.nullTrim(decryptedValue).equals("")) {
                        if (field.getAnnotation(EncField.class).nullable()) {
                            throw new RequestValidationException(ErrorCode.DECRYPT_ERROR, ("(" + fieldName + ")"));
                        }
                        throw new RequestValidationException(ErrorCode.ESSENTIAL_PARAM_EMPTY_FIELD_NAME);
                    }

                    /* 금액 검증 */
                    if (fieldName.endsWith("Blc") || fieldName.endsWith("Amt")) {
                        getValidAmt(decryptedValue);
                    }

                } catch (Exception e) {
                    log.error("DTO 복호화 실패! 필드명[{}]::{}", fieldName, e.getMessage());
                    if (e.getMessage().equals(ErrorCode.ESSENTIAL_PARAM_EMPTY_FIELD_NAME.getErrorMessage())) {
                        throw new RequestValidationException(ErrorCode.ESSENTIAL_PARAM_EMPTY_FIELD_NAME, fieldName);
                    } else if (e.getMessage().equals(ErrorCode.NOT_VALID_AMT.getErrorMessage())) {
                        throw new RequestValidationException(ErrorCode.NOT_VALID_AMT);
                    } else if (e.getMessage().equals(ErrorCode.DECRYPT_ERROR.getErrorMessage())) {
                        throw new RequestValidationException(ErrorCode.DECRYPT_ERROR, ("(" + fieldName + ")"));
                    }
                    throw new RequestValidationException(ErrorCode.ENCRYPT_MISSING, ("(" + fieldName + ")"));
                }
            }
        }
    }

    private void getValidNotBlank(Object dto) throws IllegalAccessException {

        Class<?> curClass = dto.getClass();

        while (curClass != null) {
            Field[] fields = curClass.getDeclaredFields();
            for (Field field : fields) {
                if (field.isAnnotationPresent(NotBlank.class)) {
                    field.setAccessible(true);
                    Object value = field.get(dto);
                    NotBlank tgtNotBlank = field.getAnnotation(NotBlank.class);
                    if (CommonUtil.nullTrim(String.valueOf(value)).equals("")) {
                        String message = tgtNotBlank.message();
                        log.error("DTO 필수 값 누락! 필드명: [{}]", message);
                        throw new RequestValidationException(ErrorCode.ESSENTIAL_PARAM_EMPTY_FIELD_NAME, message);
                    }
                }
            }
            curClass = curClass.getSuperclass(); // 상위 클래스로 이동
        }

    }

    private void getValidDateFormat(Object dto) throws IllegalAccessException {

        Field[] fields = dto.getClass().getDeclaredFields();
        for (Field field : fields) {
            if (field.isAnnotationPresent(DateFormat.class)) {
                field.setAccessible(true);

                Object value = field.get(dto);
                DateFormat dateFormat = field.getAnnotation(DateFormat.class);
                String pattern = dateFormat.pattern();
                String message = dateFormat.message();

                if (value instanceof String) {
                    String dateStr = (String) value;
                    if (CommonUtil.nullTrim(dateStr).equals("")) {
                        continue;
                    }

                    CustomDateTimeUtil customDateTimeUtil = new CustomDateTimeUtil();
                    if (pattern.equals("yyyyMMdd")) {
                        log.info("yyyyMMdd :[{}]", dateStr);
                        if (!customDateTimeUtil.isValidDate(dateStr)) {
                            throw new RequestValidationException(ErrorCode.DATE_VALID_ERROR, StringUtils.isNotBlank(message) ? "(" + message + ")" : "");
                        }
                    } else if (pattern.equals("HHmmss")) {
                        log.info("HHmmss :[{}]", dateStr);
                        if (!customDateTimeUtil.isValidTime(dateStr)) {
                            throw new RequestValidationException(ErrorCode.DATE_VALID_ERROR, StringUtils.isNotBlank(message) ? "(" + message + ")" : "");
                        }
                    } else if (pattern.equals("yyyyMM")) {
                        log.info("yyyyMM :[{}]", dateStr);
                        if (!customDateTimeUtil.isValidPeriod(dateStr)) {
                            throw new RequestValidationException(ErrorCode.DATE_VALID_ERROR, StringUtils.isNotBlank(message) ? "(" + message + ")" : "");
                        }
                    }
                }
            }
        }
    }

    private void getValidMinMax(Object dto) throws IllegalAccessException {

        Field[] fields = dto.getClass().getDeclaredFields();
        for (Field field : fields) {
            if (field.isAnnotationPresent(Min.class)) {
                field.setAccessible(true);
                Object value = field.get(dto);
                if (value != null && value instanceof Number) { //
                    Number numberValue = (Number) value;

                    Min minAnnotation = field.getAnnotation(Min.class);
                    long minValue = minAnnotation.value();

                    // 필드 값이 최소값보다 작은지 확인
                    if (numberValue.longValue() < minValue) {
                        throw new RequestValidationException(ErrorCode.MIN_MAX_VALID_ERROR);
                    }
                }
            }

            if (field.isAnnotationPresent(Max.class)) {
                field.setAccessible(true);
                Object value = field.get(dto);

                if (value != null && value instanceof Number) {
                    Number numberValue = (Number) value;

                    Max maxAnnotation = field.getAnnotation(Max.class);
                    long maxValue = maxAnnotation.value();

                    // 필드 값이 최대값보다 큰지 확인
                    if (numberValue.longValue() > maxValue) {
                        throw new RequestValidationException(ErrorCode.MIN_MAX_VALID_ERROR);
                    }
                }
            }
        }
    }

    /* 금액 필드 검증 추가 */
    private void getValidAmt(String data) {
        try {
            BigDecimal bigDecimal = new BigDecimal(data);
            if (bigDecimal.signum() < 0 || bigDecimal.scale() > 0) { //금액 필드 0원미만, 소수점 포함
                throw new RequestValidationException(ErrorCode.NOT_VALID_AMT);
            }
        } catch (NumberFormatException | ArithmeticException e) {
            throw new RequestValidationException(ErrorCode.NOT_VALID_AMT);
        }
    }


    /**
     * 회원 번호로 유효한 회원인지 검증하여 , 유효한 회원인 경우 customerDto를 return 한다
     * 상점이 빌키 사용 Y 이면서 회원상태가 잠금 (L) 일 경우는 통과한다
     *
     * @param custNo
     * @return CustomerDto
     */
    private CustomerDto getValidCustomerDto(String custNo, String url, String trDivCd) {

        if (custNo == null) {
            throw new RequestValidationException(ErrorCode.ESSENTIAL_PARAM_EMPTY_FIELD_NAME,"선불 회원 번호");
        }
        /* 회원 상태 검증 */
        CustomerDto customerDto = commonService.getCustomerByCustNo(custNo);
        String statCd = customerDto.getStatCd();
        String mid = customerDto.getMid();

        if (!CustStatCd.isValid(statCd)) {
            MonitAgent.sendMonitAgent(ErrorCode.NOT_REGISTERED_COMM_CODE.getErrorCode(), "미등록된 회원상태 코드: " + statCd + " \n회원번호: " + custNo);
            throw new RequestValidationException(ErrorCode.NOT_REGISTERED_CUSTOMER_STAT_CD);
        }

        if (url.equals("/v1/approval/charge") && trDivCd.equals(TrdDivCd.POINT_PROVIDE.getTrdDivCd())) {
            if (!statCd.equals(CustStatCd.STANDARD.getStatCd()) && !statCd.equals(CustStatCd.KYC_RENEW_NEEDED.getStatCd())) {
                if (statCd.equals(CustStatCd.LOCK.getStatCd())) {
                    //상점이 빌키 사용 Y 일 경우, 통과
                    if (StringUtils.isNotBlank(customerDto.getBillKeyEnc()) && commonService.isMpsMarketBillKeyEnable(mid)) {
                        log.info("custNo={} (LOCKED) -> allowed by MpsMarket={} billKeyUseYn=Y", custNo, mid);
                        return customerDto;
                    }
                }
                throw new RequestValidationException(ErrorCode.CUSTOMER_STATUS_NOT_VALID);
            }
        } else {
            if (!statCd.equals(CustStatCd.STANDARD.getStatCd())) {
                if (statCd.equals(CustStatCd.WITHDRAW.getStatCd())) {
                    throw new RequestValidationException(ErrorCode.CUSTOMER_STATUS_WITHDRAW);
                } else if (statCd.equals(CustStatCd.WAITTING.getStatCd())) {
                    throw new RequestValidationException(ErrorCode.CUSTOMER_STATUS_WAITTING);
                } else if (statCd.equals(CustStatCd.STOP.getStatCd())) {
                    throw new RequestValidationException(ErrorCode.CUSTOMER_STATUS_STOP);
                } else if (statCd.equals(CustStatCd.LOCK.getStatCd())) {
                    //상점이 빌키 사용 Y 일 경우, 통과
                    if (StringUtils.isNotBlank(customerDto.getBillKeyEnc()) && commonService.isMpsMarketBillKeyEnable(mid)) {
                        log.info("custNo={} (LOCKED) -> allowed by MpsMarket={} billKeyUseYn=Y", custNo, mid);
                        return customerDto;
                    }
                    throw new RequestValidationException(ErrorCode.CUSTOMER_STATUS_LOCK);
                } else if (statCd.equals(CustStatCd.KYC_RENEW_NEEDED.getStatCd())) {
                    /* 유예기간에 허용가능한 API 목록 (부릉만 허용. 2026.02.12 by 최준혁 팀장) */
                    if ("M2513093".equals(mid) || "M2548094".equals(mid)) { /* 개발 M_ID || 운영 M_ID) */
                        String kycKindCd = customerDto.getKycKindCd();
                        String kycExecDt = customerDto.getKycExecDt();
                        String kycDueDt = DateTimeUtil.getKycExpiredDate(kycKindCd, kycExecDt);
                        LocalDate kycDueDate = LocalDate.parse(kycDueDt, DateTimeFormatter.ofPattern("yyyyMMdd"));

                        /* 유예기간이 지난 경우 */
                        if (LocalDate.now().isAfter(kycDueDate.plusDays(14))) {
                            throw new RequestValidationException(ErrorCode.KYC_RENEW_NEEDED);
                        }

                        switch (url) {
                            case "/v1/wallet/use":              // 사용
                            case "/v1/wallet/use/cancel":       // 사용취소
                            case "/v1/wallet/use/each":         // 각각사용
                            case "/v1/approval/charge":         // 충전
                            case "/v1/approval/charge/cancel":  // 충전취소
                            case "/v1/wallet/transfer":         // 전환
                            case "/v1/card/use":                // 카드사용
                            case "/v1/card/use/cancel":         // 카드사용취소
                            case "/v1/point/revoke":            // 포인트 회수
                                return customerDto;
                            default:
                                throw new RequestValidationException(ErrorCode.KYC_RENEW_NEEDED);
                        }
                    } else {
                        throw new RequestValidationException(ErrorCode.KYC_RENEW_NEEDED);
                    }
                } else if (statCd.equals(CustStatCd.REJECTED.getStatCd())) {
                    throw new RequestValidationException(ErrorCode.REJECTED_CUSTOMER);
                } else if (statCd.equals(CustStatCd.BLCOK.getStatCd())) {
                    throw new RequestValidationException(ErrorCode.BLOCKED_CUSTOMER);
                }
            }
        }
        //상점 확인(TB_MPS_M)
        return customerDto;
    }

    /**
     * 회원 번호로 유효한 회원인지 검증 후, 유효할 경우 회원의 상점 아이디를 return 한다
     * 사용취소, 포인트회수, 관리자출금만 고객상태 서비스해지 아닐경우 전체 허용 (application.yml 에 mps.customer.exception.methods에 ,로 구분)
     *
     * @param custNo
     * @return mId
     */
    private CustomerDto getValidCustomerDtoWithSoftCheck(String custNo) {
        if (custNo == null) {
            throw new RequestValidationException(ErrorCode.ESSENTIAL_PARAM_EMPTY_FIELD_NAME,"선불 회원 번호");
        }
        /* 회원 상태 검증 */
        CustomerDto customerDto = commonService.getCustomerByCustNo(custNo);
        String statCd = customerDto.getStatCd();
        if (!CustStatCd.isValid(statCd)) {
            MonitAgent.sendMonitAgent(ErrorCode.NOT_REGISTERED_COMM_CODE.getErrorCode(), "미등록된 회원상태 코드: " + statCd + " \n회원번호: " + custNo);
            throw new RequestValidationException(ErrorCode.NOT_REGISTERED_CUSTOMER_STAT_CD);
        }

        //서비스 해지 상태이면 안됨
        if (statCd.equals(CustStatCd.WITHDRAW.getStatCd())) {
            throw new RequestValidationException(ErrorCode.CUSTOMER_STATUS_WITHDRAW);
        }
        log.info(">>>>>> softCustomerCheck custNo[{}] statCd[{}]", custNo, statCd);

        //상점 확인(TB_MPS_M)
        return customerDto;
    }

    /**
     * 거래내역조회/거래상세조회에서 사용하는 customerDto 조회
     *
     * @param custNo
     * @return
     */
    private CustomerDto getValidCustomerDtoForTradeList(String custNo) {
        if (custNo == null) {
            throw new RequestValidationException(ErrorCode.ESSENTIAL_PARAM_EMPTY_FIELD_NAME,"선불 회원 번호");
        }
        return commonService.getCustomerByCustNo(custNo);
    }

    /**
     * 회원정보 조회에서 사용하는 customerDto 조회
     *
     * @param custNo
     * @param mId
     * @return
     */
    private CustomerDto getValidCustOmerDtoForCustomerInfo(String custNo, String mId) {
        return commonService.getCustomerByCustNoAndMId(custNo, mId);
    }

    /**2
     * WAS 는 보통 2차 방화벽 안에 있고 Web Server 를 통해 client 에서 호출되거나 cluster로
     * 구성되어 load balancer 에서 호출되는데
     * 이럴 경우에서 getRemoteAddr() 을 호출하면 웹서버나 load balancer의 IP 가 나옴
     * 위와 같은 문제를 해결하기 위해 사용되는 HTTP Header인 X-Forwarded-For 값을 확인해서 있으면
     * 해당 키값을 사용하고 없으면 getRemoteAddr()사용
     */
    private String getRemoteAddr(HttpServletRequest request) {
        return (null != request.getHeader("X-FORWARDED-FOR")) ? request.getHeader("X-FORWARDED-FOR") : request.getRemoteAddr();
    }


}
