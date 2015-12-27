package com.tqmall.search.common.cache;

import com.tqmall.search.common.param.NotifyChangeParam;

/**
 * Created by xing on 15/12/22.
 * slave机器接收变化的数据,并处理之
 */
public interface RtCacheReceive {

    /**
     * 本地机器cache 对象注册
     * 请求调用,都是单个单个调用,不用考虑多线程问题
     * @return 注册是否成功
     */
    boolean registerHandler(RtCacheSlaveHandle slaveCache);

    /**
     * 向master机器注册当前机器receive
     * @param masterHost master主机机器, 如果是Http注册,需要端口号
     * 请求调用,不用考虑多线程问题
     * @return 注册是否成功
     */
    boolean registerMaster(String masterHost);

    /**
     * 处理接收到的变化
     * @param param 变化的cache
     * canal更新都是单线程,所以接收处理请求也无需考虑多线程
     */
    void receive(NotifyChangeParam param);
}
