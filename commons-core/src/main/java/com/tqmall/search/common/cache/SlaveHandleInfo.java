package com.tqmall.search.common.cache;

/**
 * Created by xing on 15/12/29.
 * 默认的{@link RtCacheSlaveHandle} 相关信息封装
 */
public class SlaveHandleInfo {

    private RtCacheSlaveHandle handler;

    private String masterHost;

    private String cacheKey;
    /**
     * 注册是否成功
     */
    private volatile boolean registerSucceed;

    public SlaveHandleInfo(RtCacheSlaveHandle handler, String masterHost) {
        this(RtCacheManager.getCacheHandleKey(handler), handler, masterHost);
    }

    public SlaveHandleInfo(String cacheKey, RtCacheSlaveHandle handler, String masterHost) {
        this.cacheKey = cacheKey;
        this.handler = handler;
        this.masterHost = masterHost;
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

    public String getMasterHost() {
        return masterHost;
    }

    public String getCacheKey() {
        return cacheKey;
    }

}
