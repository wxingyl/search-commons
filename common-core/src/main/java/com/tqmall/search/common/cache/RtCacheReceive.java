package com.tqmall.search.common.cache;

/**
 * Created by xing on 15/12/22.
 * slave机器接收变化的数据,并处理之
 */
public interface RtCacheReceive {

    /**
     * 本地机器cache 对象注册
     * 请求调用,都是单个单个调用,不用考虑多线程问题
     */
    void registerHandler(RtCacheSlaveHandle slaveCache);

    /**
     * 向master机器注册当前机器receive
     * @param masterHost master主机机器
     * 请求调用,不用考虑多线程问题
     */
    void registerMaster(String masterHost);

    /**
     * 处理接收到的变化
     * @param param 变化的cache
     * canal更新都是单线程,所以接收处理请求也无需考虑多线程
     */
    void receive(NotifyChangeParam param);
}
