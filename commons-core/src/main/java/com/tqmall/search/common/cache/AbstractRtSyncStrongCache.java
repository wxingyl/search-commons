package com.tqmall.search.common.cache;

import com.google.common.base.Function;
import com.google.common.collect.Lists;
import com.tqmall.search.common.utils.HostInfo;
import com.tqmall.search.common.utils.StrValueConvert;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by xing on 15/12/24.
 * 缓存需要实时修改,并且发送或者接受其他机器的改变
 */
public abstract class AbstractRtSyncStrongCache<K, V> extends AbstractRtStrongCache<K, V>
        implements RtCacheSlaveHandle {

    private List<K> updateKeyRecordList = new ArrayList<>();

    private final HostInfo masterHost;

    public AbstractRtSyncStrongCache(HostInfo masterHost) {
        this.masterHost = masterHost;
        getRtCacheManager().getReceive().registerHandler(this);
    }

    /**
     * 默认直接拿{@link RtCacheManager#DEFAULT_INSTANCE}
     * 注意, 该方法是在构造方法里面调用
     */
    protected RtCacheManager getRtCacheManager() {
        return RtCacheManager.DEFAULT_INSTANCE;
    }

    protected abstract Map<K, V> reloadValue(List<K> keys);

    /**
     * 该方法能够被调用, 保证了缓存已经初始化了, 否则不做更新
     * @param key key值
     * @param value 新的value值
     * @return value较原先是否变化
     */
    protected abstract boolean onSlaveHandle(K key, V value);

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
     * 需要完全重写,如果有改动,本地立即加载,因为需要通知slave机器
     * 该方法的更新操作默认调用{@link #onSlaveHandle(Object, Object)}处理
     * @return 是否有改动
     */
    @Override
    public boolean onMasterHandle(K key, V newVal) {
        if (key == null) return false;
        boolean changed;
        if (initialized()) {
            changed = onSlaveHandle(key, newVal);
        } else {
            //主机更新时,如果还没有初始化,那先初始化
            getAllCache();
            changed = true;
        }
        if (changed) {
            appendChangedKey(key);
        }
        return changed;
    }

    @Override
    public void finishUpdate() {
        if (updateKeyRecordList.isEmpty()) return;
        try {
            getRtCacheManager().getNotify().notify(this, Lists.transform(updateKeyRecordList,
                    new Function<K, String>() {
                        @Override
                        public String apply(K k) {
                            return k.toString();
                        }
                    }));
        } finally {
            clearChangedKey();
        }
    }

    /**
     * 调用该方法, 保证了缓存已经初始化了, 否则不做更新
     * @param keys 变动的key list, 参数必须
     */
    @Override
    public boolean onSlaveHandle(List<String> keys) {
        if (!initialized() || keys == null || keys.isEmpty()) {
            //没必要更新
            return false;
        }
        final StrValueConvert<K> keyConvert = getKeyValueConvert();
        List<K> keyList = new ArrayList<>();
        for (String k : keys) {
            keyList.add(keyConvert.convert(k));
        }
        Map<K, V> newData = reloadValue(keyList);
        if (newData == null) newData = new HashMap<>();
        for (K key : keyList) {
            onSlaveHandle(key, newData.get(key));
        }
        return true;
    }

    @Override
    public HostInfo getMasterHost() {
        return masterHost;
    }
}
