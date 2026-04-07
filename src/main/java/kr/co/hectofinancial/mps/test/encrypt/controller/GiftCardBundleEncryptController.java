package kr.co.hectofinancial.mps.test.encrypt.controller;

import com.fasterxml.jackson.core.type.TypeReference;
import kr.co.hectofinancial.mps.api.v1.common.service.CommonService;
import kr.co.hectofinancial.mps.api.v1.giftcard.bundle.dto.search.GiftCardBundleInfo;
import kr.co.hectofinancial.mps.api.v1.giftcard.single.dto.GiftCardIssueInfo;
import kr.co.hectofinancial.mps.api.v1.market.dto.MarketAddInfoDto;
import kr.co.hectofinancial.mps.global.util.CipherSha256Util;
import kr.co.hectofinancial.mps.global.util.CipherUtil;
import kr.co.hectofinancial.mps.global.util.JsonUtil;
import kr.co.hectofinancial.mps.test.encrypt.dto.EncryptMapRequestDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Profile({"test", "local"})//TB환경에서만 적용
@RequiredArgsConstructor
@RestController
@Slf4j
public class GiftCardBundleEncryptController {

    private final CommonService commonService;
    private final JsonUtil jsonUtil;

    @PostMapping("/test/giftcard/bundle/encrypt")
    public ResponseEntity<Map<String, Object>> encrypt(@RequestBody EncryptMapRequestDto dto) throws Exception {
        Map<String, Object> resMap = new LinkedHashMap<>();

        MarketAddInfoDto marketAddInfoByMId = commonService.getMarketAddInfoByMId(dto.getM_id());
        String encKey = marketAddInfoByMId.getEncKey();
        String pktHashKey = marketAddInfoByMId.getPktHashKey();

        Set<String> keys = dto.getBody().keySet();

        Map<String, String> hashList = new LinkedHashMap<>();
        for (String key : keys) {
            switch (dto.getUri()) {
                case "/v1/giftcard/bundle/charge/etc":
                    if ("gcDstbNo".equals(key)) {
                        hashList.put(key, CipherUtil.encrypt(String.valueOf(dto.getBody().get(key)), encKey));
                        resMap.put(key, CipherUtil.encrypt(String.valueOf(dto.getBody().get(key)), encKey));
                    } else if ("useMid".equals(key)) {
                        hashList.put(key, String.valueOf(dto.getBody().get(key)));
                        resMap.put(key, String.valueOf(dto.getBody().get(key)));
                    } else if ("mTrdNo".equals(key)) {
                        hashList.put(key, String.valueOf(dto.getBody().get(key)));
                        resMap.put(key, String.valueOf(dto.getBody().get(key)));
                    } else if ("trdAmt".equals(key)) {
                        hashList.put(key, String.valueOf(dto.getBody().get(key)));
                        resMap.put(key, CipherUtil.encrypt(String.valueOf(dto.getBody().get(key)), encKey));
                    } else if ("dstbBlc".equals(key)) {
                        hashList.put(key, String.valueOf(dto.getBody().get(key)));
                        resMap.put(key, CipherUtil.encrypt(String.valueOf(dto.getBody().get(key)), encKey));
                        hashList.put("hashKey", pktHashKey);
                    } else if ("chrgMeanCd".equals(key)) {
                        hashList.put(key, String.valueOf(dto.getBody().get(key)));
                        resMap.put(key, String.valueOf(dto.getBody().get(key)));
                        hashList.put("hashKey", pktHashKey);
                    } else if ("chrgTrdNo".equals(key)) {
                        hashList.put(key, String.valueOf(dto.getBody().get(key)));
                        resMap.put(key, String.valueOf(dto.getBody().get(key)));
                        hashList.put("hashKey", pktHashKey);
                    } else {
                        resMap.put(key, String.valueOf(dto.getBody().get(key)));
                    }
                case "/v1/giftcard/bundle/balance/use":
                    if ("gcDstbNo".equals(key)) {
                        hashList.put(key, CipherUtil.encrypt(String.valueOf(dto.getBody().get(key)), encKey));
                        resMap.put(key, CipherUtil.encrypt(String.valueOf(dto.getBody().get(key)), encKey));
                    } else if ("useMid".equals(key)) {
                        hashList.put(key, String.valueOf(dto.getBody().get(key)));
                        resMap.put(key, String.valueOf(dto.getBody().get(key)));
                    } else if ("mTrdNo".equals(key)) {
                        hashList.put(key, String.valueOf(dto.getBody().get(key)));
                        resMap.put(key, String.valueOf(dto.getBody().get(key)));
                    } else if ("trdAmt".equals(key)) {
                        hashList.put(key, String.valueOf(dto.getBody().get(key)));
                        resMap.put(key, CipherUtil.encrypt(String.valueOf(dto.getBody().get(key)), encKey));
                    } else if ("dstbBlc".equals(key)) {
                        hashList.put(key, String.valueOf(dto.getBody().get(key)));
                        resMap.put(key, CipherUtil.encrypt(String.valueOf(dto.getBody().get(key)), encKey));
                        hashList.put("hashKey", pktHashKey);
                    } else {
                        resMap.put(key, String.valueOf(dto.getBody().get(key)));
                    }
                    break;
                case "/v1/giftcard/bundle/charge":
                    if ("gcDstbNo".equals(key)) {
                        hashList.put(key, CipherUtil.encrypt(String.valueOf(dto.getBody().get(key)), encKey));
                        resMap.put(key, CipherUtil.encrypt(String.valueOf(dto.getBody().get(key)), encKey));
                    } else if ("useMid".equals(key)) {
                        hashList.put(key, String.valueOf(dto.getBody().get(key)));
                        resMap.put(key, String.valueOf(dto.getBody().get(key)));
                    } else if ("mTrdNo".equals(key)) {
                        hashList.put(key, String.valueOf(dto.getBody().get(key)));
                        resMap.put(key, String.valueOf(dto.getBody().get(key)));
                    } else if ("trdAmt".equals(key)) {
                        hashList.put(key, String.valueOf(dto.getBody().get(key)));
                        resMap.put(key, CipherUtil.encrypt(String.valueOf(dto.getBody().get(key)), encKey));
                    } else if ("mnyBlc".equals(key)) {
                        resMap.put(key, CipherUtil.encrypt(String.valueOf(dto.getBody().get(key)), encKey));
                    } else if ("pntBlc".equals(key)) {
                        resMap.put(key, CipherUtil.encrypt(String.valueOf(dto.getBody().get(key)), encKey));
                    } else if ("pinNo".equals(key)) {
                        resMap.put(key, CipherUtil.encrypt(String.valueOf(dto.getBody().get(key)), encKey));
                    } else if ("dstbBlc".equals(key)) {
                        hashList.put(key, String.valueOf(dto.getBody().get(key)));
                        resMap.put(key, CipherUtil.encrypt(String.valueOf(dto.getBody().get(key)), encKey));
                        hashList.put("hashKey", pktHashKey);
                    } else {
                        resMap.put(key, String.valueOf(dto.getBody().get(key)));
                    }
                    break;
                case "/v1/giftcard/bundle/issue":
                    if ("gcDstbNo".equals(key)) {
                        hashList.put(key, CipherUtil.encrypt(String.valueOf(dto.getBody().get(key)), encKey));
                        resMap.put(key, CipherUtil.encrypt(String.valueOf(dto.getBody().get(key)), encKey));
                    } else if ("useMid".equals(key)) {
                        hashList.put(key, String.valueOf(dto.getBody().get(key)));
                        resMap.put(key, String.valueOf(dto.getBody().get(key)));
                    } else if ("mTrdNo".equals(key)) {
                        hashList.put(key, String.valueOf(dto.getBody().get(key)));
                        resMap.put(key, String.valueOf(dto.getBody().get(key)));
                    } else if ("trdAmt".equals(key)) {
                        hashList.put(key, String.valueOf(dto.getBody().get(key)));
                        resMap.put(key, CipherUtil.encrypt(String.valueOf(dto.getBody().get(key)), encKey));
                    } else if ("dstbBlc".equals(key)) {
                        hashList.put(key, String.valueOf(dto.getBody().get(key)));
                        resMap.put(key, CipherUtil.encrypt(String.valueOf(dto.getBody().get(key)), encKey));
                        hashList.put("hashKey", pktHashKey);
                    } else if ("issList".equals(key)) {
                        List<GiftCardIssueInfo> issList = jsonUtil.fromJsonArray(jsonUtil.toJson(dto.getBody().get(key)), new TypeReference<List<GiftCardIssueInfo>>() {});
                        resMap.put(key, issList);
                    } else {
                        resMap.put(key, String.valueOf(dto.getBody().get(key)));
                    }
                    break;
                case "/v1/giftcard/bundle/transfer":
                    if ("gcDstbNo".equals(key)) {
                        hashList.put(key, CipherUtil.encrypt(String.valueOf(dto.getBody().get(key)), encKey));
                        resMap.put(key, CipherUtil.encrypt(String.valueOf(dto.getBody().get(key)), encKey));
                    } else if ("useMid".equals(key)) {
                        hashList.put(key, String.valueOf(dto.getBody().get(key)));
                        resMap.put(key, String.valueOf(dto.getBody().get(key)));
                    } else if ("mTrdNo".equals(key)) {
                        hashList.put(key, String.valueOf(dto.getBody().get(key)));
                        resMap.put(key, String.valueOf(dto.getBody().get(key)));
                    } else if ("gcBndlNo".equals(key)) {
                        hashList.put(key, String.valueOf(dto.getBody().get(key)));
                        resMap.put(key, CipherUtil.encrypt(String.valueOf(dto.getBody().get(key)), encKey));
                    } else if ("trdAmt".equals(key)) {
                        hashList.put(key, String.valueOf(dto.getBody().get(key)));
                        resMap.put(key, CipherUtil.encrypt(String.valueOf(dto.getBody().get(key)), encKey));
                    } else if ("dstbBlc".equals(key)) {
                        hashList.put(key, String.valueOf(dto.getBody().get(key)));
                        resMap.put(key, CipherUtil.encrypt(String.valueOf(dto.getBody().get(key)), encKey));
                        hashList.put("hashKey", pktHashKey);
                    } else {
                        resMap.put(key, String.valueOf(dto.getBody().get(key)));
                    }
                    break;
                case "/v1/giftcard/bundle/use":
                    if ("mTrdNo".equals(key)) {
                        hashList.put(key, String.valueOf(dto.getBody().get(key)));
                        resMap.put(key, String.valueOf(dto.getBody().get(key)));
                    } else if ("useMid".equals(key)) {
                        hashList.put(key, String.valueOf(dto.getBody().get(key)));
                        resMap.put(key, String.valueOf(dto.getBody().get(key)));
                    } else if ("gcBndlNo".equals(key)) {
                        hashList.put(key, String.valueOf(dto.getBody().get(key)));
                        hashList.put("hashKey", pktHashKey);
                        resMap.put(key, CipherUtil.encrypt(String.valueOf(dto.getBody().get(key)), encKey));
                    } else if ("stlMid".equals(key)) {
                        hashList.put(key, String.valueOf(dto.getBody().get(key)));
                        resMap.put(key, CipherUtil.encrypt(String.valueOf(dto.getBody().get(key)), encKey));
                    } else if ("trdAmt".equals(key)) {
                        hashList.put(key, String.valueOf(dto.getBody().get(key)));
                        resMap.put(key, CipherUtil.encrypt(String.valueOf(dto.getBody().get(key)), encKey));
                    } else {
                        resMap.put(key, String.valueOf(dto.getBody().get(key)));
                    }
                    break;
                case "/v1/giftcard/bundle/info":
                    if ("gcDstbNo".equals(key)) {
                        resMap.put(key, CipherUtil.encrypt(String.valueOf(dto.getBody().get(key)), encKey));
                    } else if ("gcBndlNo".equals(key)) {
                        resMap.put(key, CipherUtil.encrypt(String.valueOf(dto.getBody().get(key)), encKey));
                    } else {
                        resMap.put(key, String.valueOf(dto.getBody().get(key)));
                    }
                    break;
                case "/v1/giftcard/bundle/balance":
                case "/v1/giftcard/bundle/list":
                    if ("gcDstbNo".equals(key)) {
                        resMap.put(key, CipherUtil.encrypt(String.valueOf(dto.getBody().get(key)), encKey));
                    } else {
                        resMap.put(key, String.valueOf(dto.getBody().get(key)));
                    }
                    break;
                case "/v1/giftcard/bundle/balance/use/cancel":
                case "/v1/giftcard/bundle/charge/cancel":
                    if ("gcDstbNo".equals(key)) {
                        hashList.put(key, CipherUtil.encrypt(String.valueOf(dto.getBody().get(key)), encKey));
                        resMap.put(key, CipherUtil.encrypt(String.valueOf(dto.getBody().get(key)), encKey));
                    } else if ("useMid".equals(key)) {
                        hashList.put(key, String.valueOf(dto.getBody().get(key)));
                        resMap.put(key, String.valueOf(dto.getBody().get(key)));
                    } else if ("mTrdNo".equals(key)) {
                        hashList.put(key, String.valueOf(dto.getBody().get(key)));
                        resMap.put(key, String.valueOf(dto.getBody().get(key)));
                    } else if ("trdAmt".equals(key)) {
                        hashList.put(key, String.valueOf(dto.getBody().get(key)));
                        resMap.put(key, CipherUtil.encrypt(String.valueOf(dto.getBody().get(key)), encKey));
                    } else if ("orgDstbTrdNo".equals(key)) {
                        hashList.put(key, String.valueOf(dto.getBody().get(key)));
                        resMap.put(key, String.valueOf(dto.getBody().get(key)));
                    } else if ("orgTrdDt".equals(key)) {
                        hashList.put(key, String.valueOf(dto.getBody().get(key)));
                        resMap.put(key, String.valueOf(dto.getBody().get(key)));
                    } else if ("dstbBlc".equals(key)) {
                        hashList.put(key, String.valueOf(dto.getBody().get(key)));
                        resMap.put(key, CipherUtil.encrypt(String.valueOf(dto.getBody().get(key)), encKey));
                        hashList.put("hashKey", pktHashKey);
                    } else {
                        resMap.put(key, String.valueOf(dto.getBody().get(key)));
                    }
                    break;
            }
        }

        String pktHash;
        if (!hashList.isEmpty()) {
            switch (dto.getUri()) {
                case "/v1/giftcard/bundle/charge/etc":
                    pktHash = hashList.get("gcDstbNo") +
                            hashList.get("useMid") +
                            hashList.get("mTrdNo") +
                            hashList.get("trdAmt") +
                            hashList.get("dstbBlc") +
                            hashList.get("chrgMeanCd") +
                            hashList.get("chrgTrdNo") +
                            hashList.get("hashKey");
                    resMap.put("pktHash", CipherSha256Util.digestSHA256(pktHash));
                    break;
                case "/v1/giftcard/bundle/charge":
                case "/v1/giftcard/bundle/issue":
                case "/v1/giftcard/bundle/balance/use":
                    pktHash = hashList.get("gcDstbNo") +
                            hashList.get("useMid") +
                            hashList.get("mTrdNo") +
                            hashList.get("trdAmt") +
                            hashList.get("dstbBlc") +
                            hashList.get("hashKey");
                    resMap.put("pktHash", CipherSha256Util.digestSHA256(pktHash));
                    break;
                case "/v1/giftcard/bundle/transfer":
                    pktHash = hashList.get("gcDstbNo") +
                            hashList.get("useMid") +
                            hashList.get("mTrdNo") +
                            hashList.get("gcBndlNo") +
                            hashList.get("trdAmt") +
                            hashList.get("dstbBlc") +
                            hashList.get("hashKey");
                    resMap.put("pktHash", CipherSha256Util.digestSHA256(pktHash));
                    break;
                case "/v1/giftcard/bundle/use":
                    pktHash = hashList.get("mTrdNo") +
                            hashList.get("useMid") +
                            hashList.get("gcBndlNo") +
                            hashList.get("trdAmt") +
                            hashList.get("stlMid") +
                            hashList.get("hashKey");
                    resMap.put("pktHash", CipherSha256Util.digestSHA256(pktHash));
                    break;
                case "/v1/giftcard/bundle/balance/use/cancel":
                case "/v1/giftcard/bundle/charge/cancel":
                    pktHash = hashList.get("gcDstbNo") +
                            hashList.get("useMid") +
                            hashList.get("mTrdNo") +
                            hashList.get("orgDstbTrdNo") +
                            hashList.get("orgTrdDt") +
                            hashList.get("trdAmt") +
                            hashList.get("dstbBlc") +
                            hashList.get("hashKey");
                    resMap.put("pktHash", CipherSha256Util.digestSHA256(pktHash));
                    break;
            }
        }

        return ResponseEntity.ok(resMap);
    }

    @PostMapping("/test/giftcard/bundle/decrypt")
    public ResponseEntity<Map<String, Object>> decrypt(@RequestBody EncryptMapRequestDto dto) throws Exception {
        Map<String, Object> resMap = new LinkedHashMap<>();

        MarketAddInfoDto marketAddInfoByMId = commonService.getMarketAddInfoByMId(dto.getM_id());
        String encKey = marketAddInfoByMId.getEncKey();
        String pktHashKey = marketAddInfoByMId.getPktHashKey();

        Set<String> keys = dto.getBody().keySet();
        Map<String, String> hashList = new LinkedHashMap<>();
        for (String key : keys) {
            switch (dto.getUri()) {
                case "/v1/giftcard/bundle/charge":
                    if ("gcDstbNo".equals(key)) {
                        String custNo = String.valueOf(dto.getBody().get(key));
                        hashList.put(key, custNo);
                        resMap.put(key, custNo);
                    } else if ("useMid".equals(key)) {
                        hashList.put(key, String.valueOf(dto.getBody().get(key)));
                        resMap.put(key, String.valueOf(dto.getBody().get(key)));
                    } else if ("mTrdNo".equals(key)) {
                        hashList.put(key, String.valueOf(dto.getBody().get(key)));
                        resMap.put(key, String.valueOf(dto.getBody().get(key)));
                    } else if ("dstbTrdNo".equals(key)) {
                        hashList.put(key, String.valueOf(dto.getBody().get(key)));
                        resMap.put(key, String.valueOf(dto.getBody().get(key)));
                    } else if ("trdAmt".equals(key)) {
                        hashList.put(key, String.valueOf(dto.getBody().get(key)));
                        resMap.put(key, String.valueOf(dto.getBody().get(key)));
                    } else if ("mnyAmt".equals(key)) {
                        resMap.put(key, String.valueOf(dto.getBody().get(key)));
                    } else if ("pntAmt".equals(key)) {
                        resMap.put(key, String.valueOf(dto.getBody().get(key)));
                    } else if ("dstbBlc".equals(key)) {
                        hashList.put(key, String.valueOf(dto.getBody().get(key)));
                        resMap.put(key, String.valueOf(dto.getBody().get(key)));
                        hashList.put("hashKey", pktHashKey);
                    } else {
                        resMap.put(key, String.valueOf(dto.getBody().get(key)));
                    }
                    break;
                case "/v1/giftcard/bundle/charge/etc":
                    if ("gcDstbNo".equals(key)) {
                        String custNo = String.valueOf(dto.getBody().get(key));
                        hashList.put(key, custNo);
                        resMap.put(key, custNo);
                    } else if ("useMid".equals(key)) {
                        hashList.put(key, String.valueOf(dto.getBody().get(key)));
                        resMap.put(key, String.valueOf(dto.getBody().get(key)));
                    } else if ("mTrdNo".equals(key)) {
                        hashList.put(key, String.valueOf(dto.getBody().get(key)));
                        resMap.put(key, String.valueOf(dto.getBody().get(key)));
                    } else if ("dstbTrdNo".equals(key)) {
                        hashList.put(key, String.valueOf(dto.getBody().get(key)));
                        resMap.put(key, String.valueOf(dto.getBody().get(key)));
                    } else if ("trdAmt".equals(key)) {
                        hashList.put(key, String.valueOf(dto.getBody().get(key)));
                        resMap.put(key, String.valueOf(dto.getBody().get(key)));
                    } else if ("chrgMeanCd".equals(key)) {
                        hashList.put(key, String.valueOf(dto.getBody().get(key)));
                        resMap.put(key, String.valueOf(dto.getBody().get(key)));
                    } else if ("chrgTrdNo".equals(key)) {
                        hashList.put(key, String.valueOf(dto.getBody().get(key)));
                        resMap.put(key, String.valueOf(dto.getBody().get(key)));
                    } else if ("dstbBlc".equals(key)) {
                        hashList.put(key, String.valueOf(dto.getBody().get(key)));
                        resMap.put(key, String.valueOf(dto.getBody().get(key)));
                        hashList.put("hashKey", pktHashKey);
                    } else {
                        resMap.put(key, String.valueOf(dto.getBody().get(key)));
                    }
                    break;
                case "/v1/giftcard/bundle/issue":
                    if ("gcDstbNo".equals(key)) {
                        String custNo = String.valueOf(dto.getBody().get(key));
                        hashList.put(key, custNo);
                        resMap.put(key, custNo);
                    } else if ("useMid".equals(key)) {
                        hashList.put(key, String.valueOf(dto.getBody().get(key)));
                        resMap.put(key, String.valueOf(dto.getBody().get(key)));
                    } else if ("mTrdNo".equals(key)) {
                        hashList.put(key, String.valueOf(dto.getBody().get(key)));
                        resMap.put(key, String.valueOf(dto.getBody().get(key)));
                    } else if ("gcBndlNo".equals(key)) {
                        String gcBndlNo = CipherUtil.decrypt(String.valueOf(dto.getBody().get(key)), encKey);
                        hashList.put(key, gcBndlNo);
                        resMap.put(key, gcBndlNo);
                    } else if ("dstbTrdNo".equals(key)) {
                        hashList.put(key, String.valueOf(dto.getBody().get(key)));
                        resMap.put(key, String.valueOf(dto.getBody().get(key)));
                    } else if ("trdAmt".equals(key)) {
                        hashList.put(key, String.valueOf(dto.getBody().get(key)));
                        resMap.put(key, String.valueOf(dto.getBody().get(key)));
                    } else if ("dstbBlc".equals(key)) {
                        hashList.put(key, String.valueOf(dto.getBody().get(key)));
                        resMap.put(key, String.valueOf(dto.getBody().get(key)));
                        hashList.put("hashKey", pktHashKey);
                    } else {
                        resMap.put(key, String.valueOf(dto.getBody().get(key)));
                    }
                    break;
                case "/v1/giftcard/bundle/transfer":
                    if ("gcDstbNo".equals(key)) {
                        String custNo = String.valueOf(dto.getBody().get(key));
                        hashList.put(key, custNo);
                        resMap.put(key, custNo);
                    } else if ("useMid".equals(key)) {
                        hashList.put(key, String.valueOf(dto.getBody().get(key)));
                        resMap.put(key, String.valueOf(dto.getBody().get(key)));
                    } else if ("mTrdNo".equals(key)) {
                        hashList.put(key, String.valueOf(dto.getBody().get(key)));
                        resMap.put(key, String.valueOf(dto.getBody().get(key)));
                    } else if ("dstbTrdNo".equals(key)) {
                        hashList.put(key, String.valueOf(dto.getBody().get(key)));
                        resMap.put(key, String.valueOf(dto.getBody().get(key)));
                    } else if ("trdAmt".equals(key)) {
                        hashList.put(key, String.valueOf(dto.getBody().get(key)));
                        resMap.put(key, String.valueOf(dto.getBody().get(key)));
                    } else if ("gcBndlNo".equals(key)) {
                        String gcBndlNo = CipherUtil.decrypt(String.valueOf(dto.getBody().get(key)), encKey);
                        hashList.put(key, gcBndlNo);
                        resMap.put(key, gcBndlNo);
                    } else if ("gcBndlStatCd".equals(key)) {
                        hashList.put(key, String.valueOf(dto.getBody().get(key)));
                        resMap.put(key, String.valueOf(dto.getBody().get(key)));
                    } else if ("dstbBlc".equals(key)) {
                        hashList.put(key, String.valueOf(dto.getBody().get(key)));
                        resMap.put(key, String.valueOf(dto.getBody().get(key)));
                        hashList.put("hashKey", pktHashKey);
                    } else {
                        resMap.put(key, String.valueOf(dto.getBody().get(key)));
                    }
                    break;
                case "/v1/giftcard/bundle/use":
                    if ("mTrdNo".equals(key)) {
                        hashList.put(key, String.valueOf(dto.getBody().get(key)));
                        resMap.put(key, String.valueOf(dto.getBody().get(key)));
                    } else if ("gcTrdNo".equals(key)) {
                        hashList.put(key, String.valueOf(dto.getBody().get(key)));
                        resMap.put(key, String.valueOf(dto.getBody().get(key)));
                    } else if ("useMid".equals(key)) {
                        hashList.put(key, String.valueOf(dto.getBody().get(key)));
                        resMap.put(key, String.valueOf(dto.getBody().get(key)));
                    } else if ("gcBndlNo".equals(key)) {
                        String gcBndlNo = CipherUtil.decrypt(String.valueOf(dto.getBody().get(key)), encKey);
                        hashList.put(key, gcBndlNo);
                        resMap.put(key, gcBndlNo);
                        hashList.put("hashKey", pktHashKey);
                    } else if ("stlMid".equals(key)) {
                        hashList.put(key, String.valueOf(dto.getBody().get(key)));
                        resMap.put(key, String.valueOf(dto.getBody().get(key)));
                    } else if ("trdAmt".equals(key)) {
                        hashList.put(key, String.valueOf(dto.getBody().get(key)));
                        resMap.put(key, String.valueOf(dto.getBody().get(key)));
                    } else if ("gcBndlStatCd".equals(key)) {
                        hashList.put(key, String.valueOf(dto.getBody().get(key)));
                        resMap.put(key, String.valueOf(dto.getBody().get(key)));
                    } else {
                        resMap.put(key, String.valueOf(dto.getBody().get(key)));
                    }
                    break;
                case "/v1/giftcard/bundle/info":
                    if ("gcBndlNo".equals(key)) {
                        resMap.put(key, CipherUtil.decrypt(String.valueOf(dto.getBody().get(key)), encKey));
                    } else if ("issList".equals(key)) {
                        List<GiftCardIssueInfo> issList = jsonUtil.fromJsonArray(jsonUtil.toJson(dto.getBody().get(key)), new TypeReference<List<GiftCardIssueInfo>>() {});
                        resMap.put(key, issList);
                    } else if ("histList".equals(key)) {
                        if (dto.getBody().get(key) != null) {
                            List<Map<String, String>> histList = jsonUtil.fromJsonArray(jsonUtil.toJson(dto.getBody().get(key)), new TypeReference<List<Map<String, String>>>() {});
                            resMap.put(key, histList);
                        }
                    } else {
                        resMap.put(key, String.valueOf(dto.getBody().get(key)));
                    }
                    break;
                case "/v1/giftcard/bundle/balance":
                    resMap.put(key, String.valueOf(dto.getBody().get(key)));
                    break;
                case "/v1/giftcard/bundle/list":
                    if ("gcBndlList".equals(key)) {
                        if (dto.getBody().get(key) != null) {
                            List<GiftCardBundleInfo>  gcBndlList = jsonUtil.fromJsonArray(jsonUtil.toJson(dto.getBody().get(key)), new TypeReference<List<GiftCardBundleInfo>>() {});
                            for (GiftCardBundleInfo gcBndl : gcBndlList) {
                                gcBndl.setGcBndlNo(CipherUtil.decrypt(String.valueOf(gcBndl.getGcBndlNo()), encKey));
                            }
                            resMap.put(key, gcBndlList);
                        }
                    } else {
                        resMap.put(key, String.valueOf(dto.getBody().get(key)));
                    }
                    break;
                case "/v1/giftcard/bundle/balance/use/cancel":
                case "/v1/giftcard/bundle/charge/cancel":
                case "/v1/giftcard/bundle/balance/use":
                    if ("gcDstbNo".equals(key)) {
                        String custNo = String.valueOf(dto.getBody().get(key));
                        hashList.put(key, custNo);
                        resMap.put(key, custNo);
                    } else if ("useMid".equals(key)) {
                        hashList.put(key, String.valueOf(dto.getBody().get(key)));
                        resMap.put(key, String.valueOf(dto.getBody().get(key)));
                    } else if ("mTrdNo".equals(key)) {
                        hashList.put(key, String.valueOf(dto.getBody().get(key)));
                        resMap.put(key, String.valueOf(dto.getBody().get(key)));
                    } else if ("dstbTrdNo".equals(key)) {
                        hashList.put(key, String.valueOf(dto.getBody().get(key)));
                        resMap.put(key, String.valueOf(dto.getBody().get(key)));
                    } else if ("trdAmt".equals(key)) {
                        hashList.put(key, String.valueOf(dto.getBody().get(key)));
                        resMap.put(key, String.valueOf(dto.getBody().get(key)));
                    } else if ("dstbBlc".equals(key)) {
                        hashList.put(key, String.valueOf(dto.getBody().get(key)));
                        resMap.put(key, String.valueOf(dto.getBody().get(key)));
                        hashList.put("hashKey", pktHashKey);
                    } else {
                        resMap.put(key, String.valueOf(dto.getBody().get(key)));
                    }
                    break;
            }
        }

        String pktHash;
        if (!hashList.isEmpty()) {
            switch (dto.getUri()) {
                case "/v1/giftcard/bundle/balance/use/cancel":
                case "/v1/giftcard/bundle/charge":
                case "/v1/giftcard/bundle/charge/cancel":
                case "/v1/giftcard/bundle/balance/use":
                    pktHash = hashList.get("gcDstbNo") +
                            hashList.get("useMid") +
                            hashList.get("mTrdNo") +
                            hashList.get("dstbTrdNo") +
                            hashList.get("trdAmt") +
                            hashList.get("dstbBlc") +
                            hashList.get("hashKey");
                    log.info("pktHash: {}", pktHash);

                    resMap.put("pktHash", dto.getBody().get("pktHash"));
                    resMap.put("pktHashVerified", CipherSha256Util.digestSHA256(pktHash));
                    break;
                case "/v1/giftcard/bundle/charge/etc":
                    pktHash = hashList.get("gcDstbNo") +
                            hashList.get("useMid") +
                            hashList.get("mTrdNo") +
                            hashList.get("dstbTrdNo") +
                            hashList.get("trdAmt") +
                            hashList.get("dstbBlc") +
                            hashList.get("chrgMeanCd") +
                            hashList.get("chrgTrdNo") +
                            hashList.get("hashKey");

                    resMap.put("pktHash", dto.getBody().get("pktHash"));
                    resMap.put("pktHashVerified", CipherSha256Util.digestSHA256(pktHash));
                    break;
                case "/v1/giftcard/bundle/issue":
                    pktHash = hashList.get("gcDstbNo") +
                            hashList.get("useMid") +
                            hashList.get("mTrdNo") +
                            hashList.get("dstbTrdNo") +
                            hashList.get("gcBndlNo") +
                            hashList.get("trdAmt") +
                            hashList.get("dstbBlc") +
                            hashList.get("hashKey");

                    resMap.put("pktHash", dto.getBody().get("pktHash"));
                    resMap.put("pktHashVerified", CipherSha256Util.digestSHA256(pktHash));
                    break;
                case "/v1/giftcard/bundle/transfer":
                    pktHash = hashList.get("gcDstbNo") +
                            hashList.get("useMid") +
                            hashList.get("mTrdNo") +
                            hashList.get("dstbTrdNo") +
                            hashList.get("gcBndlNo") +
                            hashList.get("gcBndlStatCd") +
                            hashList.get("trdAmt") +
                            hashList.get("dstbBlc") +
                            hashList.get("hashKey");

                    resMap.put("pktHash", dto.getBody().get("pktHash"));
                    resMap.put("pktHashVerified", CipherSha256Util.digestSHA256(pktHash));
                    break;
                case "/v1/giftcard/bundle/use":
                    pktHash = hashList.get("mTrdNo") +
                            hashList.get("gcTrdNo") +
                            hashList.get("useMid") +
                            hashList.get("gcBndlNo") +
                            hashList.get("trdAmt") +
                            hashList.get("stlMid") +
                            hashList.get("gcBndlStatCd") +
                            hashList.get("hashKey");
                    resMap.put("pktHash", dto.getBody().get("pktHash"));
                    resMap.put("pktHashVerified", CipherSha256Util.digestSHA256(pktHash));
                    break;
            }
        }

        return ResponseEntity.ok(resMap);
    }
}