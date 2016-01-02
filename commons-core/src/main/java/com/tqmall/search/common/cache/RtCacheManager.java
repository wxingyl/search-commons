package com.tqmall.search.common.cache;

import com.tqmall.search.common.cache.notify.RtCacheNotify;
import com.tqmall.search.common.cache.receive.RtCacheReceive;
import com.tqmall.search.common.cache.receive.RtCacheSlaveHandle;

import java.util.Iterator;
import java.util.ServiceLoader;

/**
 * Created by xing on 15/12/22.
 * 通过{@link ServiceLoader}机制指定自己的具体实现类, 如果没有指定, 则默认{@link HttpCacheManager}
 */
public abstract class RtCacheManager {

    public static final RtCacheManager INSTANCE;

    static {
        Iterator<RtCacheManager> it = ServiceLoader.load(RtCacheManager.class).iterator();
        if (it.hasNext()) {
            INSTANCE = it.next();
        } else {
            INSTANCE = new HttpCacheManager();
        }
    }

    /**
     * 获取RtCacheSlaveHandle唯一的一个key
     */
    public static String getCacheHandleKey(RtCacheSlaveHandle handle) {
        return INSTANCE.getCacheKey(handle);
    }

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

    /**
     * default get className
     */
    public String getCacheKey(RtCacheSlaveHandle handle) {
        return handle.getClass().getName();
    }

}
