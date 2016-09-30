package com.tqmall.search.commons.mcache.receive;

import com.tqmall.search.commons.param.NotifyChangeParam;
import com.tqmall.search.commons.result.MapResult;
import com.tqmall.search.commons.lang.HostInfo;

import java.util.List;
import java.util.Map;

/**
 * Created by xing on 15/12/22.
 * slave机器接收变化的数据,并处理之
 * 多台机器,多个应用之间[也就是多个Tomcat实例之间]通过ip:port来做区分,这也是为什么很多函数里面需要port的原因
 */
public interface RtCacheReceive {

    /**
     * 本地机器cache 对象注册, 如果重复注册则使用最新的,覆盖原先的
     * 请求调用,都是单个单个调用,不用考虑多线程问题
     * @return {@link MapResult#isSuccess()} 为false表示参数等存在错误, 必须修改当前入参或者当前配置才能重新注册, 正常情况下一只返回true
     */
    MapResult registerHandler(RtCacheSlaveHandle handler);

    /**
     * 向master机器注册当前机器receive, 调用该方法建议异步调用, http等调用还是挺耗时的
     * 如果原先已经调用过该方法,并且已经成功了,那不会做重复注册, 除非重新调用{@link #registerHandler(RtCacheSlaveHandle)}添加
     * {@link MapResult#isSuccess()} 为false表示参数等存在错误, 必须修改当前入参或者当前配置才能重新注册, 正常情况下一只返回true
     * @param localHost 本地地址信息
     * @return 注册是否全部都成功, 如果成功返回true, 存在失败的返回false
     */
    MapResult registerMaster(HostInfo localHost);

    /**
     * 注销RtCacheSlaveHandle处理, 同时请求master执行注销操作
     * @return {@link MapResult#isSuccess()} 为false表示参数等存在错误, 必须修改当前入参或者当前配置才能重新注册, 正常情况下一只返回true
     */
    MapResult unRegister(HostInfo localHost);

    /**
     * 监听本地连接master是否正常, 如果还没有注册成功, 自然也就没有必要检查了,但是如果已经注册成功的,需要检查连接是否正常
     * 如果无法正常连接, 建议调用{@link #registerMaster(HostInfo)}重新连接
     * @return 是否正常
     */
    boolean doMonitor(HostInfo localHost);

    /**
     * 处理接收到的变化
     * @param param 变化的cache
     * canal更新都是单线程,所以接收处理请求也无需考虑多线程
     * @return 是否处理
     */
    boolean receive(NotifyChangeParam param);

    /**
     * 获取当前管理的Handler状态, 结果根据master机器纬度返回
     * @return 以map返回, 返回结果又不做传输什么的, Map就行了
     */
    List<Map<String, Object>> status();
}
