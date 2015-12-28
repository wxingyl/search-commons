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
     * 调用masterHost的注册接口的返回结构,建议都以{@link com.tqmall.search.common.result.MapResult}返回,
     * 这样好处理,兼容性好点, 提供的默认{@link HttpRtCacheReceive} 实现就是这样做的, 当然你可以自定义实现
     * @param masterIp master主机ip
     * @param masterPort 端口号, 如果是Http注册,需要
     * 请求调用,不用考虑多线程问题
     * @return 注册是否成功
     */
    boolean registerMaster(String masterIp, int masterPort);

    /**
     * 处理接收到的变化
     * @param param 变化的cache
     * canal更新都是单线程,所以接收处理请求也无需考虑多线程
     * @return 是否处理
     */
    boolean receive(NotifyChangeParam param);
}
