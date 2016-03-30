package com.tqmall.search.commons.lang;

import java.util.Map;

/**
 * Created by xing on 15/12/23.
 * 缓存接口定义
 */
public interface Cache<K, V> {

    V getValue(K key);

    Map<K, V> getValue(Iterable<K> keys);

    Map<K, V> getAllCache();

    boolean initialized();

    int reload();

    void clear();
}
