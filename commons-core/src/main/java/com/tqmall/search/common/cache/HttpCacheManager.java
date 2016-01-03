package com.tqmall.search.common.cache;

import com.tqmall.search.common.cache.notify.HttpRtCacheNotify;
import com.tqmall.search.common.cache.receive.HttpRtCacheReceive;

/**
 * Created by xing on 16/1/2.
 * 默认的HttpCacheManager实例
 */
public class HttpCacheManager extends RtCacheManager {

    /**
     * 本地机器注册cache的的默认路径, 默认POST请求
     */
    public static final String LOCAL_DEFAULT_REGISTER_PATH = "cache/handle/register";
    /**
     * 本地机器注销cache的的默认路径, 默认POST请求
     */
    public static final String LOCAL_DEFAULT_UNREGISTER_PATH = "cache/handle/unregister";
    /**
     * 本地机器监听cache服务正常的默认路径, 默认GET请求
     */
    public static final String LOCAL_DEFAULT_MONITOR_PATH = "cache/monitor";

    /**
     * master机器通知变更的默认路径, 默认POST请求
     * 该路径在local机器向master注册时需要告诉master机器
     */
    public static final String MASTER_DEFAULT_NOTIFY_PATH = "cache/handle/notify";

    public HttpCacheManager() {
        super(new HttpRtCacheNotify(), new HttpRtCacheReceive());
    }

}
