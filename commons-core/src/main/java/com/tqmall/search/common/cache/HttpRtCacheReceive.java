package com.tqmall.search.common.cache;

import com.google.common.collect.Lists;
import com.tqmall.search.common.param.HttpSlaveRegisterParam;
import com.tqmall.search.common.result.MapResult;
import com.tqmall.search.common.result.ResultUtils;
import com.tqmall.search.common.utils.HttpUtils;
import com.tqmall.search.common.utils.ResultJsonConverts;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Objects;

/**
 * Created by xing on 15/12/23.
 * http implement RtCacheReceive
 */
public class HttpRtCacheReceive extends AbstractRtCacheReceive {

    private final static Logger log = LoggerFactory.getLogger(HttpRtCacheReceive.class);

    private Integer port;

    /**
     * slave机器接收urlPath, 该值为默认值
     */
    private String notifyChangePath = "/cache/notify_change";
    /**
     * slave机器注册cache的路径, 该值为默认值
     */
    private String registerPath = "/cache/register";

    @Override
    public boolean registerMaster(String masterHost) {
        //本地机器就没有必要注册了
        if (cacheHandlerMap.isEmpty() || masterHost.contains(HttpUtils.LOCAL_IP)) return false;
        Objects.requireNonNull(port, "unknown local port");
        HttpSlaveRegisterParam param = new HttpSlaveRegisterParam();
        param.setMethod(HttpUtils.POST_METHOD);
        param.setSlaveHost(HttpUtils.LOCAL_IP + ':' + port);
        param.setUrlPath(notifyChangePath);
        param.setInterestCache(Lists.newArrayList(cacheHandlerMap.keySet()));
        MapResult mapResult = HttpUtils.requestPost(HttpUtils.buildURL(masterHost, registerPath),
                param, ResultJsonConverts.mapResultConvert());
        log.info("注册master: " + masterHost + " 完成,返回结果: " + ResultUtils.resultToString(mapResult));
        return mapResult.isSucceed();
    }

    public void setPort(Integer port) {
        this.port = port;
    }

    public void setNotifyChangePath(String notifyChangePath) {
        if (notifyChangePath != null) {
            this.notifyChangePath = notifyChangePath;
        }
    }

    public void setRegisterPath(String registerPath) {
        this.registerPath = registerPath;
    }

}
