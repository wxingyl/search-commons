package com.tqmall.search.util;

import com.tqmall.search.common.utils.DateStrValueConvert;
import org.apache.commons.lang3.time.DateFormatUtils;

import java.util.Calendar;
import java.util.Date;

/**
 * 日期的公用函数
 * Created by wcong on 14/10/29.
 */
public class DateFormat {

    /**
     * 一分钟的毫秒数
     */
    public final static long MINUTE_MM = 1000 * 60L;
    /**
     * 一小时的毫秒数
     */
    public final static long HOUR_MM = 1000 * 60 * 60L;
    /**
     * 一天的毫秒数
     */
    public final static long DAY_MM = 1000 * 60 * 60 * 24L;

    private static final DateStrValueConvert DAY_DATE_FORMAT = new DateStrValueConvert(DateFormatUtils.ISO_DATE_FORMAT);

    public static String makeStringFromDate(Date date) {
        return DAY_DATE_FORMAT.format(date);
    }

    public static Date makeDateFromString(String dateStr) {
        return DAY_DATE_FORMAT.convert(dateStr);
    }

    /**
     * 返回指定date当天的凌晨时间
     */
    public static Date weeHouse(Date date) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        int hour = cal.get(Calendar.HOUR_OF_DAY);
        int minute = cal.get(Calendar.MINUTE);
        int second = cal.get(Calendar.SECOND);
        long millisecond = hour * HOUR_MM + minute * MINUTE_MM + second * 1000L;
        cal.setTimeInMillis(cal.getTimeInMillis() - millisecond);
        return cal.getTime();
    }

    /**
     * 返回指定date的月初时间
     */
    public static Date monthEarly(Date date) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        int day = cal.get(Calendar.DAY_OF_MONTH);
        int hour = cal.get(Calendar.HOUR_OF_DAY);
        int minute = cal.get(Calendar.MINUTE);
        int second = cal.get(Calendar.SECOND);
        long millisecond = (day - 1) * DAY_MM + hour * HOUR_MM + minute * MINUTE_MM + second * 1000L;
        cal.setTimeInMillis(cal.getTimeInMillis() - millisecond);
        return cal.getTime();
    }

    /**
     * 返回多少天之前的日期, 有时分秒
     */
    public static String someDaysAgo(int days) {
        Calendar cal = Calendar.getInstance();
        long curTime = System.currentTimeMillis();
        cal.setTimeInMillis(curTime);
        int hour = cal.get(Calendar.HOUR_OF_DAY);
        int minute = cal.get(Calendar.MINUTE);
        int second = cal.get(Calendar.SECOND);
        long dis = days * DAY_MM + hour * HOUR_MM + minute * MINUTE_MM + second * 1000L;
        return DateStrValueConvert.dateFormat(new Date(curTime - dis));
    }

    public static String nowMonthEarly() {
        return DateStrValueConvert.dateFormat(monthEarly(new Date()));
    }

}