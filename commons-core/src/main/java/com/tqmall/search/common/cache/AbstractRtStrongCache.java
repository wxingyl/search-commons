package com.tqmall.search.common.cache;

import com.tqmall.search.common.utils.StrValueConvert;

import java.util.Map;

/**
 * Created by xing on 15/12/1.
 * 需要处理实时变更的强引用缓存
 */
public abstract class AbstractRtStrongCache<K, V> extends AbstractStrongCache<K, V>
        implements RtCacheMasterHandle<K, V> {

    protected abstract StrValueConvert<K> getKeyValueConvert();

    /**
     * 对于Key, 我们默认读取主键"id"的值
     * 当然可以自己执行,那就重写该方法
     */
    @Override
    public K initKey(Map<String, String> dataMap) {
        return getKeyValueConvert().convert(dataMap.get("id"));
    }

    @Override
    public boolean filter(Map<String, String> dataMap) {
        return "N".equals(dataMap.get("is_deleted"));
    }

    @Override
    public boolean onMasterHandle(K key, V newVal) {
        //还没有使用,那就不用更新了,直接返回就得了, 不用考虑Slave机器的情况
        return !(key == null || !initialized()) && updateValue(key, newVal);
    }

}
