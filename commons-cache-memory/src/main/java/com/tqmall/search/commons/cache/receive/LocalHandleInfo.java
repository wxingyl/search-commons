package com.tqmall.search.commons.cache.receive;

import com.tqmall.search.commons.cache.RtCacheManager;
import com.tqmall.search.commons.lang.HostInfo;

/**
 * Created by xing on 15/12/29.
 * 默认的{@link RtCacheSlaveHandle} 相关信息封装
 */
public abstract class LocalHandleInfo {

    private RtCacheSlaveHandle handler;

    private HostInfo masterHost;
    /**
     * 区分各个cache的key, 跟环境无关, 默认实现是调用{@link RtCacheManager#getCacheKey(RtCacheSlaveHandle)}生成
     */
    private String cacheKey;
    /**
     * 注册是否成功
     */
    private volatile boolean registerSucceed;

    public LocalHandleInfo(RtCacheSlaveHandle handler, HostInfo masterHost) {
        this.handler = handler;
        this.masterHost = masterHost;
        this.setCacheKey(RtCacheManager.getCacheHandleKey(handler));
    }

    public void setCacheKey(String cacheKey) {
        this.cacheKey = cacheKey;
    }

    public boolean isRegisterSucceed() {
        return registerSucceed;
    }

    public void registerSucceed() {
        if (registerSucceed) return;
        registerSucceed = true;
    }

    public RtCacheSlaveHandle getHandler() {
        return handler;
    }

    public HostInfo getMasterHost() {
        return masterHost;
    }

    public String getCacheKey() {
        return cacheKey;
    }

}
