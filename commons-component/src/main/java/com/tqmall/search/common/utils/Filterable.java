package com.tqmall.search.common.utils;

import com.google.common.base.Predicate;

/**
 * Created by xing on 16/1/1.
 * 可以过滤, 定义{@link #setFilter(Predicate)}, 指定过滤器
 */
public interface Filterable<T> {

    /**
     * 设定过滤器, 过滤器返回true则认为有用, 返回false则认为无用
     */
    void setFilter(Predicate<T> predicate);
}
