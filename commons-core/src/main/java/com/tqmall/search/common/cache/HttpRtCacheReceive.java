package com.tqmall.search.common.cache;

import com.google.common.collect.Lists;
import com.tqmall.search.common.param.HttpSlaveRegisterParam;
import com.tqmall.search.common.param.Param;
import com.tqmall.search.common.result.MapResult;
import com.tqmall.search.common.result.ResultUtils;
import com.tqmall.search.common.utils.HostInfo;
import com.tqmall.search.common.utils.HttpUtils;
import com.tqmall.search.common.utils.ResultJsonConverts;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Objects;

/**
 * Created by xing on 15/12/23.
 * http implement RtCacheReceive
 */
public class HttpRtCacheReceive extends AbstractRtCacheReceive<SlaveHandleInfo> {

    private final static Logger log = LoggerFactory.getLogger(HttpRtCacheReceive.class);

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

    @Override
    protected SlaveHandleInfo initSlaveHandleInfo(RtCacheSlaveHandle handler, HostInfo masterHost) {
        return new SlaveHandleInfo(handler, masterHost);
    }

    @Override
    protected boolean doMasterRegister(HostInfo localHost, HostInfo masterHost, List<SlaveHandleInfo> handleInfo) {
        HttpSlaveRegisterParam param = new HttpSlaveRegisterParam();
        param.setMethod(HttpUtils.POST_METHOD);
        param.setSlaveHost(HttpUtils.hostInfoToString(localHost));
        param.setUrlPath(buildFullUrlPath(notifyChangePath));
        List<String> interestCache = Lists.newArrayList();
        for (SlaveHandleInfo info : handleInfo) {
            interestCache.add(info.getCacheKey());
        }
        param.setInterestCache(interestCache);
        MapResult mapResult = HttpUtils.requestPost(HttpUtils.buildURL(HttpUtils.hostInfoToString(masterHost),
                buildFullUrlPath(registerPath)), param, ResultJsonConverts.mapResultConvert());
        log.info("注册master: " + HttpUtils.hostInfoToString(masterHost) + " 完成,返回结果: " + ResultUtils.resultToString(mapResult));
        return mapResult.isSuccess();
    }

}
