package com.tqmall.search.common.cache;

/**
 * Created by xing on 15/12/23.
 * http implement RtCacheReceive
 */
public class HttpRtCacheReceive extends AbstractRtCacheReceive {

    private Integer port;

    //TODO 通过Http调用进行注册, 构造HttpSlaveRegisterInfo对象
    @Override
    public void registerMaster(String masterHost) {
        if (cacheHandlerMap.isEmpty()) return;

    }

    public void setPort(Integer port) {
        this.port = port;
    }
}
