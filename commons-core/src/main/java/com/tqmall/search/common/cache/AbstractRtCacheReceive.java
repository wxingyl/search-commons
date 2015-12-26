package com.tqmall.search.common.cache;

import com.tqmall.search.common.param.NotifyChangeParam;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by xing on 15/12/23.
 * AbstractRtCacheReceive
 */
public abstract class AbstractRtCacheReceive implements RtCacheReceive {

    /**
     * 本地机器注册的cache对象,这些对象对应着处理slave变化通知
     * key: cache的名称,即cacheKey; value: 对应的RtCacheSlaveHandle对象实例
     */
    protected final Map<String, RtCacheSlaveHandle> cacheHandlerMap = new HashMap<>();

    @Override
    public void registerHandler(RtCacheSlaveHandle slaveCache) {
        cacheHandlerMap.put(RtCacheManager.getCacheHandleKey(slaveCache), slaveCache);
    }

    @Override
    public void receive(NotifyChangeParam param) {
        if (param.getKeys() == null || param.getKeys().isEmpty()) return;
        RtCacheSlaveHandle slaveCache = cacheHandlerMap.get(param.getCacheKey());
        if (slaveCache != null && slaveCache.initialized()) {
            slaveCache.onSlaveHandle(param.getKeys());
        }
    }
}
