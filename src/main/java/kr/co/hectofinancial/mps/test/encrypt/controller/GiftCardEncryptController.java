package kr.co.hectofinancial.mps.test.encrypt.controller;

import com.fasterxml.jackson.core.type.TypeReference;
import kr.co.hectofinancial.mps.api.v1.common.service.CommonService;
import kr.co.hectofinancial.mps.api.v1.customer.domain.Customer;
import kr.co.hectofinancial.mps.api.v1.customer.repository.CustomerRepository;
import kr.co.hectofinancial.mps.api.v1.giftcard.single.dto.GiftCardHistoryInfo;
import kr.co.hectofinancial.mps.api.v1.giftcard.single.dto.GiftCardIssueInfo;
import kr.co.hectofinancial.mps.api.v1.giftcard.single.dto.GiftCardSearch;
import kr.co.hectofinancial.mps.api.v1.market.dto.MarketAddInfoDto;
import kr.co.hectofinancial.mps.global.util.CipherSha256Util;
import kr.co.hectofinancial.mps.global.util.CipherUtil;
import kr.co.hectofinancial.mps.global.util.JsonUtil;
import kr.co.hectofinancial.mps.test.encrypt.dto.EncryptMapRequestDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.context.annotation.Profile;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.*;

@Profile({"test", "local"})//TB환경에서만 적용
@RequiredArgsConstructor
@RestController
@Slf4j
public class GiftCardEncryptController {

    private final CommonService commonService;
    private final CustomerRepository customerRepository;
    private final JsonUtil jsonUtil;

    @PostMapping("/test/giftcard/encrypt")
    public ResponseEntity<Map<String, Object>> encrypt(@RequestBody EncryptMapRequestDto dto) throws Exception {
        Map<String, Object> resMap = new HashMap<>();

        MarketAddInfoDto marketAddInfoByMId = commonService.getMarketAddInfoByMId(dto.getM_id());
        String encKey = marketAddInfoByMId.getEncKey();
        String pktHashKey = marketAddInfoByMId.getPktHashKey();

        Set<String> keys = dto.getBody().keySet();

        Map<String, String> hashList = new HashMap<>();
        for (String key : keys) {
            switch (dto.getUri()) {
                case "/v1/giftcard/issue":
                    if ("custNo".equals(key)) {
                        String custNo = String.valueOf(dto.getBody().get(key));
                        hashList.put(key, custNo);

                        Optional<Customer> customerDto = customerRepository.findCustomerByMpsCustNo(custNo);
                        customerDto.ifPresent(customer -> hashList.put("mId", customer.getMid()));

                        resMap.put(key, custNo);
                    } else if ("mTrdNo".equals(key)) {
                        hashList.put(key, String.valueOf(dto.getBody().get(key)));
                        resMap.put(key, String.valueOf(dto.getBody().get(key)));
                    } else if ("useMid".equals(key)) {
                        hashList.put(key, String.valueOf(dto.getBody().get(key)));
                        resMap.put(key, String.valueOf(dto.getBody().get(key)));
                    } else if ("trdAmt".equals(key)) {
                        hashList.put(key, String.valueOf(dto.getBody().get(key)));
                        resMap.put(key, CipherUtil.encrypt(String.valueOf(dto.getBody().get(key)), encKey));
                        hashList.put("hashKey", pktHashKey);
                    } else if ("mnyBlc".equals(key)) {
                        resMap.put(key, CipherUtil.encrypt(String.valueOf(dto.getBody().get(key)), encKey));
                    } else if ("pntBlc".equals(key)) {
                        resMap.put(key, CipherUtil.encrypt(String.valueOf(dto.getBody().get(key)), encKey));
                    } else if ("pinNo".equals(key)) {
                        resMap.put(key, CipherUtil.encrypt(String.valueOf(dto.getBody().get(key)), encKey));
                    } else if ("totCnt".equals(key)) {
                        resMap.put(key, CipherUtil.encrypt(String.valueOf(dto.getBody().get(key)), encKey));
                    } else if ("issList".equals(key)) {
                        List<GiftCardIssueInfo> issList = jsonUtil.fromJsonArray(jsonUtil.toJson(dto.getBody().get(key)), new TypeReference<List<GiftCardIssueInfo>>() {});
                        resMap.put(key, issList);
                    } else {
                        resMap.put(key, String.valueOf(dto.getBody().get(key)));
                    }
                    break;
                case "/v1/giftcard/use":
                    if ("mTrdNo".equals(key)) {
                        hashList.put(key, String.valueOf(dto.getBody().get(key)));
                        resMap.put(key, String.valueOf(dto.getBody().get(key)));
                    } else if ("useMid".equals(key)) {
                        hashList.put(key, String.valueOf(dto.getBody().get(key)));
                        resMap.put(key, String.valueOf(dto.getBody().get(key)));
                    } else if ("useGcList".equals(key)) {
                        List<String> useGcList = (List<String>) dto.getBody().get(key);
                        String jsonVal = jsonUtil.toJson(useGcList);
                        resMap.put(key, CipherUtil.encrypt(jsonVal, encKey));

                        hashList.put(key, jsonVal);
                        hashList.put("hashKey", pktHashKey);
                    } else {
                        resMap.put(key, String.valueOf(dto.getBody().get(key)));
                    }
                    break;
                case "/v1/giftcard/search":
                    if ("gcInfo".equals(key)) {
                        resMap.put(key, CipherUtil.encrypt(String.valueOf(dto.getBody().get(key)), encKey));
                    } else {
                        resMap.put(key, String.valueOf(dto.getBody().get(key)));
                    }
                    break;
                case "/v1/giftcard/reissue":
                    if ("useMid".equals(key)) {
                        hashList.put(key, String.valueOf(dto.getBody().get(key)));
                        resMap.put(key, String.valueOf(dto.getBody().get(key)));
                    } else if ("gcNo".equals(key)) {
                        hashList.put(key, String.valueOf(dto.getBody().get(key)));
                        resMap.put(key, CipherUtil.encrypt(String.valueOf(dto.getBody().get(key)), encKey));
                    } else if ("gcAmt".equals(key)) {
                        hashList.put(key, String.valueOf(dto.getBody().get(key)));
                        hashList.put("hashKey", pktHashKey);
                        resMap.put(key, CipherUtil.encrypt(String.valueOf(dto.getBody().get(key)), encKey));
                    } else {
                        resMap.put(key, String.valueOf(dto.getBody().get(key)));
                    }
                    break;
            }
        }

        String pktHash = "";
        if (!hashList.isEmpty()) {
            switch (dto.getUri()) {
                case "/v1/giftcard/issue":
                    pktHash = hashList.get("custNo") +
                            hashList.get("mId") +
                            hashList.get("mTrdNo") +
                            hashList.get("useMid") +
                            hashList.get("trdAmt") +
                            hashList.get("hashKey");
                    resMap.put("pktHash", CipherSha256Util.digestSHA256(pktHash));
                    break;
                case "/v1/giftcard/use":
                    pktHash = hashList.get("mTrdNo") +
                            hashList.get("useMid") +
                            hashList.get("useGcList") +
                            hashList.get("hashKey");
                    resMap.put("pktHash", CipherSha256Util.digestSHA256(pktHash));
                    break;
                case "/v1/giftcard/reissue":
                    pktHash = hashList.get("useMid") +
                            hashList.get("gcNo") +
                            hashList.get("gcAmt") +
                            hashList.get("hashKey");
                    resMap.put("pktHash", CipherSha256Util.digestSHA256(pktHash));
                    break;
            }
        }

        return ResponseEntity.ok(resMap);
    }

    @PostMapping("/test/giftcard/decrypt")
    public ResponseEntity<Map<String, Object>> decrypt(@RequestBody EncryptMapRequestDto dto) throws Exception {
        Map<String, Object> resMap = new HashMap<>();

        MarketAddInfoDto marketAddInfoByMId = commonService.getMarketAddInfoByMId(dto.getM_id());

        String encKey = marketAddInfoByMId.getEncKey();
        String pktHashKey = marketAddInfoByMId.getPktHashKey();

        Set<String> keys = dto.getBody().keySet();

        Map<String, String> hashList = new HashMap<>();
        for (String key : keys) {
            switch (dto.getUri()) {
                case "/v1/giftcard/issue":
                    if ("gcList".equals(key)) {
                        List<GiftCardIssueInfo> gcList = jsonUtil.fromJsonArray(jsonUtil.toJson(dto.getBody().get(key)), new TypeReference<List<GiftCardIssueInfo>>() {});

                        List<Map<String, Object>> resGcList = new ArrayList<>();
                        for (GiftCardIssueInfo gc : gcList) {
                            Map<String, Object> res = new HashMap<>();
                            res.put("gcAmt", gc.getGcAmt());
                            res.put("gcQty", gc.getGcQty());
                            res.put("gcNoList", jsonUtil.fromJsonArray(CipherUtil.decrypt(gc.getGcNoList(), encKey), new TypeReference<List<String>>() {}));
                            resGcList.add(res);
                        }

                        resMap.put("gcList", resGcList);
                    } else if ("custNo".equals(key)) {
                        String custNo = String.valueOf(dto.getBody().get(key));
                        hashList.put(key, custNo);

                        Optional<Customer> customerDto = customerRepository.findCustomerByMpsCustNo(custNo);
                        customerDto.ifPresent(customer -> hashList.put("mId", customer.getMid()));

                        resMap.put(key, custNo);
                    } else if ("mTrdNo".equals(key)) {
                        hashList.put(key, String.valueOf(dto.getBody().get(key)));
                        resMap.put(key, String.valueOf(dto.getBody().get(key)));
                    } else if ("useMid".equals(key)) {
                        hashList.put(key, String.valueOf(dto.getBody().get(key)));
                        resMap.put(key, String.valueOf(dto.getBody().get(key)));
                    } else if ("trdAmt".equals(key)) {
                        hashList.put(key, String.valueOf(dto.getBody().get(key)));
                        resMap.put(key, String.valueOf(dto.getBody().get(key)));
                    } else if ("trdNo".equals(key)) {
                        hashList.put(key, String.valueOf(dto.getBody().get(key)));
                        resMap.put(key, String.valueOf(dto.getBody().get(key)));
                        hashList.put("hashKey", pktHashKey);
                    } else {
                        resMap.put(key, String.valueOf(dto.getBody().get(key)));
                    }
                    break;
                case "/v1/giftcard/use":
                    if ("mTrdNo".equals(key)) {
                        hashList.put(key, String.valueOf(dto.getBody().get(key)));
                        resMap.put(key, String.valueOf(dto.getBody().get(key)));
                    }  else if ("gcTrdNo".equals(key)) {
                        hashList.put(key, String.valueOf(dto.getBody().get(key)));
                        resMap.put(key, String.valueOf(dto.getBody().get(key)));
                    } else if ("useMid".equals(key)) {
                        hashList.put(key, String.valueOf(dto.getBody().get(key)));
                        resMap.put(key, String.valueOf(dto.getBody().get(key)));
                    } else if ("useGcList".equals(key)) {
                        String strUseGcList = String.valueOf(dto.getBody().get(key));
                        String decUseGcList = CipherUtil.decrypt(strUseGcList, encKey);
                        resMap.put(key, decUseGcList);

                        hashList.put(key, decUseGcList);
                        hashList.put("hashKey", pktHashKey);
                    } else {
                        resMap.put(key, String.valueOf(dto.getBody().get(key)));
                    }
                    break;
                case "/v1/giftcard/search":
                    if ("gcList".equals(key)) {
                        List<GiftCardSearch> gcSearchList = jsonUtil.fromJsonArray(jsonUtil.toJson(dto.getBody().get(key)), new TypeReference<List<GiftCardSearch>>() {});

                        List<GiftCardSearch> gcResMapList = new ArrayList<>();

                        for (GiftCardSearch search : gcSearchList) {
                            GiftCardSearch gcResMap = GiftCardSearch.builder()
                                    .gcNo(CipherUtil.decrypt(search.getGcNo(), encKey))
                                    .gcAmt(search.getGcAmt())
                                    .gcStatCd(search.getGcStatCd())
                                    .vldDt(search.getVldDt())
                                    .build();

                            List<GiftCardHistoryInfo> histList = new ArrayList<>();
                            for (GiftCardHistoryInfo hist : search.getHistList()) {
                                GiftCardHistoryInfo resHist = GiftCardHistoryInfo.builder()
                                        .type(hist.getType())
                                        .date(hist.getDate())
                                        .time(hist.getTime())
                                        .trdNo(hist.getTrdNo())
                                        .trdSumry(hist.getTrdSumry())
                                        .mResrvField1(hist.getMResrvField1())
                                        .mResrvField2(hist.getMResrvField2())
                                        .mResrvField3(hist.getMResrvField3())
                                        .newGcNo(StringUtils.isNotEmpty(hist.getNewGcNo()) ?
                                                CipherUtil.decrypt(hist.getNewGcNo(), encKey) : hist.getNewGcNo())
                                        .build();
                                histList.add(resHist);
                            }

                            gcResMap.setHistList(histList);
                            gcResMapList.add(gcResMap);
                        }

                        resMap.put(key, gcResMapList);
                    } else {
                        resMap.put(key, String.valueOf(dto.getBody().get(key)));
                    }
                    break;
                case "/v1/giftcard/reissue":
                    if ("useMid".equals(key)) {
                        hashList.put(key, String.valueOf(dto.getBody().get(key)));
                        resMap.put(key, String.valueOf(dto.getBody().get(key)));
                    } else if ("gcNo".equals(key)) {
                        String gcNo = CipherUtil.decrypt(String.valueOf(dto.getBody().get(key)), encKey);

                        hashList.put(key, gcNo);
                        resMap.put(key, gcNo);
                    } else if ("bfGcNo".equals(key)) {
                        String gcNo = CipherUtil.decrypt(String.valueOf(dto.getBody().get(key)), encKey);

                        hashList.put(key, gcNo);
                        resMap.put(key, gcNo);
                    } else if ("gcAmt".equals(key)) {
                        hashList.put(key, String.valueOf(dto.getBody().get(key)));
                        hashList.put("hashKey", pktHashKey);
                        resMap.put(key, String.valueOf(dto.getBody().get(key)));
                    } else {
                        resMap.put(key, String.valueOf(dto.getBody().get(key)));
                    }
                    break;
            }
        }

        String pktHash = "";
        if (!hashList.isEmpty()) {
            switch (dto.getUri()) {
                case "/v1/giftcard/issue":
                    pktHash = hashList.get("custNo") +
                            hashList.get("mId") +
                            hashList.get("useMid") +
                            hashList.get("mTrdNo") +
                            hashList.get("trdNo") +
                            hashList.get("trdAmt") +
                            hashList.get("hashKey");

                    resMap.put("pktHash", dto.getBody().get("pktHash"));
                    resMap.put("pktHashVerified", CipherSha256Util.digestSHA256(pktHash));
                    break;
                case "/v1/giftcard/use":
                    pktHash = hashList.get("mTrdNo") +
                            hashList.get("gcTrdNo") +
                            hashList.get("useMid") +
                            hashList.get("useGcList") +
                            hashList.get("hashKey");
                    resMap.put("pktHash", dto.getBody().get("pktHash"));
                    resMap.put("pktHashVerified", CipherSha256Util.digestSHA256(pktHash));
                    break;
                case "/v1/giftcard/reissue":
                    pktHash = hashList.get("useMid") +
                            hashList.get("gcNo") +
                            hashList.get("bfGcNo") +
                            hashList.get("gcAmt") +
                            hashList.get("hashKey");
                    resMap.put("pktHash", dto.getBody().get("pktHash"));
                    resMap.put("pktHashVerified", CipherSha256Util.digestSHA256(pktHash));
                    break;
            }
        }

        return ResponseEntity.ok(resMap);
    }
}
