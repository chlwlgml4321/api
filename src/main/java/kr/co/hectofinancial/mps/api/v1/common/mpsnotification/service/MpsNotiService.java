package kr.co.hectofinancial.mps.api.v1.common.mpsnotification.service;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;
import kr.co.hectofinancial.mps.api.v1.common.mpsnotification.domain.NotiInfo;
import kr.co.hectofinancial.mps.api.v1.common.mpsnotification.domain.NotiSend;
import kr.co.hectofinancial.mps.api.v1.common.mpsnotification.dto.RelayReqDto;
import kr.co.hectofinancial.mps.api.v1.common.mpsnotification.dto.RelayResDto;
import kr.co.hectofinancial.mps.api.v1.common.mpsnotification.repository.NotiInfoRepository;
import kr.co.hectofinancial.mps.api.v1.common.mpsnotification.repository.NotiSendRepository;
import kr.co.hectofinancial.mps.api.v1.common.service.SequenceService;
import kr.co.hectofinancial.mps.api.v1.notification.repository.SitePolicyMastRepository;
import kr.co.hectofinancial.mps.global.constant.MpsNotiSendStatCd;
import kr.co.hectofinancial.mps.global.error.ErrorCode;
import kr.co.hectofinancial.mps.global.mtms.MonitAgent;
import kr.co.hectofinancial.mps.global.util.HttpRequestUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;


@Service
@RequiredArgsConstructor
@Slf4j
public class MpsNotiService {

    /**
     * 릴레이 서버
     */

    @Value("${spring.profiles.active}")
    private String profiles;
    private final SitePolicyMastRepository sitePolicyMastRepository;
    private final HttpRequestUtil httpRequestUtil;
    private final NotiInfoRepository notiInfoRepository;
    private final SequenceService sequenceService;
    private final NotiSendRepository notiSendRepository;
    private final ObjectMapper om;

    @Transactional(rollbackFor = Exception.class)
    public RelayResDto handleSendNoti(String url, Object body) throws Exception {
        try{
            return handleSendNoti(new RelayReqDto(url, HttpMethod.POST, null, body));
        }catch (Exception e){
            log.error(e.getMessage(), e);
            throw e;
        }
    }

    /**
     * 노티 발송 요청
     *
     * @return
     * @throws Exception
     */
    @Transactional(rollbackFor = Exception.class)
    public RelayResDto handleSendNoti(RelayReqDto relayReqDto) throws Exception {

        log.info("Start Relay Notification.");

        try{
            if("local".equals(profiles) || "test".equals(profiles)) {
                profiles = "tb";
            }

            String relayUrl = sitePolicyMastRepository.selectSitePolicy(profiles + ".mps.relay.api.url") + "/thirty";
            om.setSerializationInclusion(JsonInclude.Include.NON_NULL);
            String requestBody = om.writeValueAsString(relayReqDto);
            ResponseEntity<RelayResDto> entity = httpRequestUtil.sendNotiRestRequest(relayUrl, requestBody, MediaType.APPLICATION_JSON_VALUE);
            log.info("(Relay Response) StatusCode: [{}], Body: [{}]", entity.getStatusCode(), entity.getBody().getStatus());
            if(!HttpStatus.OK.equals(entity.getStatusCode())) {
                log.info("Start Relay Notification.");
                throw new Exception("Relay Server Error. STATUS[" + entity.getStatusCode() + "]");
            }
            return entity.getBody();
        }catch (Exception e ){
            log.error(e.getMessage(), e);
            throw e;
        }
    }

    /**
     * 가맹점 노티 수신 URL 조회
     * @param mid
     * @param notiTypeCd
     * @return
     */
    @Transactional(rollbackFor = Exception.class)
    public NotiInfo getNotiUrl(String mid, String notiTypeCd) {
        NotiInfo notiInfo = notiInfoRepository.findByMidAndNotiTypeCdAndUseYnAndStDateAndEdDate(mid, notiTypeCd, "Y");
        return notiInfo;
    }

    /**
     * 리노티 요청 저장 (MPS.TB_MPS_NOTI_SEND)
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW, rollbackFor = {Exception.class})
    public void handleReSendNoti(String mpsCustNo, String mid, String params, NotiInfo notiInfo){

        log.info(">>>>> 리노티 요청 정보 => M_ID: [{}], NOTI_TYPE_CD: [{}], MPS_CUST_NO: [{}], URL: [{}]", mid, notiInfo.getNotiTypeCd(), mpsCustNo, notiInfo.getNotiUrl());

        try{
            //리노티 시퀀스 채번
            String seqNo = sequenceService.generateMpsNotiSendSeq01();
            log.info("시퀀스: [{}]", seqNo);
            NotiSend notiSend = NotiSend.builder()
                    .notiSendNo(seqNo)
                    .sendDate(LocalDateTime.now().plusMinutes(notiInfo.getSendCyc()))
                    .mid(mid)
                    .notiTypeCd(notiInfo.getNotiTypeCd())
                    .maxSendCnt(notiInfo.getMaxSendCnt())
                    .sendCyc(notiInfo.getSendCyc())
                    .notiUrl(notiInfo.getNotiUrl())
                    .sendStatCd(MpsNotiSendStatCd.FAIL.getSendStatCd())
                    .notiStopYn("N")
                    .acmSendCnt(0)
                    .notiInfo(params)
                    .build();
            notiSendRepository.save(notiSend);
        }catch (Exception e){
            String message = "(MPS_API) 리노티 데이터 저장 중 오류 발생! M_ID: " + mid + ", NOTI_TYPE_CD: " + notiInfo.getNotiTypeCd() + ", MPS_CUST_NO: " + mpsCustNo + ", URL: "+ notiInfo.getNotiUrl();
            log.error(message);
            e.printStackTrace();

            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append("(MPS_API) 리노티 데이터 저장 중 오류 발생")
                    .append("\n")
                    .append("======================")
                    .append("\n")
                    .append("선불회원번호: "+mpsCustNo)
                    .append("\n")
                    .append("M_ID: "+mid)
                    .append("\n")
                    .append("노티유형: " +notiInfo.getNotiTypeCd())
                    .append("\n")
                    .append("노티URL: " +notiInfo.getNotiUrl());
            MonitAgent.sendMonitAgent(ErrorCode.MPS_NOTI_FAIL.getErrorCode(), stringBuilder.toString());
        }
    }
}