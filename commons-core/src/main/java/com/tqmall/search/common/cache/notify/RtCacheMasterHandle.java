package com.tqmall.search.common.cache.notify;

import java.util.Map;

/**
 * Created by xing on 15/10/26.
 * 缓存的数据库对象实时更新
 * 各个接口的调用都在一个线程中顺序调用
 */
public interface RtCacheMasterHandle<K, V> {
    /**
     * 单线程更新,无需考虑多线程问题
     */
    void finishUpdate();

    /**
     * 过滤数据是否有效
     * @param dataMap  db中的数据
     * @return true: 有效, false: 无效
     */
    boolean filter(Map<String, String> dataMap);

    /**
     * @return can't null
     */
    K initKey(Map<String, String> dataMap);

    V initValue(Map<String, String> dataMap);

    /**
     * @param key 需要更新的key
     * @param newVal 最新val,如果newVal == null, 意味着删除操作
     * @return 是否已经处理
     */
    boolean onMasterHandle(K key, V newVal);

}
