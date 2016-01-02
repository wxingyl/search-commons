package com.tqmall.search.common.cache.receive;

import com.tqmall.search.common.param.NotifyChangeParam;
import com.tqmall.search.common.utils.HostInfo;

/**
 * Created by xing on 15/12/22.
 * slave机器接收变化的数据,并处理之
 * 多台机器,多个应用之间[也就是多个Tomcat实例之间]通过ip:port来做区分,这也是为什么很多函数里面需要port的原因
 */
public interface RtCacheReceive {

    /**
     * 本地机器cache 对象注册, 如果重复注册则使用最新的,覆盖原先的
     * 请求调用,都是单个单个调用,不用考虑多线程问题
     * @return 注册是否成功
     */
    boolean registerHandler(RtCacheSlaveHandle handler);

    /**
     * 向master机器注册当前机器receive, 调用该方法建议异步调用, http等调用还是挺耗时的
     * 如果原先已经调用过该方法,并且已经成功了,那不会做重复注册, 除非调用{@link #registerHandler(RtCacheSlaveHandle)}
     * 重建添加, 再调用该方法注册
     * 调用masterHost的注册接口的返回结构,建议都以{@link com.tqmall.search.common.result.MapResult}返回,这样好处理,兼容性好点,
     * 提供的默认{@link HttpRtCacheReceive} 实现就是这样做的, 当然你可以自定义实现
     * @param localHost 本地地址信息
     * @return 注册是否全部都成功, 如果成功返回true, 存在失败的返回false
     */
    boolean registerMaster(HostInfo localHost);

    /**
     * 注销RtCacheSlaveHandle处理, 同事请求master执行注销操作
     * @param masterHost 要注销的masterHost
     */
    boolean unRegister(HostInfo localHost, HostInfo masterHost);

    /**
     * 处理接收到的变化
     * @param param 变化的cache
     * canal更新都是单线程,所以接收处理请求也无需考虑多线程
     * @return 是否处理
     */
    boolean receive(NotifyChangeParam param);
}
