package kr.co.hectofinancial.mps.global.util;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Calendar;
import java.util.Date;

public class CustomDateTimeUtil {
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyyMMdd");
    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HHmmss");

    private String dateTime;
    private String date;
    private String time;

    public CustomDateTimeUtil() {
        String yyyyMMddhhmiss = new SimpleDateFormat("yyyyMMddHHmmss").format(Calendar.getInstance().getTime());
        this.dateTime = yyyyMMddhhmiss;
        this.date = yyyyMMddhhmiss.substring(0, 8);
        this.time = yyyyMMddhhmiss.substring(8);
    }

    /**
     *
     * @return YYYYMMDDHHMMSS 형식의 14자리 DateTime 문자열
     */
    public String getDateTime() {
        return dateTime;
    }

    /**
     * @return YYYYMMDD 형식의 8자리 Date
     */
    public String getDate() {
        return date;
    }

    /**
     * @return HHMMSS 형식의 6자리 Time
     */
    public String getTime() {
        return time;
    }

    /**
     * 유효한 날짜판단
     */
    public boolean isValidDate(String date){
        try{
            LocalDate.parse(date, FORMATTER);
            return  true;
        }catch (DateTimeParseException e){
            return false;
        }
    }

    public boolean isValidTime(String time) {
        try {
            LocalTime.parse(time, TIME_FORMATTER);
            return true;
        } catch (DateTimeParseException e) {
            return false;
        }
    }
    public boolean isValidPeriod(String period) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMM");
        sdf.setLenient(false);//날짜 엄격하게 체크

        try{
            Date parsed = sdf.parse(period);
            if (!period.equals(sdf.format(parsed))) {
                return false;
            }

            Calendar inputCal = Calendar.getInstance();
            inputCal.setTime(parsed);

            Calendar nowCal = Calendar.getInstance(); // 오늘
            int inputYearMonth = inputCal.get(Calendar.YEAR) * 100 + inputCal.get(Calendar.MONTH) + 1;
            int nowYearMonth = nowCal.get(Calendar.YEAR) * 100 + nowCal.get(Calendar.MONTH) + 1;

            // input이 이번 달보다 크면 invalid
            return inputYearMonth <= nowYearMonth;
        } catch (ParseException e) {
            return false;
        }
    }
}
