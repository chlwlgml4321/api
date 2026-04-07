package kr.co.hectofinancial.mps.global.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * API 요청 파라미터 내 필수값 및 pktHash 에 포함되는지 확인하기 위한 어노테이션
 * (해당 어노테이션이 붙은 필드의 order 속성을 이용하여 pktHash를 생성한다)
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface HashField {
    int order() default Integer.MAX_VALUE;

}
