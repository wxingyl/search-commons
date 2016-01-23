package com.tqmall.search.commons.cache;

import java.util.Map;

/**
 * Created by xing on 15/12/23.
 * 强引用缓存接口定义
 */
public interface StrongCache<K, V> {

    V getValue(K key);

    Map<K, V> getAllCache();

    boolean initialized();

    int reload();

    void clear();
}
