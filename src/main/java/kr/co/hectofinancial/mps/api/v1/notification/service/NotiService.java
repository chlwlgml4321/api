package kr.co.hectofinancial.mps.api.v1.notification.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import kr.co.hectofinancial.mps.api.v1.common.service.SequenceService;
import kr.co.hectofinancial.mps.api.v1.cpn.domain.Cpn;
import kr.co.hectofinancial.mps.api.v1.cpn.service.CpnService;
import kr.co.hectofinancial.mps.api.v1.market.repository.MpsMarketChrgMapRepository;
import kr.co.hectofinancial.mps.api.v1.notification.domain.PyNtcSend;
import kr.co.hectofinancial.mps.api.v1.notification.dto.NotiLineworksDTO;
import kr.co.hectofinancial.mps.api.v1.notification.dto.PyNtcSendInsertRequestDto;
import kr.co.hectofinancial.mps.api.v1.notification.repository.PyNtcSendRepository;
import kr.co.hectofinancial.mps.api.v1.notification.repository.SitePolicyMastRepository;
import kr.co.hectofinancial.mps.global.config.ServerInfoConfig;
import kr.co.hectofinancial.mps.global.error.ErrorCode;
import kr.co.hectofinancial.mps.global.error.exception.RequestValidationException;
import kr.co.hectofinancial.mps.global.util.CommonUtil;
import kr.co.hectofinancial.mps.global.util.CustomDateTimeUtil;
import kr.co.hectofinancial.mps.global.util.DatabaseAESCryptoUtil;
import kr.co.hectofinancial.mps.global.util.TimeUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotiService {

    private final SitePolicyMastRepository sitePolicyMastRepository;
    private final SequenceService sequenceService;
    private final PyNtcSendRepository pyNtcSendRepository;
    private final CpnService cpnService;
    private final MpsMarketChrgMapRepository mpsMarketChrgMapRepository;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public int callLineworksNotification(String message) throws Exception {
        log.info("NotiService message: [{}]" , message);

        int resCode = 0;
//		try {
        String escapedContent = CommonUtil.escapeJSON(message);
        //max 2k
        int maxLength = 1024 * 3;
        if (escapedContent.length() >= maxLength) {
            escapedContent = escapedContent.substring(0, maxLength);
        }
        //2023.03.17 라인웍스 대괄호 이슈로 인해 소괄호로 치환작업
        escapedContent = escapedContent.replaceAll("\\]", ")").replaceAll("\\[", "(");

        String activeProfile = System.getProperty("spring.profiles.active");
        if (activeProfile.equals("test") || activeProfile.equals("local")) {
            activeProfile = "tb";
        }
        log.info("activeProfile : [{}]", activeProfile);

        String requestAppServiceSeq = sitePolicyMastRepository.selectSitePolicy(activeProfile + ".mps.notiGW.requestAppServiceSeq");
        String requestAuthToken = sitePolicyMastRepository.selectSitePolicy(activeProfile + ".mps.notiGW.requestAuthToken");
        String requestURL = sitePolicyMastRepository.selectSitePolicy(activeProfile + ".new.notiGW.requestSendLineworksURL");
        String targetId = sitePolicyMastRepository.selectSitePolicy(activeProfile + ".mps.notiGW.targetId");
        NotiLineworksDTO notiLineworksDTO = new NotiLineworksDTO();

        notiLineworksDTO.setContentType("application/json");
        notiLineworksDTO.setAuthToken(requestAuthToken);    //Bearer 4fcf648f367348938b50569d0c6ea7d7
        notiLineworksDTO.setAppServiceSeq(requestAppServiceSeq);    //NSS000000277
        notiLineworksDTO.setApiVersion("1.0");

        notiLineworksDTO.setTargetType("R");
        notiLineworksDTO.setTargetId(targetId);

        notiLineworksDTO.setContentType("T");
        notiLineworksDTO.setText(escapedContent);

        notiLineworksDTO.setOrdDay(TimeUtils.getyyyyMMdd());
        notiLineworksDTO.setOrdTime(TimeUtils.getHHmmss());
        notiLineworksDTO.setOrdNo(TimeUtils.getyyMMdd() + "_" + TimeUtils.getHHmmss() + "_" + CommonUtil.randomKey(6));

        log.info("notiLineworksDTO : [{}]", notiLineworksDTO);

//			log.info("NotiGW 서버 jsonReqParam(enc):" + DatabaseAESCryptoUtil.encryptMagicCrypto(jsonReqParam.toString()), "utf-8");

        //특정 가맹점이 라이센스키가 등록되지 않거나 유효하지 않은 Mid를 배치성으로 호출하고 있어서 다량의 알림 발송이 되는 것을 막음
        if (escapedContent.indexOf("license Key") > -1 || escapedContent.indexOf("상점아이디를 확인할 수 없습니다") > -1 || escapedContent.indexOf("해쉬 값 불일치") > -1 || escapedContent.indexOf("필수 파라미터 오류") > -1) {
            return resCode;
        }

        /* http 헤더 구성 */
        HttpHeaders http_headers = new HttpHeaders();
        MediaType mediaType = new MediaType("application", "json");
        http_headers.set("Authorization", requestAuthToken);
        http_headers.set("appServiceSeq", requestAppServiceSeq);
        http_headers.set("apiVersion", "1.0");
        http_headers.setContentType(mediaType);
        http_headers.setConnection("Close");

        /* 바디 구성 */
        Map<String, String> getResultMap = new HashMap<String, String>();
        getResultMap.put("authToken", requestAuthToken);
        getResultMap.put("appServiceSeq", requestAppServiceSeq);
        getResultMap.put("apiVersion", "1.0");
        getResultMap.put("contentType", "T");
        getResultMap.put("targetType", "R");
        getResultMap.put("targetId", targetId);
        getResultMap.put("text", escapedContent);
        getResultMap.put("ordDay", TimeUtils.getyyyyMMdd());
        getResultMap.put("ordTime", TimeUtils.getHHmmss());
        getResultMap.put("ordNo", TimeUtils.getyyMMdd() + "_" + TimeUtils.getHHmmss() + "_" + CommonUtil.randomKey(6));

        String apiUrl = requestURL;

        HttpEntity<Map<String, String>> http_entity_request = new HttpEntity<>(getResultMap, http_headers);
        RestTemplate restTemplate = CommonUtil.getRestTemplateInstance(30000, 30000);
        UriComponentsBuilder uri_builder = UriComponentsBuilder.fromHttpUrl(apiUrl);
        ResponseEntity<String> response_entity = restTemplate.exchange(uri_builder.toUriString(), HttpMethod.POST, http_entity_request, String.class);

        /* 실패시에 응답 값 세팅 */ //TODO
        String resp_body = response_entity.getBody();
        ObjectMapper objectMapper = new ObjectMapper();
        String json = objectMapper.writeValueAsString(http_entity_request);
        String req_data = json.substring(0, json.length()).replaceAll("[\\[\\]]", "");
        resp_body = resp_body.concat("@" + req_data);
        log.info("NotificationGW 라인웍스 발송 요청 resCode: [{}]", resp_body);
        return resCode;
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void savePyNtcSend(PyNtcSendInsertRequestDto pyNtcSendInsertRequestDto)throws Exception{

        CustomDateTimeUtil customDateTimeUtil = new CustomDateTimeUtil();
        String curDt = customDateTimeUtil.getDate();

        String notiTrdNo = sequenceService.generatePyNtcSendSeq01();

        String msgParam = null;
        String cpnId;
        String cpnNm;
        Cpn tgtCpn;
        String decEmail = null;

        if(pyNtcSendInsertRequestDto.getMsgTmplId().equals("MPS_TRD_01")){ //충전

            /* 충전수단 조회 */
            String chrgMeanNm = mpsMarketChrgMapRepository.findByMidAndChrgMeanCd(pyNtcSendInsertRequestDto.getChrgMeanCd());
            if(chrgMeanNm == null){
                throw new RequestValidationException(ErrorCode.TRADE_CHRG_MEAN_CD_ERROR);
            }

            /* 상점정보 SET */
            cpnId = cpnService.getCpnId(pyNtcSendInsertRequestDto.getMid());
            tgtCpn = cpnService.getCpn(cpnId);
            cpnNm = tgtCpn.getCpnNm();

            msgParam = String.join("|",
                    replaceNull(cpnNm),
                    replaceNull(cpnNm),
                    replaceNull(chrgMeanNm),
                    replaceNull(pyNtcSendInsertRequestDto.getCustNm()),
                    replaceNull(pyNtcSendInsertRequestDto.getTrdNo()),
                    pyNtcSendInsertRequestDto.getTrdDtm(),
                    replaceNull(String.valueOf(pyNtcSendInsertRequestDto.getAmt())),
                    replaceNull(cpnNm),
                    replaceNull(tgtCpn.getCpnUrl()),
                    replaceNull(tgtCpn.getCpnUrl()),
                    replaceNull(tgtCpn.getTelNo())
                    );

        }else if(pyNtcSendInsertRequestDto.getMsgTmplId().equals("MPS_TRD_02")){ //사용

            /* 상점정보 SET */
            String marketParm;
            cpnId = cpnService.getCpnId(pyNtcSendInsertRequestDto.getStlMid());
            tgtCpn = cpnService.getCpn(cpnId);
            cpnNm = tgtCpn.getCpnNm();

            if(pyNtcSendInsertRequestDto.getStorNm().equals("N")){
                marketParm = cpnNm;
            }else{
                marketParm = cpnNm + "(" + pyNtcSendInsertRequestDto.getStorNm() + ")";
            }

            msgParam = String.join("|",
                    replaceNull(marketParm),
                    replaceNull(marketParm),
                    replaceNull(pyNtcSendInsertRequestDto.getCustNm()),
                    replaceNull(pyNtcSendInsertRequestDto.getTrdNo()),
                    pyNtcSendInsertRequestDto.getTrdDtm(),
                    replaceNull(String.valueOf(pyNtcSendInsertRequestDto.getMnyAmt())),
                    replaceNull(String.valueOf(pyNtcSendInsertRequestDto.getPntAmt())),
                    replaceNull(String.valueOf(pyNtcSendInsertRequestDto.getAmt())),
                    replaceNull(marketParm),
                    replaceNull(tgtCpn.getCpnUrl()),
                    replaceNull(tgtCpn.getCpnUrl()),
                    replaceNull(tgtCpn.getTelNo())
            );
        }else if(pyNtcSendInsertRequestDto.getMsgTmplId().equals("MPS_TRD_03")){ //신규 충전 템플릿

            /* 충전수단 조회 */
            String chrgMeanNm = mpsMarketChrgMapRepository.findByMidAndChrgMeanCd(pyNtcSendInsertRequestDto.getChrgMeanCd());
            if(chrgMeanNm == null){
                throw new RequestValidationException(ErrorCode.TRADE_CHRG_MEAN_CD_ERROR);
            }

            /* 상점정보 SET */
            cpnId = cpnService.getCpnId(pyNtcSendInsertRequestDto.getMid());
            tgtCpn = cpnService.getCpn(cpnId);
            cpnNm = tgtCpn.getCpnNm();

            msgParam = String.join("|",
                    replaceNull(pyNtcSendInsertRequestDto.getCustNm()),
                    replaceNull(cpnNm),
                    replaceNull(pyNtcSendInsertRequestDto.getCustNm()),
                    replaceNull(cpnNm),
                    replaceNull(String.valueOf(pyNtcSendInsertRequestDto.getAmt())),
                    replaceNull(cpnNm),
                    replaceNull(chrgMeanNm),
                    pyNtcSendInsertRequestDto.getTrdDtm(),
                    replaceNull(pyNtcSendInsertRequestDto.getTrdNo())
            );

        }else if(pyNtcSendInsertRequestDto.getMsgTmplId().equals("MPS_TRD_04")){ //신규 사용 템플릿

            /* 상점정보 SET */
            cpnId = cpnService.getCpnId(pyNtcSendInsertRequestDto.getStlMid());
            tgtCpn = cpnService.getCpn(cpnId);
            cpnNm = tgtCpn.getCpnNm();

            if(pyNtcSendInsertRequestDto.getStorNm().equals("N")){

                pyNtcSendInsertRequestDto.setMsgTmplId("MPS_TRD_05");
                msgParam = String.join("|",
                        replaceNull(pyNtcSendInsertRequestDto.getCustNm()),
                        replaceNull(cpnNm),
                        replaceNull(pyNtcSendInsertRequestDto.getCustNm()),
                        replaceNull(cpnNm),
                        replaceNull(String.valueOf(pyNtcSendInsertRequestDto.getAmt())),
                        replaceNull(String.valueOf(pyNtcSendInsertRequestDto.getMnyAmt())),
                        replaceNull(String.valueOf(pyNtcSendInsertRequestDto.getPntAmt())),
                        pyNtcSendInsertRequestDto.getTrdDtm(),
                        replaceNull(pyNtcSendInsertRequestDto.getTrdNo())
                );
            }else{
                msgParam = String.join("|",
                        replaceNull(pyNtcSendInsertRequestDto.getCustNm()),
                        replaceNull(cpnNm),
                        replaceNull(pyNtcSendInsertRequestDto.getCustNm()),
                        replaceNull(cpnNm),
                        replaceNull(String.valueOf(pyNtcSendInsertRequestDto.getAmt())),
                        replaceNull(String.valueOf(pyNtcSendInsertRequestDto.getMnyAmt())),
                        replaceNull(String.valueOf(pyNtcSendInsertRequestDto.getPntAmt())),
                        replaceNull(pyNtcSendInsertRequestDto.getStorNm()),
                        pyNtcSendInsertRequestDto.getTrdDtm(),
                        replaceNull(pyNtcSendInsertRequestDto.getTrdNo())
                );
            }
        }

        /* 이메일 복호화 */
        try {
            DatabaseAESCryptoUtil databaseAESCryptoUtil = new DatabaseAESCryptoUtil();
            decEmail = databaseAESCryptoUtil.convertToEntityAttribute(pyNtcSendInsertRequestDto.getEmail());
        } catch (Exception e) {
            throw new RequestValidationException(ErrorCode.DECRYPT_ERROR);
        }

        PyNtcSend pyNtcSend = PyNtcSend.builder()
                .ntcSendNo(notiTrdNo)
                .regDt(curDt)
                .reqAlmTypeCd("MAIL")
                .rcvIdntDivCd("M")
                .rcvIdnt(decEmail)
                .procStatCd("W")
                .msgTmplId(pyNtcSendInsertRequestDto.getMsgTmplId())
                .msgTmplPrmtr(msgParam)
                .createdDate(LocalDateTime.now().plusMinutes(2))
                .createdIp(ServerInfoConfig.HOST_IP)
                .createdId(ServerInfoConfig.HOST_NAME)
                .build();
        pyNtcSendRepository.save(pyNtcSend);
    }

    public String replaceNull(String param){
        return (param == null) ? "|" : param;
    }
}
