package kr.co.hectofinancial.mps.global.validator;

import kr.co.hectofinancial.mps.global.annotation.DateFormat;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import java.text.ParseException;
import java.text.SimpleDateFormat;

/**
 * @DateFormat 어노테이션 사용시, 속성값인 pattern 양식에 맞게 파라미터 값이 들어왔는지 검증하는 validator
 */
public class DateFormatValidator implements ConstraintValidator<DateFormat, String> {
    private String pattern;
    @Override
    public void initialize(DateFormat dateFormat) {
        this.pattern = dateFormat.pattern();
    }

    @Override
    public boolean isValid(String value, ConstraintValidatorContext constraintValidatorContext) {
        if (value == null || value.isEmpty()) {
            return true;//null값은 검증하지 않음
        }
        SimpleDateFormat sdf = new SimpleDateFormat(pattern);
        sdf.setLenient(false);//날짜 엄격하게 체크

        try{
            sdf.parse(value);
            return true;//날짜형식 맞으면 true
        } catch (ParseException e) {
            return false;
        }
    }
}
