package kr.co.hectofinancial.mps.api.v1.common.mpsnotification.service;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;
import kr.co.hectofinancial.mps.api.v1.common.mpsnotification.domain.NotiInfo;
import kr.co.hectofinancial.mps.api.v1.common.mpsnotification.dto.NotiCustDto;
import kr.co.hectofinancial.mps.api.v1.common.mpsnotification.dto.RelayResDto;
import kr.co.hectofinancial.mps.api.v1.customer.domain.Customer;
import kr.co.hectofinancial.mps.api.v1.customer.repository.CustomerRepository;
import kr.co.hectofinancial.mps.global.constant.BizDivCd;
import kr.co.hectofinancial.mps.global.constant.MpsNotiTypeCd;
import kr.co.hectofinancial.mps.global.error.ErrorCode;
import kr.co.hectofinancial.mps.global.error.exception.RequestValidationException;
import kr.co.hectofinancial.mps.global.mtms.MonitAgent;
import kr.co.hectofinancial.mps.global.util.CipherSha256Util;
import kr.co.hectofinancial.mps.global.util.CipherUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;


@Service
@RequiredArgsConstructor
@Slf4j
public class CustomerNotiService {

    private final MpsNotiService mpsNotiService;
    private final CustomerRepository customerRepository;
    protected final ObjectMapper om;


    /**
     * 회원정보 수정(회원잠금) 노티 발송
     *
     * @param mpsCustNo
     * @param encKey
     */
    public void sendUpdateCustInfo(String mid, String mpsCustNo, String encKey, String pktHashKey) {

        log.info(">>>>> (회원정보 업데이트 노티 START) M_ID: [{}], 선불회원번호: [{}]", mid, mpsCustNo);
        NotiInfo tgtNotiInfo = mpsNotiService.getNotiUrl(mid, MpsNotiTypeCd.CUSTOMER_INFO_UPDATE.getNotiTypeCd());

        if (tgtNotiInfo == null){
            log.info(">>>>> (회원정보 업데이트) 노티 받지 않는 가맹점 : [{}], 선불회원번호: [{}]", mid, mpsCustNo);
            return;
        }

        log.info("(회원정보 업데이트) 노티 URL: " + tgtNotiInfo.getNotiUrl());

        NotiCustDto notiCustDto = new NotiCustDto();
        String body;

        try {
            Customer tgtCustomer = customerRepository.findCustomerByMpsCustNoAndMid(mpsCustNo, mid)
                    .orElseThrow(() -> new RequestValidationException(ErrorCode.CUSTOMER_NOT_FOUND));
            if(tgtCustomer == null) {
                log.error("회원정보조회 오류!! MPS_CUST_NO: [{}]", mpsCustNo);
                return;
            }

            String statCd = tgtCustomer.getStatCd();
            String bizDivCd = tgtCustomer.getBizDivCd();
            String mCustId = tgtCustomer.getMCustId();
            LocalDateTime updateDt = tgtCustomer.getModifiedDate();
            String formattedDt = updateDt.format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));

            notiCustDto.setCustNo(CipherUtil.encrypt(mpsCustNo, encKey));
            notiCustDto.setCustStatCd(CipherUtil.encrypt(statCd, encKey));
            notiCustDto.setRegDate(CipherUtil.encrypt(formattedDt, encKey));
            notiCustDto.setMCustId(CipherUtil.encrypt(mCustId, encKey));
            if(!BizDivCd.INDIVIDUAL.getBizDivCd().equals(bizDivCd)) {
                notiCustDto.setBizRegNo(CipherUtil.encrypt(tgtCustomer.getBizRegNo(), encKey));
            }

            notiCustDto.setPktHash(CipherSha256Util.digestSHA256(mpsCustNo + mCustId + statCd + formattedDt + pktHashKey));

            om.setSerializationInclusion(JsonInclude.Include.NON_NULL);
            body = om.writeValueAsString(notiCustDto);
        }
        catch(Exception e) {
            e.printStackTrace();
            String message = "(MPS_API) 회원 잠금 노티 발송데이터 설정 중 오류 발생! M_ID: " + mid + ", 선불회원번호: " + mpsCustNo;
            MonitAgent.sendMonitAgent(ErrorCode.MPS_NOTI_FAIL.getErrorCode(), message);
            log.error(message);
            return;
        }

        log.info("Send update customer info. " + body);

        try {
            RelayResDto relayResDto = mpsNotiService.handleSendNoti(tgtNotiInfo.getNotiUrl(), notiCustDto);
            String status = relayResDto.getStatus();
            log.info(">>>>> 가맹점 노티 응답 STATUS : [{}]", status);
            String result = String.valueOf(relayResDto.getBody());
            log.info(">>>>> 가맹점 노티 응답 RESULT : [{}]", result);

            if (!"OK".equals(result) && !"\"OK\"".equals(result)) {
                log.info("response external notificaiton body=[{}]", relayResDto.getBody());
                throw new RuntimeException("response external notificaiton body=" + relayResDto.getBody());
            }
        }
        catch(Exception e) {
            e.printStackTrace();
            String message = "(MPS_API) 회원 잠금 노티 발송 실패! M_ID: " + mid + ", 선불회원번호: " + mpsCustNo;
            log.error(message);
            mpsNotiService.handleReSendNoti(mpsCustNo, mid, body, tgtNotiInfo); //1차노티 실패, 리노티 등록
        }
    }

}