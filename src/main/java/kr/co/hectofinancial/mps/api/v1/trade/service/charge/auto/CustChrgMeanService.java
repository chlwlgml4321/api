package kr.co.hectofinancial.mps.api.v1.trade.service.charge.auto;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import kr.co.hectofinancial.mps.global.constant.AutoChargeType;
import kr.co.hectofinancial.mps.api.v1.trade.dto.charge.auto.CustAutoChargeInfo;
import kr.co.hectofinancial.mps.api.v1.trade.dto.charge.auto.CustAutoChargeMethod;
import kr.co.hectofinancial.mps.api.v1.trade.dto.charge.auto.CustChrgMeanDto;
import kr.co.hectofinancial.mps.api.v1.customer.domain.CustChrgMean;
import kr.co.hectofinancial.mps.api.v1.customer.repository.CustChrgMeanRepository;
import kr.co.hectofinancial.mps.global.constant.TrdChrgMeanCd;
import kr.co.hectofinancial.mps.global.error.ErrorCode;
import kr.co.hectofinancial.mps.global.error.exception.RequestValidationException;
import kr.co.hectofinancial.mps.global.util.CommonUtil;
import kr.co.hectofinancial.mps.global.util.DatabaseAESCryptoUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Service
@Slf4j
@RequiredArgsConstructor
public class CustChrgMeanService {
    private final CustChrgMeanRepository custChrgMeanRepository;
    private final ObjectMapper om;
    private final DatabaseAESCryptoUtil cryptoUtil = new DatabaseAESCryptoUtil();

    /**
     * @param custNo
     * @return
     */
    public CustChrgMeanDto getCustChrgMeanForAutoChrg(String custNo) {
        String autoChrgMeanCd = TrdChrgMeanCd.WHITELABEL.getChrgMeanCd();   //충전 수단 코드 WL 고정
        Optional<CustChrgMean> custChrgMeanOptional = custChrgMeanRepository.findByMpsCustNoAndChrgMeanCd(custNo, autoChrgMeanCd);

        CustChrgMeanDto autoChrgInfo = new CustChrgMeanDto();
        autoChrgInfo.setMpsCustNo(custNo);
        autoChrgInfo.setAutoChargeUseYn("N");
        autoChrgInfo.setRegularChargeUseYn("N");

        if (!custChrgMeanOptional.isPresent()) {
          return autoChrgInfo;
        }

        CustChrgMean custChrgMean = custChrgMeanOptional.get();

        String autoChrgYn = custChrgMean.getAutoChrgYn();       //자동충전 사용여부
        String rglChrgUseYn = custChrgMean.getRglChrgUseYn();   //정기충전 사용여부
        String acntJson = custChrgMean.getAutoChrgAcnt();       //계좌정보
        String autoChrgJson = custChrgMean.getAutoChrgInfo();   //자동충전정보
        String rglChrgJson = custChrgMean.getRglChrgInfo();     //정기충전정보

        if (autoChrgYn != null && StringUtils.isNotBlank(autoChrgYn)) {
            autoChrgInfo.setAutoChargeUseYn(autoChrgYn.toUpperCase());
        }

        if (rglChrgUseYn != null && StringUtils.isNotBlank(rglChrgUseYn)) {
            autoChrgInfo.setRegularChargeUseYn(rglChrgUseYn.toUpperCase());
        }

        if (StringUtils.isNotBlank(CommonUtil.nullTrim(acntJson))) {
            //계좌정보 세팅
            List<CustAutoChargeMethod> methods = parseList(acntJson, new TypeReference<List<CustAutoChargeMethod>>() {
            }, Collections.emptyList());
            methods.stream().forEach(m -> {
                m.setLinkKey(cryptoUtil.convertToEntityAttribute(m.getLinkKey()));
                m.setPmtType(cryptoUtil.convertToEntityAttribute(m.getPmtType()));
                m.setPmtCode(cryptoUtil.convertToEntityAttribute(m.getPmtCode()));
                m.setPmtName(cryptoUtil.convertToEntityAttribute(m.getPmtName()));
                m.setPmtNoSuffix(cryptoUtil.convertToEntityAttribute(m.getPmtNoSuffix()));
                m.setPmtKey(cryptoUtil.convertToEntityAttribute(m.getPmtKey()));
                m.setPriority(cryptoUtil.convertToEntityAttribute(m.getPriority()));
            });
            autoChrgInfo.setAutoChargeMethods(methods);
        }

        if (StringUtils.isNotBlank(CommonUtil.nullTrim(autoChrgJson))) {
            //자동충전정보 세팅
            List<CustAutoChargeInfo> custAutoChargeInfoList = parseList(autoChrgJson, new TypeReference<List<CustAutoChargeInfo>>() {
            }, Collections.emptyList());
            custAutoChargeInfoList.stream().forEach(f -> {
                f.setLinkKey(cryptoUtil.convertToEntityAttribute(f.getLinkKey()));
                f.setType(cryptoUtil.convertToEntityAttribute(f.getType()));
                f.setUseYn(cryptoUtil.convertToEntityAttribute(f.getUseYn()));
                if (f.getTypeAsEnum() == AutoChargeType.THRESHOLD && !f.getValue().isEmpty())
                    f.setValue(cryptoUtil.convertToEntityAttribute(f.getValue()));
                if (f.getTypeAsEnum() == AutoChargeType.THRESHOLD && !f.getChgAmt().isEmpty())
                    f.setChgAmt(cryptoUtil.convertToEntityAttribute(f.getChgAmt()));
            });
            autoChrgInfo.setAutoChargeInfos(custAutoChargeInfoList);
        }

        if (StringUtils.isNotBlank(CommonUtil.nullTrim(rglChrgJson))) {
            //정기충전정보 세팅
            List<CustAutoChargeInfo> custAutoChargeInfoList = parseList(rglChrgJson, new TypeReference<List<CustAutoChargeInfo>>() {
            }, Collections.emptyList());
            custAutoChargeInfoList.stream().forEach(f -> {
                f.setLinkKey(cryptoUtil.convertToEntityAttribute(f.getLinkKey()));
                f.setType(cryptoUtil.convertToEntityAttribute(f.getType()));
                f.setUseYn(cryptoUtil.convertToEntityAttribute(f.getUseYn()));
                f.setValue(cryptoUtil.convertToEntityAttribute(f.getValue()));
                f.setChgAmt(cryptoUtil.convertToEntityAttribute(f.getChgAmt()));
            });
            autoChrgInfo.setRegularChargeInfos(custAutoChargeInfoList);
        }
        return autoChrgInfo;
    }


    private <T> T parseList(String json, TypeReference<T> type, T defaultValue) {
        try {
            return om.readValue(json, type);
        } catch (JsonProcessingException e) {
            log.error("fail to parse json={} to type={}", json, type);
            return defaultValue;
        }
    }
}
