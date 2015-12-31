package com.tqmall.search.common.utils;

import java.util.Comparator;

/**
 * Created by xing on 15/12/24.
 * 转换结果是可以比较的StrValueConvert, 大部分{@link Number}的实现类都是可以比较的
 * {@link StrValueConverts#getConvert(Class)}返回的都是该实现
 */
public interface ComparableStrValueConvert<T extends Comparable<T>> extends StrValueConvert<T>, Comparator<T> {

}
