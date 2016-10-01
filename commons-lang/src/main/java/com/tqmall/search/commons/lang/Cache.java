package com.tqmall.search.commons.lang;

import java.util.Map;

/**
 * Created by xing on 15/12/23.
 * 缓存接口定义
 */
public interface Cache<K, V> {

    V getValue(K key);

    Map<K, V> getValue(Iterable<K> keys);

    /**
     * 清除指定key的缓存
     *
     * @return 是否清除成功
     */
    boolean invalid(K key);

    /**
     * 清除一系列指定key的缓存
     *
     * @return 清除成功的缓存数
     */
    int invalid(Iterable<K> keys);

    /**
     * 将缓存中的数据重新从数据源加载更新
     * 注意: 只更新在缓存中存在的数据
     *
     * @return 更新后缓存中的对象个数
     */
    int reload();

    /**
     * 清楚缓存中的内容
     */
    void clear();
}
