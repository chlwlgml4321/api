package kr.co.hectofinancial.mps.global.error.controller;

import kr.co.hectofinancial.mps.global.error.ErrorCode;
import kr.co.hectofinancial.mps.global.error.ErrorResponse;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.boot.web.error.ErrorAttributeOptions;
import org.springframework.boot.web.servlet.error.ErrorAttributes;
import org.springframework.boot.web.servlet.error.ErrorController;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.context.request.WebRequest;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;

/**
 * WebRequest 오류 일 경우, 화면에서 json 형태로 오류코드, 오류메세지 확인 가능하도록 해주는 class
 */
@Controller
@Slf4j
public class CustomErrorController implements ErrorController {
    private final ErrorAttributes errorAttributes;

    public CustomErrorController(ErrorAttributes errorAttributes) {
        this.errorAttributes = errorAttributes;
    }

    @RequestMapping("/error")
    public ResponseEntity<Object> handleError(HttpServletRequest request, WebRequest webRequest) {
        Map<String, Object> body = errorAttributes.getErrorAttributes(webRequest, ErrorAttributeOptions.of(ErrorAttributeOptions.Include.MESSAGE));
        log.info("/error from webRequest {}", body);

        Integer statusCode = (Integer) request.getAttribute("javax.servlet.error.status_code");
        HttpStatus status = HttpStatus.INTERNAL_SERVER_ERROR;
        if (statusCode != null) {
            status = HttpStatus.valueOf(statusCode);
        }

        ErrorResponse of = ErrorResponse.of(ErrorCode.SERVER_ERROR_CODE.getErrorCode(), ErrorCode.SERVER_ERROR_CODE.getErrorMessage());
        return new ResponseEntity<>(of,status);
    }
}
