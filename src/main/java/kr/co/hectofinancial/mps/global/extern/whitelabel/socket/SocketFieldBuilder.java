package kr.co.hectofinancial.mps.global.extern.whitelabel.socket;

import kr.co.hectofinancial.mps.global.extern.whitelabel.dto.PinCheckRequestDto;
import kr.co.hectofinancial.mps.global.util.CipherSha256Util;
import kr.co.hectofinancial.mps.global.util.CipherUtil;
import kr.co.hectofinancial.mps.global.util.CustomDateTimeUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
@Component
@Slf4j
public class SocketFieldBuilder {

    private static String WHITELABEL_VERSION;

    @Value("${whitelabel.version}")
    public void setWhitelabelVersion(String whitelabelVersion) {
        WHITELABEL_VERSION = whitelabelVersion;
    }

    private static void writeField(ByteArrayOutputStream stream, String data, int length) throws Exception {
        byte[] byteData = data.getBytes("UTF-8");
        stream.write(byteData, 0, byteData.length);

        for (int i = byteData.length; i < length; i++) {
            stream.write(' ');
        }
    }

    private static byte[] getHeader(int dataLen, String businessType, String mid, String mReqNo, String reqNo, String reqDt, String reqTm,
                                    String custIp, String clientIp, String isMobile, String os, String pktHash) throws Exception {

        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        try {
            writeField(stream, String.format("%06d", dataLen + 494), 6);    // 전문길이
            writeField(stream, "23", 2);  // 암호화 구분코드(AES-256/ECB/PKCS5Padding + Base64)
            writeField(stream, "U", 1); // 문자셋 구분코드
            writeField(stream, "WL", 2);  // 결제수단(화이트라벨)
            writeField(stream, businessType, 2);    // 업무구분
            writeField(stream, WHITELABEL_VERSION, 4);    // 버전
            writeField(stream, mid, 12);    // mid
            writeField(stream, reqDt, 8);   // 거래일자
            writeField(stream, reqTm, 6);   // 거래시간
            writeField(stream, mReqNo, 100);    // 가맹점거래번호
            writeField(stream, reqNo, 40);  // 거래번호
            writeField(stream, "", 208);
            writeField(stream, custIp, 15); // Customer IP
            writeField(stream, clientIp, 15); // Client IP
            writeField(stream, isMobile, 1);  // 모바일여부
            writeField(stream, os, 1);  // OS 구분
            writeField(stream, pktHash, 64);
            writeField(stream, "", 13);
        } finally {
            try {
                stream.close();
            } catch (Exception e) {
                log.error("화이트라벨 전문 생성 실패 getHeader Error! ", e);
            }
        }
        return stream.toByteArray();
    }

    private static byte[] getHeader(int dataLen, String businessType, String mid, String mReqNo, String reqNo, String reqDt, String reqTm, String pktHash) throws Exception {
        return getHeader(dataLen, businessType, mid, mReqNo, reqNo, reqDt, reqTm, "", "", "", "", pktHash);
    }


    public static byte[] getPaymentPinField(PinCheckRequestDto dto, String encKey, String pktHashKey) {

        byte[] result = null;
        ByteArrayOutputStream total = null;
        ByteArrayOutputStream stream = null;

        try {
            stream = new ByteArrayOutputStream();

            String mid = dto.getMId();
            String mReqNo = dto.getMreqNo();
            String custId = dto.getCustId();

            writeField(stream, dto.getTypeCd(), 1);
            writeField(stream, custId, 100);
            writeField(stream, CipherUtil.encrypt(dto.getPmtPwdEnc(), encKey), 24);
            writeField(stream, "", 1);

            byte[] dataPart = stream.toByteArray();

            CustomDateTimeUtil customDateTimeUtil = new CustomDateTimeUtil();
            String reqDt = customDateTimeUtil.getDate();
            String reqTm = customDateTimeUtil.getTime();
            String pktHash = CipherSha256Util.digestSHA256(reqDt + reqTm + mid + mReqNo + custId + pktHashKey);

            byte[] headerPart = getHeader(dataPart.length, "A3", mid, mReqNo, dto.getReqNo(), reqDt, reqTm, pktHash);

            total = new ByteArrayOutputStream();
            total.write(headerPart);
            total.write(dataPart);

            result = total.toByteArray();
        } catch (IOException e) {
            log.error("화이트라벨 핀검증 전문 생성 실패 getPaymentPinField Error! ", e);
            throw new RuntimeException(e);
        } catch (Exception e) {
            log.error("화이트라벨 핀검증 전문 생성 실패 getPaymentPinField Error! ", e);
            throw new RuntimeException(e);
        } finally {
            if (stream != null) {
                try {
                    stream.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            if (total != null) {
                try {
                    total.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }

        return result;
    }

    public static byte[] getPaymentPinField2(String data, String encKey, String pktHashKey) {

        byte[] result = null;
        ByteArrayOutputStream total = null;
        ByteArrayOutputStream stream = null;

        try {
            stream = new ByteArrayOutputStream();

            writeField(stream, data, 1);

            byte[] dataPart = stream.toByteArray();

            total = new ByteArrayOutputStream();
            total.write(dataPart);

            result = total.toByteArray();
        } catch (IOException e) {
            //todo monit log 찍기
            throw new RuntimeException(e);
        } catch (Exception e) {
            //todo monit log 찍기
            throw new RuntimeException(e);
        } finally {
            if (stream != null) {
                try {
                    stream.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            if (total != null) {
                try {
                    total.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }

        return result;
    }

}
