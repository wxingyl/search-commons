package com.tqmall.search.common.utils;

import com.google.common.base.Predicate;

/**
 * Created by xing on 16/1/1.
 * 可以过滤, 定义{@link #setFilter(Predicate)}, 指定过滤器
 */
public interface Filterable<T> {

    void setFilter(Predicate<T> predicate);
}
