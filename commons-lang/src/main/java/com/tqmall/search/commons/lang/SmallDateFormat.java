package com.tqmall.search.commons.lang;

import java.util.Date;

/**
 * date 16/4/16 下午3:28
 * 简单的时间转换, 支持字符串和{@link Date}之间互转
 *
 * @author xing
 */
public interface SmallDateFormat extends StrValueConvert<Date> {

    String format(Date date);

}
