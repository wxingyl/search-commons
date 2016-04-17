package com.tqmall.search.commons.utils;

import com.tqmall.search.commons.lang.SmallDateFormat;
import com.tqmall.search.commons.lang.StrValueConvert;
import org.apache.commons.lang3.time.FastDateFormat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.ParseException;
import java.util.Date;

/**
 * Created by xing on 16/1/5.
 * {@link Date} 的{@link StrValueConvert}实现
 */
public class DateStrValueConvert implements SmallDateFormat {

    private static final Logger log = LoggerFactory.getLogger(DateStrValueConvert.class);

    public static final DateStrValueConvert INSTANCE = new DateStrValueConvert("yyyy-MM-dd HH:mm:ss");

    public static Date dateConvert(String input) {
        return INSTANCE.convert(input);
    }

    /**
     * @param date 日期对象
     * @return {@link #INSTANCE}格式的字符串
     */
    public static String dateFormat(Date date) {
        return INSTANCE.format(date);
    }

    /**
     * @param input 时间字符串
     * @return 对应的时间戳, 单位为ms
     */
    public static long dateTimestamp(String input) {
        return INSTANCE.timestamp(input);
    }

    private final FastDateFormat dateFormat;

    public DateStrValueConvert(String pattern) {
        this(FastDateFormat.getInstance(pattern));
    }

    public DateStrValueConvert(FastDateFormat dateFormat) {
        this.dateFormat = dateFormat;
    }

    @Override
    public Date convert(String input) {
        if (SearchStringUtils.isEmpty(input)) return null;
        try {
            return dateFormat.parse(input);
        } catch (ParseException e) {
            log.error("解析时间, 入参input: " + input + "不符合Pattern: " + dateFormat.getPattern()
                    + ", message: " + e.getMessage());
            return null;
        }
    }

    /**
     * @param date 日期对象
     * @return 对应格式的字符串
     */
    @Override
    public String format(Date date) {
        return dateFormat.format(date);
    }

    /**
     * @param input 时间字符串
     * @return 对应的时间戳, 单位为ms
     */
    public long timestamp(String input) {
        Date date = convert(input);
        return date == null ? 0L : date.getTime();
    }

    public FastDateFormat getDateFormat() {
        return dateFormat;
    }

}
