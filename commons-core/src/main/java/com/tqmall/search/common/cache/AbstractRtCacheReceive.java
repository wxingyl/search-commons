package com.tqmall.search.common.cache;

import com.tqmall.search.common.param.NotifyChangeParam;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by xing on 15/12/23.
 * AbstractRtCacheReceive
 */
public abstract class AbstractRtCacheReceive implements RtCacheReceive {

    private static final Logger log = LoggerFactory.getLogger(AbstractRtCacheReceive.class);

    /**
     * 本地机器注册的cache对象,这些对象对应着处理slave变化通知
     * key: cache的名称,即cacheKey; value: 对应的RtCacheSlaveHandle对象实例
     */
    protected final Map<String, RtCacheSlaveHandle> cacheHandlerMap = new HashMap<>();

    @Override
    public boolean registerHandler(RtCacheSlaveHandle slaveCache) {
        cacheHandlerMap.put(RtCacheManager.getCacheHandleKey(slaveCache), slaveCache);
        return true;
    }

    @Override
    public boolean receive(NotifyChangeParam param) {
        log.info("接收到变化通知, cacheKey: " + param.getCacheKey() + ", keys: " + param.getKeys());
        if (param.getKeys() == null || param.getKeys().isEmpty()) return false;
        RtCacheSlaveHandle slaveCache = cacheHandlerMap.get(param.getCacheKey());
        if (slaveCache != null && slaveCache.initialized()) {
            slaveCache.onSlaveHandle(param.getKeys());
        }
        return true;
    }
}
