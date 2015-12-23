package com.tqmall.search.common.cache;

/**
 * Created by xing on 15/12/22.
 * RtCacheManager abstract implement
 */
public class RtCacheManagers {

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
    public static final CacheManager INSTANCE = new HttpCacheManager();

    public static abstract class CacheManager {

        private RtCacheNotify notify;

        private RtCacheReceive receive;

        CacheManager(RtCacheNotify notify, RtCacheReceive receive) {
            this.notify = notify;
            this.receive = receive;
        }

        public RtCacheNotify getNotify() {
            return notify;
        }

        public RtCacheReceive getReceive() {
            return receive;
        }
    }

    static class HttpCacheManager extends CacheManager {

        HttpCacheManager() {
            super(new HttpRtCacheNotify(), new HttpRtCacheReceive());
        }
    }

}
