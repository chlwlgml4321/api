package kr.co.hectofinancial.mps.global.util;

import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * 
 * @projectName : KorailPay-webapi
 * @packageName : kr.co.settlebank.easycash.util
 * @className   : TimeUtils.java
 * @author      : BYUNGSEOK-SHIN
 * @since       : 2019. 5. 21.
 * @desc        : 
 * <pre>
 * DATE					AUTHOR					VERSION			NOTE
 * --------------------------------------------------------------------------
 * 2019. 5. 21.		BYUNGSEOK-SHIN			Create
 *
 * </pre>
 */
public class TimeUtils
{
	/** yyyyMMddHHmmss Date Format */
	private static final SimpleDateFormat yyyyMMddHHmmss = new SimpleDateFormat("yyyyMMddHHmmss");
	/** yyMMddHHmmss Date Format */
	private static final SimpleDateFormat yyMMddHHmmss = new SimpleDateFormat("yyMMddHHmmss");
	/** MMddHHmmss Date Format */
	private static final SimpleDateFormat MMddHHmmss = new SimpleDateFormat("MMddHHmmss");
	/** yyyyMMdd Date Format */
	private static final SimpleDateFormat yyyyMMdd = new SimpleDateFormat("yyyyMMdd");
	/** yyyy Date Format */
	private static final SimpleDateFormat yyyy = new SimpleDateFormat("yyyy");
	/** yyMMdd */
	private static final SimpleDateFormat yyMMdd = new SimpleDateFormat("yyMMdd");
	/** HHmmss Date Format */
	private static final SimpleDateFormat HHmmss = new SimpleDateFormat("HHmmss");
	
	private static final SimpleDateFormat SSSSSS = new SimpleDateFormat("SSSSSS");
	
	private static final SimpleDateFormat MMddKR = new SimpleDateFormat("MM월dd일");
	
	/**
	 * get yyyy Format 
	 * @param date
	 * @return String
	 */
	public static final synchronized String getyyyy(Date date){
		return yyyy.format(date);
	}
	
	/**
	 * get yyyyMMddHHmmss Format 
	 * @return String 
	 */
	public static final synchronized String getyyyyMMddHHmmss(){
		return yyyyMMddHHmmss.format(new Date());
	}
	
	/**
	 * get yyyyMMddHHmmss Format 
	 * @param date
	 * @return String
	 */
	public static final synchronized String getyyyyMMddHHmmss(Date date){
		return yyyyMMddHHmmss.format(date);
	}
	
	/**
	 * get yyyyMMddHHmmss Format 
	 * @param date
	 * @return String
	 */
	public static final synchronized String getyyyyMMddHHmmss(long date){
		return yyyyMMddHHmmss.format(new Date(date));
	}
	
	/**
	 * get yyMMddHHmmss Format 
	 * @return String 
	 */
	public static final synchronized String getyyMMddHHmmss(Date date){
		return yyMMddHHmmss.format(date);
	}
	
	/**
	 * get yyMMddHHmmss Format 
	 * @return String 
	 */
	public static final synchronized String getyyMMddHHmmss(){
		return yyMMddHHmmss.format(new Date());
	}
	
	/**
	 * get MMddHHmmss Format 
	 * @return String 
	 */
	public static final synchronized String getMMddHHmmss(Date date){
		return MMddHHmmss.format(date);
	}
	
	/**
	 * get MMddHHmmss Format 
	 * @return String 
	 */
	public static final synchronized String getMMddHHmmss(){
		return MMddHHmmss.format(new Date());
	}
	
	
	/**
	 * get yyyyMMdd Format 
	 * @return String 
	 */
	public static final synchronized String getyyyyMMdd(Date date){
		return yyyyMMdd.format(date);
	}
	
	/**
	 * get yyyyMMdd Format 
	 * @return String 
	 */
	public static final synchronized String getyyyyMMdd(){
		return yyyyMMdd.format(new Date());
	}
	
	/**
	 * get yyyyMMdd Format 
	 * @return String 
	 */
	public static final synchronized String getyyMMdd(){
		return yyMMdd.format(new Date());
	}
	
	/**
	 * get HHmmss
	 * @return String
	 */
	public static final synchronized String getHHmmss(){
		return HHmmss.format(new Date());
	}
	
	/**
	 * get HHmmss
	 * @return String
	 */
	public static final synchronized String getSSSSSS(){
		return SSSSSS.format(new Date());
	}
	
	/**
	 * get HHmmss
	 * @param date
	 * @return String
	 */
	public static final synchronized String getHHmmss(Date date){
		return HHmmss.format(date);
	}
	
	/**
	 * getyyMMdd
	 * @param date
	 * @return String
	 */
	public static final String getyyMMdd(Date date){
		return yyMMdd.format(date);
	}
	
	
	/**
	 * @param date
	 * @return String
	 */
	public static final String getMMddKR(Date date){
		return MMddKR.format(date);
	}
	
	//--------------------------------------------------------------------- substring
	
	/**
	 * getyyMMddHHmmss
	 * @param yyyyMMddHHmmss
	 * @return String
	 */
	public static final String getyyMMddHHmmss(String yyyyMMddHHmmss){
		return yyyyMMddHHmmss.substring(2);
	}
	
	/**
	 * getMMddHHmmss
	 * @param yyyyMMddHHmmss
	 * @return String
	 */
	public static final String getMMddHHmmss(String yyyyMMddHHmmss){
		return yyyyMMddHHmmss.substring(4);
	}
	/**
	 * getHHmmss
	 * @param yyyyMMddHHmmss
	 * @return String
	 */
	public static final String getHHmmss(String yyyyMMddHHmmss){
		return yyyyMMddHHmmss.substring(8);
	}
	/**
	 * getyyMMdd
	 * @param yyyyMMddHHmmss
	 * @return String
	 */
	public static final String getyyMMdd(String yyyyMMddHHmmss){
		return yyyyMMddHHmmss.substring(2,8);
	}
	
	/**
	 * getDayBetween
	 * @param now
	 * @param yyyyMMdd
	 * @return int
	 * @throws ParseException
	 */
	public static final int getDayBetween(Date now,String yyyyMMdd) throws ParseException {
		SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
		Date d = sdf.parse(yyyyMMdd);
		d.getTime();
		long v = now.getTime() - d.getTime();
		return (int)( v / (1000*60*60*24) );
	}
	
	/**
	 * getDayBetween
	 * @param nowyyyyMMdd
	 * @param yyyyMMdd
	 * @return int
	 * @throws ParseException
	 */
	public static final int getDayBetween(String nowyyyyMMdd,String yyyyMMdd) throws ParseException {
		SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
		Date now = sdf.parse(nowyyyyMMdd);
		Date d = sdf.parse(yyyyMMdd);
		long v = now.getTime() - d.getTime();
		return (int)( v / (1000*60*60*24) );
	}
	
	public static Timestamp getTimestampWithSpan(Timestamp sourceTS, long day) throws Exception {
		Timestamp targetTS = null;
		
		if (sourceTS != null) {
			targetTS = new Timestamp(sourceTS.getTime() + (day * 1000 * 60 * 60 * 24));
		}

		return targetTS;
	}
}