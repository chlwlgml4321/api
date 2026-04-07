package kr.co.hectofinancial.mps.global.error;

import kr.co.hectofinancial.mps.global.error.exception.RequestValidationException;
import kr.co.hectofinancial.mps.global.mtms.MonitAgent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.orm.jpa.JpaSystemException;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Map;

@Slf4j
@RestControllerAdvice
@RequiredArgsConstructor
public class GlobalExceptionHandler {

    /**
     * 비지니스 로직 수행 도중 RequestValidationException 발생시
     */
    @ExceptionHandler(RequestValidationException.class)
    protected ResponseEntity<ErrorResponse> handleRequestException(RequestValidationException e) {
        ErrorCode errorCode = e.getErrorCode();
        String errorMsg = errorCode.getErrorMessage();
        if (StringUtils.isNotBlank(e.getAdditionalErrorMsg())) {
            errorMsg += e.getAdditionalErrorMsg();
        }
        ErrorResponse errorResponse = ErrorResponse.of(errorCode.getErrorCode(), errorMsg);

        if (StringUtils.isNotBlank(errorCode.getMonitFlag())) {
            MonitAgent.sendMonitAgent(e);
        }

        log.error("*** RequestValidation 내 예외 발생 => 오류코드[{}] {}", errorCode, printErrorStackTrace(e));
        return new ResponseEntity<>(errorResponse, errorCode.getHttpStatus());
    }


    /**
     * 요청 파라미터인 @RequestBody 가 @Valid 와 함께 쓰일 경우, ~RequestDto 에 @NotEmpty, @NotBlank 등 걸릴 경우 발생하는 Exception //TODO 이부분 로그 파라미터 로그찍어야함
     */
    @ExceptionHandler({MethodArgumentNotValidException.class})
    protected ResponseEntity<ErrorResponse> handleMethodArgumentNotValidException(MethodArgumentNotValidException e) {
        ErrorCode errorCode = ErrorCode.ESSENTIAL_PARAM_EMPTY;
        String errorMsg = errorCode.getErrorMessage() + "(" + e.getFieldErrors().get(0).getDefaultMessage() + ")";
        ErrorResponse errorResponse = ErrorResponse.of(errorCode.getErrorCode(), errorMsg);

        log.error("*** MethodArgumentsValidation 내 예외 발생 => 오류코드[{}] {}", errorCode, e);
        return new ResponseEntity<>(errorResponse, errorCode.getHttpStatus());
    }

    /**
     * entity에 선언해 놓은 변수 데이터 타입과 DB 데이터 타입이 달라서 발생하는 exception (예: entity에는 int 인데, DB에서 nullable 이라 값이 NULL 일경우)
     * @param e
     * @return
     */
    @ExceptionHandler(JpaSystemException.class)
    protected ResponseEntity<ErrorResponse> handleJpaSystemException(JpaSystemException e) {
        ErrorCode errorCode = ErrorCode.UNDEFINED_SERVER_ERROR_CODE;
        ErrorResponse errorResponse = ErrorResponse.of(errorCode.getErrorCode(), errorCode.getErrorMessage());

        String monitMsg = " Entity 내 선언된 변수의 데이터 타입과 DB 테이블 데이터 타입 불일치로 발생! 원인 => " + e.getMostSpecificCause();
        MonitAgent.sendMonitAgent(errorCode.getErrorCode(), monitMsg);

        log.error("*** {} => 오류코드[{}] {}", monitMsg, errorCode, printErrorStackTrace(e));
        e.printStackTrace();
        return new ResponseEntity<>(errorResponse, errorCode.getHttpStatus());
    }

    /**
     * JSON 형식의 파라미터가 올바르지 않을 경우 발생하는 오류 (예:마지막 파라미터 이후에도 , 존재 등 )
     */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    protected ResponseEntity<ErrorResponse> HttpMessageNotReadableException(HttpMessageNotReadableException e) {
        ErrorCode errorCode = ErrorCode.JSON_FORMAT_ERROR;
        ErrorResponse errorResponse = ErrorResponse.of(errorCode.getErrorCode(), errorCode.getErrorMessage());

        log.error("*** 요청파라미터 내 JSON 양식 오류 발생 => 오류코드[{}] {}", errorCode, printErrorStackTrace(e));

        e.printStackTrace();
        return new ResponseEntity<>(errorResponse, errorCode.getHttpStatus());
    }
    /**
     * 지원하지 않는 ContentType 으로 요청 보냈을 경우 발생하는 오류
     */
    @ExceptionHandler({HttpMediaTypeNotSupportedException.class})
    protected ResponseEntity<ErrorResponse> HttpMediaTypeNotSupportedException(HttpMediaTypeNotSupportedException e) {
        ErrorCode errorCode = ErrorCode.NOT_SUPPORTED_CONTENT_TYPE;
        ErrorResponse errorResponse = ErrorResponse.of(errorCode.getErrorCode(), errorCode.getErrorMessage());

        log.error("*** 지원하지 않는 ContentType => 오류코드[{}] {}", errorCode, printErrorStackTrace(e));

        e.printStackTrace();
        return new ResponseEntity<>(errorResponse, errorCode.getHttpStatus());
    }

    /**
     * POST 이외에 다른 형식의 Http Method Type 으로 요청 보냈을 경우 발생하는 오류
     */
    @ExceptionHandler({HttpRequestMethodNotSupportedException.class})
    protected ResponseEntity<ErrorResponse> HttpMediaTypeNotSupportedException(HttpRequestMethodNotSupportedException e) {
        ErrorCode errorCode = ErrorCode.NOT_SUPPORTED_METHOD;
        ErrorResponse errorResponse = ErrorResponse.of(errorCode.getErrorCode(), errorCode.getErrorMessage());

        log.error("*** 지원하지 않는 Http Method => 오류코드[{}] {}", errorCode, printErrorStackTrace(e));

        e.printStackTrace();
        return new ResponseEntity<>(errorResponse, errorCode.getHttpStatus());
    }
    /**
     * NullPointerException 발생
     */
    @ExceptionHandler(NullPointerException.class)
    protected ResponseEntity<ErrorResponse> NullPointerException(NullPointerException e) {
        ErrorCode errorCode = ErrorCode.SERVER_ERROR_CODE;
        ErrorResponse errorResponse = ErrorResponse.of(errorCode.getErrorCode(), errorCode.getErrorMessage());

        String monitMsg = " NullPointerException 발생! 원인 => " + e.getMessage();
        MonitAgent.sendMonitAgent(errorCode.getErrorCode(), monitMsg);

        log.error("*** {} => 오류코드[{}] {}", monitMsg, errorCode, printErrorStackTrace(e));
        e.printStackTrace();
        return new ResponseEntity<>(errorResponse, errorCode.getHttpStatus());
    }
    /**
     * 정의되지않은 Exception 발생시 정의되지 않은 예외로 MTMS 알람발생
     */
    @ExceptionHandler(Exception.class)
    protected ResponseEntity<ErrorResponse> handleException(Exception e) {
        ErrorCode errorCode = ErrorCode.UNDEFINED_SERVER_ERROR_CODE;
        ErrorResponse errorResponse = ErrorResponse.of(errorCode.getErrorCode(), errorCode.getErrorMessage());

        String eName = StringUtils.isNotEmpty(e.getClass().getSimpleName()) ? e.getClass().getSimpleName() : "";
        String eMessage = StringUtils.isNotEmpty(e.getMessage()) ? e.getMessage() : "";
        String monitMsg = eName + " 발생! 원인 => " + eMessage;

        MonitAgent.sendMonitAgent(errorCode.getErrorCode(), monitMsg);

        log.error("*** 정의되지 않은 예외 발생 => {}", eName, e);
        e.printStackTrace();
        return new ResponseEntity<>(errorResponse, errorCode.getHttpStatus());
    }

    private String convertMapToJson(Map<String, String> map) {
        StringBuilder json = new StringBuilder("{");
        for (Map.Entry<String, String> entry : map.entrySet()) {
            json.append("\"").append(entry.getKey()).append("\":\"").append(entry.getValue()).append("\",");
        }
        json.deleteCharAt(json.length() - 1);
        json.append("}");
        return json.toString();
    }

    /**
     * StackTrace 상위 10줄까지만 로그 찍도록 custom 한 메서드
     */
    private String printErrorStackTrace(Exception e) {
        StringBuilder sb = new StringBuilder();

        String message = e.getMessage();
        if (StringUtils.isNotBlank(message)) {
            sb.append("\n \t *** 원인 => " + message);
        }
        if (e instanceof RequestValidationException) {
            String additionalErrorMsg = ((RequestValidationException) e).getAdditionalErrorMsg();
            if (StringUtils.isNotBlank(additionalErrorMsg)) {
                sb.append("\n\t *** 추가메세지 => " + additionalErrorMsg);
            }
        }
        sb.append("\n \t");
        StackTraceElement[] stackTrace = e.getStackTrace();
        int lastIdx = stackTrace.length;
        if (lastIdx > 10) {
            lastIdx = 10;
        }
        for (int i = 0; i < lastIdx; i++) {
            StackTraceElement stackTraceElement = stackTrace[i];
            sb.append(stackTraceElement + "\n \t");
        }
        return sb.toString();
    }
}
