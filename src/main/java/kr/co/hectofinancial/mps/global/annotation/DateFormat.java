package kr.co.hectofinancial.mps.global.annotation;

import kr.co.hectofinancial.mps.global.validator.DateFormatValidator;

import javax.validation.Constraint;
import javax.validation.Payload;
import java.lang.annotation.*;

/**
 * API 요청 값내 reqDt, reqTm 등 일자,시간 값이 속성값인 pattern 양식에 맞게 인입되었는지 확인하는 어노테이션
 */
@Documented
@Constraint(validatedBy = DateFormatValidator.class)
@Target({ElementType.METHOD, ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
public @interface DateFormat {
    String message() default "";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
    String pattern();
}
