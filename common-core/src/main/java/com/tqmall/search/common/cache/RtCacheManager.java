package com.tqmall.search.common.cache;

/**
 * Created by xing on 15/12/22.
 * RtCacheManager abstract implement
 */
public abstract class RtCacheManager {

    /**
     * 获取RtCacheSlaveHandle唯一的一个key
     * @return 目前取类名
     */
    public static String getCacheHandleKey(RtCacheSlaveHandle handle) {
        return handle.getClass().getName();
    }
    /**
     * 默认实例为Http实现方式
     */
    public static final RtCacheManager DEFAULT_INSTANCE = new HttpCacheManager();

    private RtCacheNotify notify;

    private RtCacheReceive receive;

    public RtCacheManager(RtCacheNotify notify, RtCacheReceive receive) {
        this.notify = notify;
        this.receive = receive;
    }

    public RtCacheNotify getNotify() {
        return notify;
    }

    public RtCacheReceive getReceive() {
        return receive;
    }

    static class HttpCacheManager extends RtCacheManager {

        HttpCacheManager() {
            super(new HttpRtCacheNotify(), new HttpRtCacheReceive());
        }

    }

}
