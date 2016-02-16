package com.tqmall.search.commons.lang;

/**
 * Created by xing on 16/2/16.
 * 这个接口在jdk1.8有的, 砸门现在是1.7, 先定义一个吧
 */
public interface Function<T, R> {

    /**
     * Applies this function to the given argument.
     *
     * @param t the function argument
     * @return the function result
     */
    R apply(T t);
}
