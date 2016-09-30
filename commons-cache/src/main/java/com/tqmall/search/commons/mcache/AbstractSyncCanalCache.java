package com.tqmall.search.commons.mcache;

import com.google.common.base.Function;
import com.google.common.collect.Lists;
import com.tqmall.search.canal.RowChangedData;
import com.tqmall.search.commons.mcache.receive.RtCacheSlaveHandle;
import com.tqmall.search.commons.lang.HostInfo;
import com.tqmall.search.commons.lang.StrValueConvert;
import com.tqmall.search.commons.utils.CommonsUtils;
import com.tqmall.search.commons.utils.StrValueConverts;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

/**
 * Created by xing on 16/3/29.
 * 支持通过master-slave通知更新的cache
 * @author xing
 */
public abstract class AbstractSyncCanalCache<K, V> extends AbstractCanalCache<K, V> implements RtCacheSlaveHandle {

    private static final Logger log = LoggerFactory.getLogger(AbstractSyncCanalCache.class);

    private final HostInfo masterHost;

    private Set<String> updateKeyRecords = new HashSet<>();

    protected AbstractSyncCanalCache(String keyFiled, Class<K> keyCls, HostInfo masterHost) {
        this(keyFiled, StrValueConverts.getBasicConvert(keyCls), masterHost);
    }

    protected AbstractSyncCanalCache(String keyFiled, StrValueConvert<K> keyConvert, HostInfo masterHost) {
        super(keyFiled, keyConvert);
        this.masterHost = masterHost;
        RtCacheManager.INSTANCE.getReceive().registerHandler(this);
    }

    /**
     * 从数据库加载最新cache数据, 只有slave机器会调用
     * 如果返回null认为跟空一样, 即最新的keys都已经被删除
     *
     * @param keys 更新的key
     * @return 对应key-value, 如果返回null认为跟空一样, 即最新的keys都已经被删除
     */
    protected abstract Map<K, V> loadCache(List<K> keys);

    @Override
    protected boolean updateValue(K key, V val) {
        boolean changed = super.updateValue(key, val);
        if (changed) {
            updateKeyRecords.add(key.toString());
        }
        return changed;
    }

    @Override
    public void onDeleteAction(List<RowChangedData.Delete> deletedData) {
        super.onDeleteAction(deletedData);
        if (!initialized()) {
            for (RowChangedData.Delete delete : deletedData) {
                updateKeyRecords.add(delete.apply(keyField));
            }
        }
        sendNotify();
    }

    @Override
    public void onInsertAction(List<RowChangedData.Insert> insertedData) {
        super.onInsertAction(insertedData);
        if (!initialized()) {
            for (RowChangedData.Insert insert : insertedData) {
                updateKeyRecords.add(insert.apply(keyField));
            }
        }
        sendNotify();
    }

    @Override
    public void onUpdateAction(List<RowChangedData.Update> updatedData) {
        super.onUpdateAction(updatedData);
        if (!initialized()) {
            for (RowChangedData.Update update : updatedData) {
                if (update.isChanged(keyField)) {
                    updateKeyRecords.add(update.getBefore(keyField));
                }
                updateKeyRecords.add(update.getAfter(keyField));
            }
        }
        sendNotify();
    }

    @Override
    public final HostInfo getMasterHost() {
        return masterHost;
    }

    @Override
    public boolean onSlaveHandle(List<String> keys) {
        if (!initialized() || CommonsUtils.isEmpty(keys)) return false;
        List<K> realKeys = Lists.transform(keys, new Function<String, K>() {
            @Override
            public K apply(String input) {
                return keyConvert.convert(input);
            }
        });
        Map<K, V> valueMap = loadCache(realKeys);
        if (valueMap == null) valueMap = Collections.emptyMap();
        for (K k : realKeys) {
            super.updateValue(k, valueMap.get(k));
        }
        return true;
    }

    private void sendNotify() {
        if (updateKeyRecords.isEmpty()) return;
        try {
            RtCacheManager.INSTANCE.getNotify().notify(this, new ArrayList<>(updateKeyRecords));
        } catch (RuntimeException e) {
            log.error("notify cache changed keys: " + updateKeyRecords + " have exception", e);
        } finally {
            updateKeyRecords.clear();
        }
    }
}
