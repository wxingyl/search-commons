package com.tqmall.search.common.cache;

import com.tqmall.search.common.param.NotifyChangeParam;
import com.tqmall.search.common.param.SlaveRegisterParam;
import org.apache.commons.lang3.StringUtils;

import java.util.List;

/**
 * Created by xing on 15/12/23.
 * Http implement RtCacheNotify
 * 请求方法都是GET请求
 */
public class HttpRtCacheNotify extends AbstractRtCacheNotify<HttpSlaveRegisterInfo> {

    @Override
    protected HttpSlaveRegisterInfo createSlaveInfo(SlaveRegisterParam param) {
        return null;
    }

    @Override
    protected void runNotifyTask(NotifyChangeParam param, List<HttpSlaveRegisterInfo> slaveHosts) {
        final String paramStr = String.format("cacheKey=%s&keys=%s&source=%s", param.getCacheKey(),
                StringUtils.join(param.getKeys(), ','), param.getSource());
    }
}
