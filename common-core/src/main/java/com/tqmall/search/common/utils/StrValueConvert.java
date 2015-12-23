package com.tqmall.search.common.utils;

import java.util.Comparator;

/**
 * Created by xing on 15/10/24
 * 字符串转化为对应T类型
 */
public interface StrValueConvert<T> extends Comparator<T> {

    T convert(String input);

}
