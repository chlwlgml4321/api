package kr.co.hectofinancial.mps.global.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * RequestValidation AOP 에서 요청파라미터를 로그로 찍는데, 그때 제외시킬 항목에 붙이는 어노테이션
 * (주로 ~~RequestDto 내 customerDto, requestIP 등 controller - service - repository 레이어에 들고다니려고 선언해둔 변수 제외용)
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface NotLoggableParam {
}
