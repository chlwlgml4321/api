package kr.co.hectofinancial.mps.api.v1.authentication.service;

import kr.co.hectofinancial.mps.api.v1.authentication.dto.ChkPinErrorCountResponseDto;
import kr.co.hectofinancial.mps.api.v1.common.mpsnotification.service.CustomerNotiService;
import kr.co.hectofinancial.mps.api.v1.common.service.CommonService;
import kr.co.hectofinancial.mps.api.v1.customer.domain.Customer;
import kr.co.hectofinancial.mps.api.v1.customer.repository.CustomerHistoryRepository;
import kr.co.hectofinancial.mps.api.v1.customer.repository.CustomerRepository;
import kr.co.hectofinancial.mps.api.v1.market.dto.MarketAddInfoDto;
import kr.co.hectofinancial.mps.api.v1.market.repository.MpsMarketRepository;
import kr.co.hectofinancial.mps.api.v1.authentication.dto.ChkPinErrorCountRequestDto;
import kr.co.hectofinancial.mps.global.constant.PinVerifyResult;
import kr.co.hectofinancial.mps.global.error.ErrorCode;
import kr.co.hectofinancial.mps.global.error.exception.RequestValidationException;
import kr.co.hectofinancial.mps.global.extern.whitelabel.dto.PinCheckRequestDto;
import kr.co.hectofinancial.mps.global.extern.whitelabel.dto.PinCheckResponseDto;
import kr.co.hectofinancial.mps.global.extern.whitelabel.socket.SocketClient;
import kr.co.hectofinancial.mps.global.extern.whitelabel.socket.SocketFieldBuilder;
import kr.co.hectofinancial.mps.global.mtms.MonitAgent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.ObjectUtils;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthenticationService {
    @Value("${spring.profiles.active}")
    private String profiles;
    private final CustomerRepository customerRepository;
    private final CustomerHistoryRepository customerHistoryRepository;
    private final CommonService commonService;
    private final MpsMarketRepository mpsMarketRepository;
    private final CustomerNotiService customerNotiService;

    /**
     * 사용 및 출금 시 핀번호 검증 (화이트라벨 연동)
     *
     * @param chkPinErrorCountRequestDto
     * @return
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public ChkPinErrorCountResponseDto isCorrectWhiteLabelPin(ChkPinErrorCountRequestDto chkPinErrorCountRequestDto) {
        log.info("###### CheckWhiteLabelPin [Start]");

        if (profiles.equals("local") || profiles.equals("dev")) {
            log.info("###### CheckWhiteLabelPin [End] => Profile not tb or prd");
            return new ChkPinErrorCountResponseDto(PinVerifyResult.SUCCESS);
        }
        String mpsCustNo = chkPinErrorCountRequestDto.getCustomerDto().getMpsCustNo();
        String mCustId = chkPinErrorCountRequestDto.getCustomerDto().getMCustId();
        String mid = chkPinErrorCountRequestDto.getCustomerDto().getMid();

        String pinVrifyTypeCd = chkPinErrorCountRequestDto.getMpsMarket().getPinVrifyTypeCd();

        String pin = chkPinErrorCountRequestDto.getPin();  //결제비밀번호 평문

        if (pin.length() != 6) {
            throw new RequestValidationException(ErrorCode.PARAM_INVALID, "결제 비밀번호");
        }

        PinCheckRequestDto pinCheckRequestDto = PinCheckRequestDto.builder()
                .mId(mid)
                .reqNo(chkPinErrorCountRequestDto.getTrdNo())
                .mreqNo(chkPinErrorCountRequestDto.getTrdNo())
                .custId("C".equalsIgnoreCase(pinVrifyTypeCd) ? mCustId : mpsCustNo) //핀검증 유형 코드 (C=> mCustId)
                .pmtPwdEnc(pin)
                .typeCd("2") // 처리구분 0: 유효성체크, 1: 등록, 2: 확인
                .build();

        log.info("###### CheckWhiteLabelPin req: {}", pinCheckRequestDto);
        MarketAddInfoDto marketAddInfoDto = commonService.getMarketAddInfoByMId(mid);
        String encKey = marketAddInfoDto.getEncKey();
        String pktHashKey = marketAddInfoDto.getPktHashKey();
        String res = "";

        PinCheckResponseDto pinCheckResponseDto = null;
        String outStatCd = "";
        String retCd = "";

        try {
            res = SocketClient.sendTcp(SocketFieldBuilder.getPaymentPinField(pinCheckRequestDto, encKey, pktHashKey));

            pinCheckResponseDto = PinCheckResponseDto.parse(res);
            outStatCd = pinCheckResponseDto.getOutStatCd();
            retCd = pinCheckResponseDto.getRetCd();

            if (outStatCd.equals("0021") && retCd.equals("0000")) {
                //성공
                log.info("###### CheckWhiteLabelPin [Success] => Res: {}", pinCheckResponseDto);
                return new ChkPinErrorCountResponseDto(PinVerifyResult.SUCCESS);
            } else if (outStatCd.equals("0031") && retCd.equals("ST67")) {
                //비밀번호 불일치, 오류 횟수 리턴
                if (!ObjectUtils.isEmpty(pinCheckResponseDto.getPinErrorCnt())) {
                    log.info("###### CheckWhiteLabelPin [Fail] => Res: {}", pinCheckResponseDto);
                    return new ChkPinErrorCountResponseDto(PinVerifyResult.FAIL, pinCheckResponseDto.getPinErrorCnt().longValue());
                }
                //비밀번호 오류횟수 획득 실패
                log.error("*** CheckWhiteLabelPin [Error][fail to get errorCnt] => Res: {}", pinCheckRequestDto, pinCheckResponseDto);
                return new ChkPinErrorCountResponseDto(PinVerifyResult.FAIL_CNT_ERROR);
            }
        } catch (Exception e) {
            log.error("*** CheckWhiteLabelPin [Error][connection error] => req: {} e: {}", pinCheckRequestDto, e.getMessage());
            return new ChkPinErrorCountResponseDto(PinVerifyResult.CONNECTION_ERROR);
        }

        if (outStatCd.equals("0031") && retCd.equals("ST68")) {
            //결제비밀번호 5회 불일치 => 계정 Lock + 이력쌓기
            log.info(">>>>> CheckWhiteLabelPin [Lock][start] => Res: {}", pinCheckResponseDto);
            try {
                customerHistoryRepository.insertMpsCustHistory(mpsCustNo);
                log.info(">>>>> CheckWhiteLabelPin [Lock][saved customer history]");
                customerNotiService.sendUpdateCustInfo(mid, mpsCustNo, encKey, pktHashKey);
            } catch (Exception e) {
                String simpleName = (e.getClass() != null) ? e.getClass().getSimpleName() : "insertMpsCustHistory";
                String cause = (e.getCause() != null) ? e.getCause().getMessage() : "알 수 없는 오류";
                MonitAgent.sendMonitAgent(ErrorCode.UNDEFINED_SERVER_ERROR_CODE.getErrorCode(), "CheckWhiteLabelPin 후 계정 잠긴 회원 이력 저장 실패! [" + simpleName + "] : " + cause);
            } finally {
                Customer customer = customerRepository.findCustomerByMpsCustNo(mpsCustNo).get();
                customer.lockCustomerStatCd();
                log.info(">>>>> CheckWhiteLabelPin [Lock][locked customer]");
            }
            return new ChkPinErrorCountResponseDto(PinVerifyResult.LOCKED);
        } else if (outStatCd.equals("0031") && retCd.equals("ST10")) {
            //화이트라벨서버에서 핀번호 복호화 오류 => 클라이언트측의 핀번호가 잘못된 암호화키로 암호화된 경우
            log.error("*** CheckWhiteLabelPin [Error][invalid encrypted pin] => res: {} ", pinCheckResponseDto);
            return new ChkPinErrorCountResponseDto(PinVerifyResult.INVALID_ENCRYPTED_PIN, PinVerifyResult.INVALID_ENCRYPTED_PIN.getResultMsg() + "(암호화키 불일치)");
        } else {
            //기타 결과코드 (확인필요)
            log.error("*** CheckWhiteLabelPin [Error][need to check Result code] => res: {} ", pinCheckResponseDto);
            String customStatCd = "outStatCd:" + (StringUtils.isNotBlank(outStatCd) ? outStatCd : "확인불가");
            String customResultCd = "retCd:" + (StringUtils.isNotBlank(retCd) ? retCd : "확인불가");
            return new ChkPinErrorCountResponseDto(PinVerifyResult.ERROR, (customStatCd + " " + customResultCd));
        }
    }
}

