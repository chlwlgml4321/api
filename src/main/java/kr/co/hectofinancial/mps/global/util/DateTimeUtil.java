package kr.co.hectofinancial.mps.global.util;

import kr.co.hectofinancial.mps.global.constant.KycKindCd;
import kr.co.hectofinancial.mps.global.mtms.MonitAgent;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;
import org.springframework.util.ObjectUtils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.temporal.TemporalAdjusters;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

@Component
public class DateTimeUtil {

    public static final String YYYYMMDD = "yyyyMMdd";
    public static final String HHMMSS = "HHmmss";
    public static final String YYYYMMDDHHMMSS = "yyyyMMddHHmmss";
    public static final String DATETIME_YYYYMMDD = "yyyy-MM-dd";
    public static final String DATETIME_YYYYMMDDHHMMSS = "yyyy-MM-dd HH:mm:ss";
    public static final String DATETIME_HHMMSS = "HH:mm:ss";

    /**
     * @param date
     * @return localDateTime
     * @description Date type -> LocalDateTime type
     */
    public static LocalDateTime convertToLocalDateTime(Date date) {
        return date.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();
    }

    /**
     *
     * @param dateStr
     * @return
     * @description String dateStr -> LocalDateTime type
     */
    public static LocalDateTime convertStringToLocalDate(String dateStr) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd");
        try {
            // LocalDateTime.of로 LocalDate를 LocalDateTime으로 변환
            return LocalDateTime.of(LocalDate.parse(dateStr, formatter), LocalTime.MIDNIGHT);
        } catch (DateTimeParseException e) {
            return null;
        }
    }
    /**
     *
     * @param dateStr
     * @return
     * @description String dateTimeStr -> LocalDateTime type
     */
    public static LocalDateTime convertStringToLocalDateTime(String dateStr) {
        if (StringUtils.isEmpty(dateStr)) {
            dateStr = new CustomDateTimeUtil().getDateTime();
        }
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");
        try {
            // LocalDateTime.of로 LocalDate를 LocalDateTime으로 변환
            return LocalDateTime.parse(dateStr, formatter);
        } catch (DateTimeParseException e) {
            return null;
        }
    }

    /**
     * 현재 일자 시간을 리턴한다.
     * @param format_str - yyyy MM dd HH mm ss SSS
     * @return Current Date time - strNow
     */
    public static String getCurDtim(String format_str) {
        long now = System.currentTimeMillis();
        SimpleDateFormat sdfNow = new SimpleDateFormat(format_str);
        return sdfNow.format(new Date(now));
    }

    /**
     * 날짜정보에 원하는 일만큼 추가
     * @param date : yyyyMMdd 형식의 날짜
     * @param day : 추가할 일수
     * @return
     */
    public static String addDate(String date, int day) {

        SimpleDateFormat sdfNow = new SimpleDateFormat("yyyyMMdd");
        Calendar c = Calendar.getInstance();
        Date result = null;

        Date before;
        try {
            before = sdfNow.parse(date);

            c.setTime(before);
            c.add(Calendar.DATE, day);
            result = c.getTime();
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return sdfNow.format(result);
    }

    /**
     * 날짜정보에 원하는 년도 만큼 추가
     * @param date : yyyyMMdd 형식의 날짜
     * @param year : 추가할 년도수
     * @return
     */
    public static String addYear(String date, int year) {

        SimpleDateFormat sdfNow = new SimpleDateFormat("yyyyMMdd");
        Calendar c = Calendar.getInstance();
        String result = null;

        try {
            Date before = sdfNow.parse(date);

            c.setTime(before);
            c.add(Calendar.YEAR, year);
            result = sdfNow.format(c.getTime());
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return result;
    }


    public static String convertDateToString(Date date) {
        return new SimpleDateFormat("yyyyMMdd", Locale.KOREA).format(date);
    }

    /**
     * yyyymm 형식으로 넘기면 해당 월의 마지막일을 계산해줌
     * @param period
     * @return
     */
    public static String calculateEndDate(String period) {
        // 입력 형식 지정
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMM");

        // period를 LocalDate로 변환 (해당 달의 첫 번째 일자)
        LocalDate firstDate = LocalDate.parse(period + "01", DateTimeFormatter.ofPattern("yyyyMMdd"));

        // 해당 달의 마지막 일자 계산
        LocalDate lastDate = firstDate.with(TemporalAdjusters.lastDayOfMonth());

        // 원하는 형식으로 출력 (yyyyMMdd)
        DateTimeFormatter outputFormatter = DateTimeFormatter.ofPattern("yyyyMMdd");
        String endDate = lastDate.format(outputFormatter);

        return endDate;
    }

    /**
     * 거래 시작 시간을 long type 의 timeMills 로 넘기면, 소요시간 계산해서 string type 으로 반환
     * @param startTime
     * @return
     */
    public static String getElapsedTimeStr(long startTime) {
        long elapsedTime = System.currentTimeMillis() - startTime;
        String elapsedTimeStr = String.valueOf(elapsedTime);
        return elapsedTimeStr;
    }

    /**
     *
     * @param localDateTime
     * @return yyyyMMddHH24mmss type 의 string
     */
    public static String convertStringDateTime(LocalDateTime localDateTime) {
        if (ObjectUtils.isEmpty(localDateTime)) {
            return "";
        }
        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");
        return localDateTime.format(dateTimeFormatter);
    }

    /**
     * kyc 종류별로 만료일자 계산, (CDD:3년 , EDD:1년)
     * @param kycKindCd
     * @param kycExecDt
     * @return
     */
    public static String getKycExpiredDate(String kycKindCd, String kycExecDt) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd");
        try {
            LocalDate date = LocalDate.parse(kycExecDt, formatter);
            int year = 1;
            if (kycKindCd.equals(KycKindCd.CDD.getKycKindCd())) {
                //CDD = 3년, EDD = 1년
                year = 3;
            }
            LocalDate expiredDate = date.plusYears(year).minusDays(1);
            return expiredDate.format(formatter);
        } catch (Exception e) {
            MonitAgent.sendMonitAgent("E-999", "KYC 만료일자 계산 오류 KYC수행일자:" + kycExecDt);
            e.printStackTrace();
            return "";
        }
    }

    public static LocalDateTime getCurrentDateTime() {
        return LocalDateTime.now();
    }

    public static LocalDate getCurrentDate() {
        return LocalDate.now();
    }

    public static LocalTime getCurrentTime() {
        return LocalTime.now();
    }

    public static LocalDateTime toLocalDateTime(String datetime, String pattern) {
        try {
            return LocalDateTime.parse(datetime, DateTimeFormatter.ofPattern(pattern));
        } catch (NullPointerException | DateTimeParseException e) {
            return null;
        }
    }

    public static LocalDate toLocalDate(String date, String pattern) {
        try {
            return LocalDate.parse(date, DateTimeFormatter.ofPattern(pattern));
        } catch (NullPointerException | DateTimeParseException e) {
            return null;
        }
    }

    public static LocalTime toLocalTime(String time, String pattern) {
        try {
            return LocalTime.parse(time, DateTimeFormatter.ofPattern(pattern));
        } catch (NullPointerException | DateTimeParseException e) {
            return null;
        }
    }

    public static String fromLocalDateTime(LocalDateTime dateTime, String pattern) {
        try {
            return dateTime.format(DateTimeFormatter.ofPattern(pattern));
        } catch (NullPointerException | DateTimeParseException e) {
            return null;
        }
    }

    public static String fromLocalDate(LocalDate date, String pattern) {
        try {
            return date.format(DateTimeFormatter.ofPattern(pattern));
        } catch (NullPointerException | DateTimeParseException e) {
            return null;
        }
    }

    public static String fromLocalTime(LocalTime time, String pattern) {
        try {
            return time.format(DateTimeFormatter.ofPattern(pattern));
        } catch (NullPointerException | DateTimeParseException e) {
            return null;
        }
    }
}
