package kr.co.hectofinancial.mps.global.aop;

import kr.co.hectofinancial.mps.api.v1.common.dto.BaseResponseDto;
import kr.co.hectofinancial.mps.api.v1.common.dto.CommonLogicalResponseDto;
import kr.co.hectofinancial.mps.api.v1.common.service.CommonService;
import kr.co.hectofinancial.mps.api.v1.giftcard.bundle.dto.charge.GiftCardBundleChargeEtcResponseDto;
import kr.co.hectofinancial.mps.api.v1.giftcard.bundle.dto.charge.cancel.GiftCardBundleChargeCancelResponseDto;
import kr.co.hectofinancial.mps.api.v1.giftcard.bundle.dto.issue.GiftCardBundleIssueResponseDto;
import kr.co.hectofinancial.mps.api.v1.giftcard.bundle.dto.search.GiftCardBundleBalanceResponseDto;
import kr.co.hectofinancial.mps.api.v1.giftcard.bundle.dto.search.GiftCardBundleInfoResponseDto;
import kr.co.hectofinancial.mps.api.v1.giftcard.bundle.dto.search.GiftCardBundleListResponseDto;
import kr.co.hectofinancial.mps.api.v1.giftcard.bundle.dto.transfer.GiftCardBundleTransferResponseDto;
import kr.co.hectofinancial.mps.api.v1.giftcard.bundle.dto.use.GiftCardBundleUseResponseDto;
import kr.co.hectofinancial.mps.api.v1.giftcard.bundle.dto.use.balance.GiftCardBundleBalanceUseResponseDto;
import kr.co.hectofinancial.mps.api.v1.giftcard.bundle.dto.use.balance.cancel.GiftCardBundleBalanceUseCancelResponseDto;
import kr.co.hectofinancial.mps.api.v1.giftcard.single.dto.GiftCardIssueResponseDto;
import kr.co.hectofinancial.mps.api.v1.giftcard.single.dto.GiftCardReissueResponseDto;
import kr.co.hectofinancial.mps.api.v1.giftcard.single.dto.GiftCardSearchResponseDto;
import kr.co.hectofinancial.mps.api.v1.giftcard.single.dto.GiftCardUseResponseDto;
import kr.co.hectofinancial.mps.api.v1.market.dto.MarketAddInfoDto;
import kr.co.hectofinancial.mps.api.v1.trade.dto.admin.AdminChargeApprovalResponseDto;
import kr.co.hectofinancial.mps.api.v1.trade.dto.charge.ChargeApprovalRequestDto;
import kr.co.hectofinancial.mps.api.v1.trade.dto.money.WithdrawApprovalRequestDto;
import kr.co.hectofinancial.mps.api.v1.trade.dto.point.GetExpPntByMidResponseDto;
import kr.co.hectofinancial.mps.api.v1.trade.dto.wallet.WalletUseRequestDto;
import kr.co.hectofinancial.mps.global.annotation.EncField;
import kr.co.hectofinancial.mps.global.annotation.HashField;
import kr.co.hectofinancial.mps.global.constant.MpsPrdtCd;
import kr.co.hectofinancial.mps.global.error.ErrorCode;
import kr.co.hectofinancial.mps.global.error.exception.RequestValidationException;
import kr.co.hectofinancial.mps.global.mtms.TmsAgent;
import kr.co.hectofinancial.mps.global.util.CipherSha256Util;
import kr.co.hectofinancial.mps.global.util.CipherUtil;
import kr.co.hectofinancial.mps.global.util.CommonUtil;
import kr.co.hectofinancial.mps.global.util.DateTimeUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.core.env.Environment;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.util.ObjectUtils;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 응답 내 BaseResponseDto 의 rsltObj 에 담긴 dto (walletDto, tradeInfoDto 등) 내
 *
 * @EncField 붙은 값은 암호화 해주고, pktHash 있으면 @HashField 의 order 대로 hash 만들어줘야함
 */
@Aspect
@Component
@RequiredArgsConstructor
@Slf4j
public class ResponseDtoFieldEncryptAspect {
    private final CommonService commonservice;
    private final Environment env;

    @Around("(execution(* kr.co.hectofinancial.mps.api.v1.*.controller..*(..)) || execution(* kr.co.hectofinancial.mps.api.v1.*.*.controller..*(..))) && " +
            "@within(org.springframework.web.bind.annotation.RestController)")
    public Object encryptEncFields(ProceedingJoinPoint joinPoint) throws Throwable {

        MethodSignature methodSignature = (MethodSignature) joinPoint.getSignature();
        Method method = methodSignature.getMethod();
        String trPrcsSeq = CommonUtil.makeTrPrcsSeq(method.getName());
        String classNm = methodSignature.getDeclaringTypeName();
        String controllerNm = classNm.substring(classNm.lastIndexOf(".") + 1);

        //TMS 로그 쌓을 때 필요한 변수 선언 (소요시간 계산을 위해서 response 나가기 전에 찍음)
        boolean isTmsLoggable = false;
        String prdtCd = "", requestIp = "", mId = "", trdAmt = "", mTrdNo = "";
        long requestStartTime = 0l;

        //요청 내 dto 가져와서 변수에 값 세팅
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attributes != null) {
            HttpServletRequest request = attributes.getRequest();
            String url = request.getRequestURI().toLowerCase();
            switch (url) {
                case "/v1/approval/charge":
                    isTmsLoggable = true;
                    prdtCd = MpsPrdtCd.charge.getPrdtCd();
                    requestIp = ((ChargeApprovalRequestDto) joinPoint.getArgs()[0]).getRequestIp();
                    requestStartTime = ((ChargeApprovalRequestDto) joinPoint.getArgs()[0]).getRequestStartTime();
                    mId = ((ChargeApprovalRequestDto) joinPoint.getArgs()[0]).getCustomerDto().getMid();
                    trdAmt = ((ChargeApprovalRequestDto) joinPoint.getArgs()[0]).getTrdAmt();
                    mTrdNo = ((ChargeApprovalRequestDto) joinPoint.getArgs()[0]).getMTrdNo();
                    break;
                case "/v1/wallet/use":
                    isTmsLoggable = true;
                    prdtCd = MpsPrdtCd.use.getPrdtCd();
                    requestIp = ((WalletUseRequestDto) joinPoint.getArgs()[0]).getRequestIp();
                    requestStartTime = ((WalletUseRequestDto) joinPoint.getArgs()[0]).getRequestStartTime();
                    mId = ((WalletUseRequestDto) joinPoint.getArgs()[0]).getCustomerDto().getMid();
                    trdAmt = ((WalletUseRequestDto) joinPoint.getArgs()[0]).getTrdAmt();
                    mTrdNo = ((WalletUseRequestDto) joinPoint.getArgs()[0]).getMTrdNo();
                    break;
                case "/v1/money/withdrawal":
                    isTmsLoggable = true;
                    prdtCd = MpsPrdtCd.withdrawal.getPrdtCd();
                    requestIp = ((WithdrawApprovalRequestDto) joinPoint.getArgs()[0]).getRequestIp();
                    requestStartTime = ((WithdrawApprovalRequestDto) joinPoint.getArgs()[0]).getRequestStartTime();
                    mId = ((WithdrawApprovalRequestDto) joinPoint.getArgs()[0]).getCustomerDto().getMid();
                    trdAmt = ((WithdrawApprovalRequestDto) joinPoint.getArgs()[0]).getTrdAmt();
                    mTrdNo = ((WithdrawApprovalRequestDto) joinPoint.getArgs()[0]).getMTrdNo();
                    break;
            }
        }
        //응답 "실패"인 경우, 최종 소요시간 계산용 종료시간
        String elapsedTimeStr = DateTimeUtil.getElapsedTimeStr(requestStartTime);

        Object result = joinPoint.proceed();
        //local 제외
        if (Arrays.stream(env.getActiveProfiles()).anyMatch(s -> s.toLowerCase().equals("local") || s.toLowerCase().equals("dev"))) {
            log.info("------------- 거래 종료 METHOD: [{}], CONTROLLER: [{}]", method.getName(), controllerNm);
//            return result;
        }

        //ResponseEntity 형태의 응답인지 확인
        if (result == null || !(result instanceof ResponseEntity)) {
            return result;
        }

        //StatusCode 200 인지 확인
        ResponseEntity<?> responseEntity = (ResponseEntity<?>) result;
        if (!responseEntity.getStatusCode().is2xxSuccessful()) {
            //실패인경우 찍기
            String rsltCd = String.valueOf(responseEntity.getBody().getClass().getField("rsltCd"));
            log.info("ResponseEntity 내 상태코드 200아님 => 상태코드[{}]", ((ResponseEntity<?>) result).getStatusCode());
            TmsAgent.sendTmsAgent(mId, prdtCd, requestIp, rsltCd, elapsedTimeStr, String.valueOf(trdAmt), mTrdNo);
            return result;
        }

        //BaseResponseDto 가 body에 담겨있는지 확인
        Object body = responseEntity.getBody();
        if (!(body instanceof BaseResponseDto)) {
            return result;
        }

        // BaseResonseDto 의 resultCode 가 성공인지 확인
        BaseResponseDto baseResponseDto = (BaseResponseDto) body;
        if (!baseResponseDto.getRsltCd().equals(ErrorCode.SUCCESS.getErrorCode())) {
            TmsAgent.sendTmsAgent(mId, prdtCd, requestIp, baseResponseDto.getRsltCd(), elapsedTimeStr, String.valueOf(trdAmt), mTrdNo);
            return result;
        }

        //BaseResponseDto 의 resultObj 검증
        Object rsltObj = baseResponseDto.getRsltObj();
//        if (ObjectUtils.isEmpty(rsltObj)) {
//            TmsAgent.sendTmsAgent(mId, prdtCd, requestIp, baseResponseDto.getRsltCd(), elapsedTimeStr, String.valueOf(trdAmt), mTrdNo);
//            throw new RequestValidationException(ErrorCode.SERVER_ERROR_CODE);
//        }

        //rsl
        if (rsltObj instanceof List) {
            if (((List) rsltObj).size() == 0) {
                log.info("------------------------- 거래 종료 METHOD: [{}], CONTROLLER: [{}]", method.getName(), controllerNm);
                return ResponseEntity.status(responseEntity.getStatusCode()).body(BaseResponseDto.builder()
                        .rsltCd(baseResponseDto.getRsltCd())
                        .rsltMsg(baseResponseDto.getRsltMsg())
                        .rsltObj(new ArrayList<>())
                        .build());
            }
        }

        Object rsltObj1 = rsltObj;
        if(!(rsltObj instanceof GetExpPntByMidResponseDto) && !(rsltObj instanceof AdminChargeApprovalResponseDto)){
            if (rsltObj instanceof GiftCardUseResponseDto || rsltObj instanceof GiftCardSearchResponseDto ||
                rsltObj instanceof GiftCardReissueResponseDto || rsltObj instanceof GiftCardBundleIssueResponseDto ||
                rsltObj instanceof GiftCardBundleTransferResponseDto || rsltObj instanceof GiftCardBundleUseResponseDto ||
                rsltObj instanceof GiftCardBundleInfoResponseDto || rsltObj instanceof GiftCardBundleBalanceResponseDto ||
                rsltObj instanceof GiftCardBundleListResponseDto || rsltObj instanceof GiftCardBundleChargeCancelResponseDto ||
                rsltObj instanceof GiftCardBundleChargeEtcResponseDto || rsltObj instanceof GiftCardBundleBalanceUseResponseDto ||
                rsltObj instanceof GiftCardBundleBalanceUseCancelResponseDto
            ) {
                //상품권 발행 말고 나머지 4개는 사용처 상점 아이디로 AES 암호화
                Field useMid = rsltObj.getClass().getDeclaredField("useMid");
                useMid.setAccessible(true);
                String useMidValue = (String) useMid.get(rsltObj);
                log.info("useMidValue: {}", useMidValue);
                MarketAddInfoDto marketAddInfo = commonservice.getMarketAddInfoByMId(useMidValue);
                rsltObj1 = dtoWithEncrypedField(rsltObj, marketAddInfo);
            }else{
                MarketAddInfoDto marketAddInfo = commonservice.getMarketAddInfoByCustNo(getCustNoFromDto(rsltObj));
                rsltObj1 = dtoWithEncrypedField(rsltObj, marketAddInfo);
            }
        }
        if (isTmsLoggable) {
            //응답 "성공"인 경우, 최종 소요시간 계산용 종료시간
            elapsedTimeStr = DateTimeUtil.getElapsedTimeStr(requestStartTime);
            //가맹점 아이디, 상품 코드, 요청 IP, 에러코드, 원천사 아이디(가맹점아이디로 통일), 소요시간, 거래금액, 상점거래번호
            TmsAgent.sendTmsAgent(mId, prdtCd, requestIp, ErrorCode.SUCCESS.getErrorCode(), elapsedTimeStr, String.valueOf(trdAmt), mTrdNo);
        }

        log.info("------------------------- 거래 종료 METHOD: [{}], CONTROLLER: [{}]", method.getName(), controllerNm);
        return ResponseEntity.status(responseEntity.getStatusCode()).body(BaseResponseDto.builder()
                .rsltCd(baseResponseDto.getRsltCd())
                .rsltMsg(baseResponseDto.getRsltMsg())
                .rsltObj(rsltObj1)
                .build());

    }

    /**
     * CommonResponseDto 내 resultObj 에 담긴 ~ResponseDto class 의 custNo 가져오기
     *
     * @param dto
     * @param <T>
     * @return
     * @throws IllegalAccessException
     */
    private static <T> String getCustNoFromDto(T dto) {
        String custNo = null;

        if (dto.getClass().getTypeName().equals(ArrayList.class.getTypeName())) {
            dto = (T) ((ArrayList) dto).get(0);
        }
        Field[] fields = dto.getClass().getDeclaredFields();
        for (Field field : fields) {
            if (field.getName().equals("custNo")) {
                field.setAccessible(true);
                try {
                    custNo = (String) field.get(dto);
                } catch (IllegalAccessException e) {
                    log.info("응답 DTO 내 CustNo 조회 실패! DTO : [{}]", dto.getClass().getSimpleName());
                    throw new RequestValidationException(ErrorCode.CUSTOMER_NOT_FOUND);
                }
                break;
            }
        }
        if (custNo == null) {
            log.info("응답 DTO 내 CustNo 조회 실패! DTO : [{}]", dto.getClass().getSimpleName());
            throw new RequestValidationException(ErrorCode.CUSTOMER_NOT_FOUND);
        }
        return custNo;
    }

    /**
     * BaseResponseDto 내 rsltObj 에 담긴 ~ResponseDto class 의 field 중 @encField 붙은 필드 암호화
     *
     * @param dto
     * @param marketAddInfo
     * @param <T>
     * @return
     */
    private static <T> T dtoWithEncrypedField(T dto, MarketAddInfoDto marketAddInfo) {
        if (dto instanceof List) {
            List<T> dtos = (List<T>) dto;
            for (int i = 0; i < dtos.size(); i++) {
                dtos.set(i, getT(dtos.get(i), marketAddInfo));
            }
        } else {
            dto = getT(dto, marketAddInfo);
        }
        return dto;
    }

    private static <T> T getT(T dto, MarketAddInfoDto marketAddInfo) {
        String encKey = marketAddInfo.getEncKey();
        String encMthd = marketAddInfo.getEncMthdCd();
        String encIv = marketAddInfo.getEncIv();
        String pktHashKey = marketAddInfo.getPktHashKey();

        if (StringUtils.isBlank(encKey) || StringUtils.isBlank(pktHashKey)) {
            log.info("응답 DTO 암호화 실패! ::암호화 키 확인 필요::MID[{}]", marketAddInfo.getMid());
            throw new RequestValidationException(ErrorCode.MARKET_ADD_INFO_NOT_FOUND);
        }

        try {
            Object builder = dto.getClass().getMethod("toBuilder").invoke(dto);
            Field[] fields = dto.getClass().getDeclaredFields();

            //응답 dto 에서 HashField 붙어있는 대상 추출
            List<Field> collect = Arrays.stream(fields).filter(field -> field.isAnnotationPresent(HashField.class)).collect(Collectors.toList());
            //HashField 값 담을 배열
            String[] pktHashArr = new String[collect.size()];
            //HashField 배열을 순서대로 나열해서 담을객체 (해당 객체를 sha256해서 응답 나감)
            StringBuffer pktHashBuffer = new StringBuffer();

            log.info("------------- 응답 파라미터 시작");
            for (Field field : fields) {
                //응답파라미터 로그 추가를 위해 field, value 상단에서 선언
                field.setAccessible(true);
                Object value = field.get(dto);
                if (value == null || ObjectUtils.isEmpty(value)) {
                    log.info("{} :[{}]", field.getName(), value);
                    continue;
                }
                log.info("{} :[{}]", field.getName(), value);

                //pktHash 만들기 위한 작업
                if (field.isAnnotationPresent(HashField.class)) {
                    HashField hashField = field.getAnnotation(HashField.class);
                    int order = hashField.order();

                    String valStr = value.toString();
                    if (order == 1) {
                        //선불상품권 사용, 사용취소, 재발행이 모두 아닌 경우에만 HashField(order=1) 인 "custNo" 뒤에 mid 붙힘
                        if (!(dto instanceof GiftCardUseResponseDto)
                                && !(dto instanceof GiftCardReissueResponseDto)
                                && !(dto instanceof GiftCardBundleIssueResponseDto)
                                && !(dto instanceof GiftCardBundleTransferResponseDto)
                                && !(dto instanceof GiftCardBundleChargeCancelResponseDto)
                                && !(dto instanceof GiftCardBundleUseResponseDto)
                                && !(dto instanceof GiftCardBundleChargeEtcResponseDto)
                                && !(dto instanceof GiftCardBundleBalanceUseResponseDto)
                                && !(dto instanceof GiftCardBundleBalanceUseCancelResponseDto))
                        {
                            valStr += marketAddInfo.getMid();
                        }
                    }

                    if (dto instanceof GiftCardUseResponseDto && order == 4) {
                        valStr = CipherUtil.decrypt(((GiftCardUseResponseDto) dto).getUseGcList(), encKey);
                    }

                    pktHashArr[order - 1] = valStr;
                }
                //암호화되어서 나가야하는 필드는 암호화
                if (field.isAnnotationPresent(EncField.class)) {
                    String encryptedValue = CipherUtil.encrypt(value.toString(), encKey);
                    builder.getClass().getMethod(field.getName(), String.class).invoke(builder, encryptedValue);
                }
            }
            //pktHash 값 생성 (일단 pktHashArr 이 있고, CommonLogicalResponseDto 상속받거나, 상품권 발행, 사용, 사용취소, 재발행인경우
            if (pktHashArr.length > 0 && (dto instanceof CommonLogicalResponseDto
                    || dto instanceof GiftCardIssueResponseDto || dto instanceof GiftCardReissueResponseDto
                    || dto instanceof GiftCardUseResponseDto || dto instanceof GiftCardBundleIssueResponseDto
                    || dto instanceof GiftCardBundleTransferResponseDto || dto instanceof GiftCardBundleUseResponseDto
                    || dto instanceof GiftCardBundleChargeCancelResponseDto || dto instanceof GiftCardBundleChargeEtcResponseDto
                    || dto instanceof GiftCardBundleBalanceUseResponseDto || dto instanceof GiftCardBundleBalanceUseCancelResponseDto
            )) {
                Arrays.stream(pktHashArr).forEach(s -> pktHashBuffer.append(s.toString()));

                //만들어진 pktHash 문자열 마지막에 pktHashKey 추가
                pktHashBuffer.append(pktHashKey);

                String pktHash = CipherSha256Util.digestSHA256(pktHashBuffer.toString());
                log.info("pktHash :[{}] [{}] [{}]", collect.stream().map(field -> field.getName()).collect(Collectors.joining(",")), pktHashBuffer, pktHash);

                builder.getClass().getMethod("pktHash", String.class).invoke(builder, pktHash);
            }
            log.info("------------- 응답 파라미터 종료");
            Method build = builder.getClass().getMethod("build");
            build.setAccessible(true);
            return (T) build.invoke(builder);
        } catch (Exception e) {
            log.info("DTO 암호화 실패! {}", e.getMessage());
            throw new RequestValidationException(ErrorCode.RESPONSE_DTO_ENCRYPTION_FAILED);
        }
    }
}
