package kr.co.hectofinancial.mps.global.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 요청/응답 파라미터 내 AES 암호화 대상 필드에 사용하는 어노테이션
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface EncField {
    boolean nullable() default false;
}
