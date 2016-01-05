package com.tqmall.search.common.utils;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.FastDateFormat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.ParseException;
import java.util.Date;

/**
 * Created by xing on 16/1/5.
 * {@link java.util.Date} 的{@link ComparableStrValueConvert}实现
 */
public class DateStrValueConvert implements ComparableStrValueConvert<Date> {

    private static final Logger log = LoggerFactory.getLogger(DateStrValueConvert.class);

    public static final DateStrValueConvert INSTANCE = new DateStrValueConvert("yyyy-MM-dd HH:mm:ss");

    public static Date dateConvert(String input) {
        return INSTANCE.convert(input);
    }

    public static String dateFormat(Date date) {
        return INSTANCE.format(date);
    }

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
    public int compare(Date o1, Date o2) {
        return o1.compareTo(o2);
    }

    @Override
    public Date convert(String input) {
        if (StringUtils.isEmpty(input)) return null;
        try {
            return dateFormat.parse(input);
        } catch (ParseException e) {
            log.error("解析时间, 入参input: " + input + "不符合Pattern: " + dateFormat.getPattern()
                    + ", message: " + e.getMessage());
            return null;
        }
    }

    public String format(Date date) {
        return dateFormat.format(date);
    }

    public long timestamp(String input) {
        Date date = convert(input);
        return date == null ? 0l : date.getTime();
    }

    public FastDateFormat getDateFormat() {
        return dateFormat;
    }

}
