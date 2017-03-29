package com.utils;

import java.util.regex.Pattern;

/** 
 * 正则表达式类
 * @author wangfan 
 * @version 创建时间：2016年12月23日 上午10:42:03 
 */
public class Regex {

	private static String dateTimeRegex=".*(((01[0-9]{2}|0[2-9][0-9]{2}|[1-9][0-9]{3})-(0?[13578]|1[02])-(0?[1-9]|[12]\\d|3[01]))|((01[0-9]{2}|0[2-9][0-9]{2}|[1-9][0-9]{3})-(0?[13456789]|1[012])-(0?[1-9]|[12]\\d|30))|((01[0-9]{2}|0[2-9][0-9]{2}|[1-9][0-9]{3})-0?2-(0?[1-9]|1\\d|2[0-8]))|(((1[6-9]|[2-9]\\d)(0[48]|[2468][048]|[13579][26])|((04|08|12|16|[2468][048]|[3579][26])00))-0?2-29)) (20|21|22|23|[0-1]?\\d):[0-5]?\\d:[0-5]?\\d.*";
	private static String dateRegex=".*月.*日.*";
	/**
	 * 判断是否为合法的日期时间字符串
	 * @param timeString
	 * @return 
	 */
	public static boolean isDateTime(String dateTimeString){
		return Pattern.matches(dateTimeRegex, dateTimeString);
	}
	/**
	 * 判断是否为合法的日期字符串
	 * @param dateString
	 * @return
	 */
	public static boolean isDate(String dateString){
		return Pattern.matches(dateRegex, dateString);
	}
}
