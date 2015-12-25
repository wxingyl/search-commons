package com.tqmall.search.common.cache;

import com.tqmall.search.common.param.SlaveRegisterParam;

import java.util.List;

/**
 * Created by xing on 15/12/22.
 * 本地cache对象通知变化的数据
 */
public interface RtCacheNotify {

    /**
     * 记录slave机器的注册请求,这个很有可能同时有多个进来,需要考虑多线程
     * @param param slave机器注册感兴趣的cache
     */
    void handleSlaveRegister(SlaveRegisterParam param);

    /**
     * master 机器通知给slave, 哪些key更改了
     * @param keys 统一用String标识
     * 发送通知,无需考虑多线程
     */
    void notify(RtCacheSlaveHandle slaveCache, List<String> keys);

}
