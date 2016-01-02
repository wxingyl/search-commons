package com.tqmall.search.common.cache;

import com.tqmall.search.common.cache.notify.HttpRtCacheNotify;
import com.tqmall.search.common.cache.receive.HttpRtCacheReceive;

/**
 * Created by xing on 16/1/2.
 * 默认的HttpCacheManager实例
 */
public class HttpCacheManager extends RtCacheManager {

    /**
     * 本地机器注册cache的的默认路径, 当然这个是通过Http方式注册才会用到
     */
    public static final String LOCAL_DEFAULT_REGISTER_PATH = "cache/handle/register";

    public static final String LOCAL_DEFAULT_UNREGISTER_PATH = "cache/handle/unregister";

    public HttpCacheManager() {
        super(new HttpRtCacheNotify(), new HttpRtCacheReceive());
    }

}
