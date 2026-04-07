package kr.co.hectofinancial.mps.api.v1.notification.dto;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Data
public class NotiLineworksDTO {
	
	private String appServiceSeq; //Notification GW에서 발급한 어플리케이션 고유번호
	private String apiVersion;
	private String authToken;     //Notification GW에서 발급한 어플리케이션 인증 토큰
	
	private String encoding;      // utf-8
	private String targetType;    // R 대화방, P 사용자
	private String targetId;      // 대화방, 사용자 ID
	private String botNo;		  // 메시지를 보낼 봇 번호 (targetType가 'P'인경우 필수)
	private String contentType;   // 메시지 타입 (T : 일반 텍스트, B : 버튼메시지)
	private String text;          // 내용
	
	private String ordDay;        // 주문일자
	private String ordTime;       // 주문시간
	private String ordNo;         // 주문번호
	
	private String param1;
	private String param2;
	
	private String linkTypeCd;    // 링크 타입 (HLD : 휴일정산,  FUND_TRSF: 자금이관)
	private String linkText;      // 링크표시 텍스트
	private String linkUrl;       // 링크 URL
	private String callbackUrl;   // 전송 결과 수신 URL
	private String instId;
	private String instIp;
	private String hostName;
	private String msgTypeCode;
	
	
	
}
