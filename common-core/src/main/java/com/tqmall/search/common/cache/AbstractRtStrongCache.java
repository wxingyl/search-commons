package com.tqmall.search.common.cache;

import com.tqmall.search.common.utils.StrValueConvert;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created by xing on 15/12/1.
 * 需要处理实时变更的强引用缓存
 */
public abstract class AbstractRtStrongCache<K, V> extends AbstractStrongCache<K, V> implements RtCacheMasterHandle<K, V>, RtCacheSlaveHandle {

    private List<K> updateKeyRecordList = new ArrayList<>();

    public AbstractRtStrongCache() {
        RtCacheManagers.INSTANCE.getReceive().registerHandler(this);
    }

    protected abstract StrValueConvert<K> getKeyValueConvert();

    protected final void appendChangedKey(K key) {
        if (!updateKeyRecordList.contains(key)) {
            updateKeyRecordList.add(key);
        }
    }

    protected final void clearChangedKey() {
        if (!updateKeyRecordList.isEmpty()) {
            updateKeyRecordList.clear();
        }
    }

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
    public void onMasterHandle(K key, V newVal) {
        if (key == null || !initialized()) {
            //还没有使用,那就不用更新了,直接返回就得了
            return;
        }
        appendChangedKey(key);
        updateValue(key, newVal);
    }

    @Override
    public void finishUpdate() {
        try {

        } finally {
            clearChangedKey();
        }
    }

    @Override
    public void onSlaveHandle(List<String> keys) {
        if (!initialized() || keys == null || keys.isEmpty()) {
            //没必要更新
            return;
        }
        final StrValueConvert<K> keyConvert = getKeyValueConvert();
        List<K> keyList = new ArrayList<>();
        for (String k : keys) {
            keyList.add(keyConvert.convert(k));
        }

    }
}
