package com.tqmall.search.common.cache;

import com.google.common.collect.Lists;
import com.tqmall.search.common.param.HttpSlaveRegisterParam;
import com.tqmall.search.common.param.Param;
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

    /**
     * 本应用响应Http服务的端口号, 该值必须设定, 不做默认值处理
     * 该field, 包括下面的3个都是用来注册本地slave机器用的
     */
    private Integer port;

    /**
     * 使用Tomcat时, WebApps下面存在多个context, 该值为对应的contextPath
     */
    private String contextPath;

    /**
     * slave机器接收urlPath, 该值为默认值
     */
    private String notifyChangePath = "cache/handle/notify";
    /**
     * slave机器注册cache的路径, 该值为默认值
     */
    private String registerPath = "cache/handle/register";

    @Override
    public boolean registerMaster(String masterHost) {
        //本地机器就没有必要注册了
        if (cacheHandlerMap.isEmpty() || masterHost.contains(HttpUtils.LOCAL_IP)) return false;
        Objects.requireNonNull(port, "unknown local port");
        HttpSlaveRegisterParam param = new HttpSlaveRegisterParam();
        param.setMethod(HttpUtils.POST_METHOD);
        param.setSlaveHost(HttpUtils.LOCAL_IP + ':' + port);
        param.setUrlPath(buildFullUrlPath(notifyChangePath));
        param.setInterestCache(Lists.newArrayList(cacheHandlerMap.keySet()));
        MapResult mapResult = HttpUtils.requestPost(HttpUtils.buildURL(masterHost, buildFullUrlPath(registerPath)),
                param, ResultJsonConverts.mapResultConvert());
        log.info("注册master: " + masterHost + " 完成,返回结果: " + ResultUtils.resultToString(mapResult));
        return mapResult.isSucceed();
    }

    public void setPort(Integer port) {
        this.port = port;
    }

    public void setNotifyChangePath(String notifyChangePath) {
        this.notifyChangePath = filterUrlPath(notifyChangePath);
    }

    public void setRegisterPath(String registerPath) {
        this.registerPath = filterUrlPath(registerPath);
    }

    public void setContextPath(String urlPath) {
        this.contextPath = filterUrlPath(urlPath);
    }

    /**
     * 过滤urlPath, 如果其以'/'开头或者结尾, 则去掉
     *
     * @param urlPath 不能为null或者为空
     * @return 过滤结果
     */
    private String filterUrlPath(String urlPath) {
        urlPath = Param.filterString(urlPath);
        Objects.requireNonNull(urlPath);
        int start = 0, end = urlPath.length();
        if (urlPath.charAt(0) == '/') {
            start++;
        }
        if (urlPath.charAt(end - 1) == '/') {
            end--;
        }
        if (start > 0 || end < urlPath.length()) {
            urlPath = urlPath.substring(start, end);
        }
        return urlPath;
    }

    private String buildFullUrlPath(String urlPath) {
        return contextPath == null ? urlPath : (contextPath + '/' + urlPath);
    }
}
