package com.tqmall.search.commons.lang;

/**
 * Created by xing on 16/2/3.
 * 这个接口在jdk1.8有的, 砸门现在是1.7, 先定义一个吧
 */
public interface Supplier<T> {
    /**
     * Retrieves an instance of the appropriate type. The returned object may or
     * may not be a new instance, depending on the implementation.
     *
     * @return an instance of the appropriate type
     */
    T get();
}

