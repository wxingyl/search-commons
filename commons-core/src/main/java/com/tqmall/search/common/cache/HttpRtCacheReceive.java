package com.tqmall.search.common.cache;

import com.tqmall.search.common.param.HttpSlaveRegisterParam;
import com.tqmall.search.common.utils.HttpUtils;

import java.util.Objects;

/**
 * Created by xing on 15/12/23.
 * http implement RtCacheReceive
 */
public class HttpRtCacheReceive extends AbstractRtCacheReceive {

    private Integer port;

    //TODO 通过Http调用进行注册, 构造HttpSlaveRegisterParam对象
    @Override
    public void registerMaster(String masterHost) {
        if (cacheHandlerMap.isEmpty()) return;
        Objects.requireNonNull(port, "unknown local port");
        HttpSlaveRegisterParam param = new HttpSlaveRegisterParam();
        param.setMethod(HttpUtils.POST_METHOD);
    }

    public void setPort(Integer port) {
        this.port = port;
    }
}
