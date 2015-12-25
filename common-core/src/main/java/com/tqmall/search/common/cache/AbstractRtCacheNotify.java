package com.tqmall.search.common.cache;

import com.google.common.base.Supplier;
import com.google.common.collect.Maps;
import com.tqmall.search.common.param.NotifyChangeParam;
import com.tqmall.search.common.param.SlaveRegisterParam;
import com.tqmall.search.common.utils.RwLock;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created by xing on 15/12/23.
 * abstract RtCacheNotify
 */
public abstract class AbstractRtCacheNotify<T extends AbstractSlaveRegisterInfo> implements RtCacheNotify {

    /**
     * key: cache key, value: {@link Pair#getLeft()} is slaveHost, {@link Pair#getRight()} is urlPath
     */
    private RwLock<Map<String, List<T>>> slaveHostLock = RwLock.build(new Supplier<Map<String, List<T>>>() {
        @Override
        public Map<String, List<T>> get() {
            return Maps.newHashMap();
        }
    });

    protected abstract T createSlaveInfo(SlaveRegisterParam param);

    protected abstract void runNotifyTask(NotifyChangeParam param, List<T> slaveHosts);

    @Override
    public void handleSlaveRegister(final SlaveRegisterParam param) {
        if (StringUtils.isEmpty(param.getSlaveHost()) ||
                param.getInterestCache() == null || param.getInterestCache().isEmpty()) return;
        slaveHostLock.writeOp(new RwLock.Op<Map<String, List<T>>>() {
            @Override
            public void op(Map<String, List<T>> input) {
                for (String cache : param.getInterestCache()) {
                    List<T> slaveHosts = input.get(cache);
                    if (slaveHosts == null) {
                        input.put(cache, slaveHosts = new ArrayList<>());
                    }
                    T info = createSlaveInfo(param);
                    if (info != null && !slaveHosts.contains(info)) {
                        slaveHosts.add(info);
                    }
                }
            }
        });
    }

    @Override
    public void notify(RtCacheSlaveHandle slaveCache, List<String> keys) {
        if (keys == null || keys.isEmpty()) return;
        final NotifyChangeParam param = new NotifyChangeParam();
        param.setCacheKey(RtCacheManager.getCacheHandleKey(slaveCache));
        param.setKeys(keys);
        final String cacheKey = RtCacheManager.getCacheHandleKey(slaveCache);
        slaveHostLock.readOp(new RwLock.Op<Map<String, List<T>>>() {
            @Override
            public void op(Map<String, List<T>> input) {
                List<T> slaveHosts = input.get(cacheKey);
                if (slaveHosts == null || slaveHosts.isEmpty()) return;
                runNotifyTask(param, slaveHosts);
            }
        });
    }

}
