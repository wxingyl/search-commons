package com.tqmall.search.common.cache.receive;

import com.google.common.base.Function;
import com.tqmall.search.common.cache.HttpCacheManager;
import com.tqmall.search.common.param.HostInfoObj;
import com.tqmall.search.common.param.HttpLocalRegisterParam;
import com.tqmall.search.common.result.MapResult;
import com.tqmall.search.common.result.ResultUtils;
import com.tqmall.search.common.utils.HostInfo;
import com.tqmall.search.common.utils.HttpUtils;
import com.tqmall.search.common.utils.ResultJsonConverts;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * Created by xing on 15/12/23.
 * http implement RtCacheReceive
 */
public class HttpRtCacheReceive extends AbstractRtCacheReceive<HttpMasterHostInfo> {

    private final static Logger log = LoggerFactory.getLogger(HttpRtCacheReceive.class);

    private String contextPath;

    /**
     * 本地机器接收cache变化的urlPath, 注册是时候告知master
     */
    private String notifyChangePath = "cache/handle/notify";

    private Function<HostInfo, String> registerUrlPathFactory;

    public void setNotifyChangePath(String notifyChangePath) {
        this.notifyChangePath = HttpUtils.filterUrlPath(notifyChangePath);
    }

    /**
     * 使用Tomcat时, WebApps下面存在多个context, 该值为对应的contextPath
     * contextPath对{@link #notifyChangePath}起作用, 并且如果registerUrlPath使用的是
     * {@link HttpCacheManager#LOCAL_DEFAULT_REGISTER_PATH}, 也起作用
     *
     * @see #setRegisterUrlPathFactory(Function)
     */
    public void setContextPath(String contextPath) {
        this.contextPath = HttpUtils.filterUrlPath(contextPath);
    }

    /**
     * @see #setContextPath(String)
     */
    public void setRegisterUrlPathFactory(final Function<HostInfo, String> registerUrlPathFactory) {
        this.registerUrlPathFactory = registerUrlPathFactory;
    }

    @Override
    protected HttpMasterHostInfo initMasterHostInfo(HostInfo masterHost) {
        HttpMasterHostInfo hostInfo = new HttpMasterHostInfo(masterHost);
        hostInfo.setRegisterUrlPath(getRegisterUrlPath(masterHost));
        return hostInfo;
    }

    @Override
    protected boolean doMasterRegister(HostInfo localHost, HttpMasterHostInfo masterHostInfo,
                                       List<String> cacheKeys) {
        HttpLocalRegisterParam param = new HttpLocalRegisterParam();
        param.setMethod(HttpUtils.POST_METHOD);
        param.setSlaveHost(new HostInfoObj(localHost));
        param.setNotifyUrlPath(buildFullUrlPath(notifyChangePath));
        param.setInterestCache(cacheKeys);
        MapResult mapResult = HttpUtils.requestPost(HttpUtils.buildURL(masterHostInfo, masterHostInfo.getRegisterUrlPath()),
                param, ResultJsonConverts.mapResultConvert());
        String urlPath = (String) mapResult.get("unRegisterUrlPath");
        if (urlPath == null) {
            urlPath = buildFullUrlPath(HttpCacheManager.LOCAL_DEFAULT_UNREGISTER_PATH);
        }
        masterHostInfo.setUnRegisterUrlPath(urlPath);
        urlPath = (String) mapResult.get("monitorUrlPath");
        if (urlPath == null) {
            urlPath = buildFullUrlPath(HttpCacheManager.LOCAL_DEFAULT_MONITOR_PATH);
        }
        masterHostInfo.setMonitorPath(urlPath);
        log.info("注册master: " + HttpUtils.hostInfoToString(masterHostInfo) + " 完成,返回结果: "
                + ResultUtils.resultToString(mapResult));
        return mapResult.isSuccess();
    }

    @Override
    protected boolean doMasterUnRegister(HostInfo localHost, HttpMasterHostInfo masterHostInfo) {
        HostInfoObj obj = new HostInfoObj(localHost);
        MapResult mapResult = HttpUtils.requestPost(HttpUtils.buildURL(masterHostInfo, masterHostInfo.getUnRegisterUrlPath()),
                obj, ResultJsonConverts.mapResultConvert());
        log.info("注销master: " + HttpUtils.hostInfoToString(masterHostInfo) + " 完成,返回结果: "
                + ResultUtils.resultToString(mapResult));
        return mapResult.isSuccess();
    }

    @Override
    protected boolean doMasterMonitor(HttpMasterHostInfo masterHostInfo) {
        MapResult mapResult = HttpUtils.requestGetMapResult(HttpUtils.buildURL(masterHostInfo, masterHostInfo.getMonitorPath()));
        return mapResult.isSuccess();
    }

    private String buildFullUrlPath(String urlPath) {
        return contextPath == null ? urlPath : (contextPath + '/' + urlPath);
    }

    private String getRegisterUrlPath(HostInfo hostInfo) {
        String ret;
        if (registerUrlPathFactory == null || ((ret = registerUrlPathFactory.apply(hostInfo)) == null)) {
            return buildFullUrlPath(HttpCacheManager.LOCAL_DEFAULT_REGISTER_PATH);
        } else {
            return ret;
        }
    }
}
