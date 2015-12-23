package com.tqmall.search.common.cache;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by xing on 15/12/23.
 * AbstractRtCacheReceive
 */
public abstract class AbstractRtCacheReceive implements RtCacheReceive {

    /**
     * 本地机器注册的cache对象,这些对象对应着处理slave变化通知
     */
    private Map<String, RtCacheSlaveHandle> cacheHandlerMap = new HashMap<>();

    @Override
    public void registerHandler(RtCacheSlaveHandle slaveCache) {
        cacheHandlerMap.put(RtCacheManagers.getCacheHandleKey(slaveCache), slaveCache);
    }

    @Override
    public void receive(NotifyChangeParam param) {
        RtCacheSlaveHandle slaveCache = cacheHandlerMap.get(param.getCacheKey());
        if (slaveCache != null) {
            slaveCache.onSlaveHandle(param.getKeys());
        }
    }
}
