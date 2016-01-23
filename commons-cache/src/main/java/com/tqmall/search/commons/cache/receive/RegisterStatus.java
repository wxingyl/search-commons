package com.tqmall.search.commons.cache.receive;

/**
 * Created by xing on 16/1/4.
 * 注册状态
 */
public enum RegisterStatus {
    //初始状态, 还没有执行注册
    INIT,
    //无需注册, masterHost为本地或者过滤掉了
    USELESS,
    //注册成功
    SUCCEED,
    //注册失败
    FAILED,
    //注册中断
    INTERRUPT,
    //完成注销
    UNREGISTER;
}
