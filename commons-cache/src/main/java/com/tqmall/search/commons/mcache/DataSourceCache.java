package com.tqmall.search.commons.mcache;

import com.tqmall.search.commons.lang.Cache;

import java.util.Map;

/**
 * date 16/4/19 上午10:22
 * 从数据源获取, 并且确定的数据需要加载到缓存
 * 对于数据库中小批量数据缓存用处比较多了~~~~~~
 *
 * @author 尚辰
 */
public interface DataSourceCache<K, V> extends Cache<K, V> {

    /**
     * 获取所有应该加载到缓存的数据, 并不是当前缓存中所有的数据
     */
    Map<K, V> getAllCache();

    /**
     * 是否已经完成初始化, 有的缓存实现是不关心该函数返回的, 比如通过redis缓存,
     * 但是通过内存做小数据量缓存的就需要了, 需要知道到底初始化了没
     *
     * @return 是否已完成初始化
     */
    boolean initialized();
}
