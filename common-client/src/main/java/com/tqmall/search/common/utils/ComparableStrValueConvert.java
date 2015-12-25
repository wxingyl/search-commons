package com.tqmall.search.common.utils;

import java.util.Comparator;

/**
 * Created by xing on 15/12/24.
 * 转换结果是可以比较的StrValueConvert
 */
public interface ComparableStrValueConvert<T extends Comparable<T>> extends StrValueConvert<T>, Comparator<T> {

}
