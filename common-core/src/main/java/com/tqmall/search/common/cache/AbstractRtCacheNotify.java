package com.tqmall.search.common.cache;

import com.google.common.base.Supplier;
import com.tqmall.search.common.param.NotifyChangeParam;
import com.tqmall.search.common.param.SlaveRegisterParam;
import com.tqmall.search.common.utils.RwLock;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by xing on 15/12/23.
 * abstract RtCacheNotify
 */
public abstract class AbstractRtCacheNotify implements RtCacheNotify {

    /**
     * key: cache key, value: slave host list
     */
    private RwLock<Map<String, List<String>>> cacheHostLock = RwLock.build(new Supplier<Map<String, List<String>>>() {
        @Override
        public Map<String, List<String>> get() {
            return new HashMap<>();
        }
    });

    protected abstract void runNotifyTask(NotifyChangeParam param, List<String> slaveHosts);

    @Override
    public void recordSlaveRegister(final SlaveRegisterParam param) {
        final String slaveHost = param.getSlaveHost();
        if (slaveHost == null || slaveHost.isEmpty() ||
                param.getInterestCache() == null || param.getInterestCache().isEmpty()) return;
        cacheHostLock.writeOp(new RwLock.Op<Map<String, List<String>>>() {
            @Override
            public void op(Map<String, List<String>> input) {
                for (String cache : param.getInterestCache()) {
                    List<String> slaveHosts = input.get(cache);
                    if (slaveHosts == null) {
                        input.put(cache, slaveHosts = new ArrayList<>());
                    }
                    if (!slaveHosts.contains(slaveHost)) {
                        slaveHosts.add(slaveHost);
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
        cacheHostLock.readOp(new RwLock.Op<Map<String, List<String>>>() {
            @Override
            public void op(Map<String, List<String>> input) {
                List<String> slaveHosts = input.get(cacheKey);
                if (slaveHosts == null) return;
                runNotifyTask(param, slaveHosts);
            }
        });
    }

}
