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

import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
    private String notifyChangePath;

    private Function<HostInfo, String> registerUrlPathFactory;

    /**
     * 默认超时链接600ms, 读取超时时间1000ms
     */
    private HttpUtils.Config httpConfig = new HttpUtils.Config(600, 1000, 1024, "UTF-8");

    public void setHttpConfig(HttpUtils.Config httpConfig) {
        if (httpConfig == null) return;
        this.httpConfig = httpConfig;
    }

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
        return new HttpMasterHostInfo(masterHost);
    }

    @Override
    protected boolean doMasterRegister(HostInfo localHost, HttpMasterHostInfo masterHostInfo,
                                       List<String> cacheKeys) {
        HttpLocalRegisterParam bodyBean = new HttpLocalRegisterParam();
        bodyBean.setMethod(HttpUtils.POST_METHOD);
        bodyBean.setSlaveHost(new HostInfoObj(localHost));
        bodyBean.setNotifyUrlPath(buildFullUrlPath(notifyChangePath == null ?
                HttpCacheManager.MASTER_DEFAULT_NOTIFY_PATH : notifyChangePath));
        bodyBean.setInterestCache(cacheKeys);
        MapResult mapResult = HttpUtils.requestPost(HttpUtils.buildURL(masterHostInfo,
                        getRegisterUrlPath(masterHostInfo)),
                bodyBean, ResultJsonConverts.mapResultConvert());
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
        HostInfoObj bodyBean = new HostInfoObj(localHost);
        MapResult mapResult = HttpUtils.requestPost(HttpUtils.buildURL(masterHostInfo,
                        masterHostInfo.getUnRegisterUrlPath()),
                bodyBean, ResultJsonConverts.mapResultConvert());
        log.info("注销master: " + HttpUtils.hostInfoToString(masterHostInfo) + " 完成,返回结果: "
                + ResultUtils.resultToString(mapResult));
        return mapResult.isSuccess();
    }

    /**
     * monitor监控统一用GET请求就可以了, 返回结果获取status字段
     */
    @Override
    protected boolean doMasterMonitor(HostInfo localHost, HttpMasterHostInfo masterHostInfo) {
        Map<String, Object> param = new HashMap<>();
        param.put("ip", localHost.getIp());
        param.put("port", localHost.getPort());
        MapResult mapResult = HttpUtils.buildGet()
                .setUrl(HttpUtils.buildURL(masterHostInfo, masterHostInfo.getMonitorPath(), param))
                .setConfig(httpConfig)
                .request(ResultJsonConverts.mapResultConvert());
        return mapResult.isSuccess() && Boolean.TRUE.equals(mapResult.get("status"));
    }

    @Override
    protected Map<String, Object> appendHostStatusInfo(HttpMasterHostInfo masterHost, Map<String, Object> hostInfo) {
        hostInfo.put("unRegisterUrlPath", masterHost.getUnRegisterUrlPath());
        hostInfo.put("monitorPath", masterHost.getMonitorPath());
        return hostInfo;
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
