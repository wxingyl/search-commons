package com.tqmall.search.common.utils;

/**
 * Created by xing on 15/10/24
 * 字符串转化为对应T类型
 */
public interface StrValueConvert<T> {

    T convert(String input);

}
