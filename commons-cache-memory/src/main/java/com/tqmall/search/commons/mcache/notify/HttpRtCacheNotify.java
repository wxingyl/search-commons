package com.tqmall.search.commons.mcache.notify;

import com.tqmall.search.commons.lang.HostInfo;
import com.tqmall.search.commons.param.HttpLocalRegisterParam;
import com.tqmall.search.commons.param.LocalRegisterParam;
import com.tqmall.search.commons.param.NotifyChangeParam;
import com.tqmall.search.commons.result.MapResult;
import com.tqmall.search.commons.result.ResultUtils;
import com.tqmall.search.commons.utils.HttpMethod;
import com.tqmall.search.commons.utils.HttpUtils;
import com.tqmall.search.commons.utils.SearchStringUtils;
import com.tqmall.search.commons.utils.StrValueConverts;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;
import java.util.concurrent.Executor;

/**
 * Created by xing on 15/12/23.
 * Http implement RtCacheNotify
 * 请求方法都是GET请求
 */
public class HttpRtCacheNotify extends AbstractRtCacheNotify<HttpSlaveHostInfo> {

    private static final Logger log = LoggerFactory.getLogger(HttpRtCacheNotify.class);

    private String contextPath;

    private String unRegisterUrlPath;

    private String monitorUrlPath;
    /**
     * 具体的Http执行通过异步执行, 如果没有设置, 只能同步发送了
     * 当然建议异步执行
     */
    private Executor executor;

    @Override
    protected HttpSlaveHostInfo createSlaveInfo(LocalRegisterParam param) {
        HttpLocalRegisterParam httpParam = (HttpLocalRegisterParam) param;
        //只是检查Http Method是否OK
        if (httpParam.getHttpMethod() == null) {
            httpParam.setHttpMethod(HttpMethod.GET);
        }
        return HttpSlaveHostInfo.build(param.getSlaveHost())
                .notifyUrlPath(httpParam.getNotifyUrlPath())
                .httpMethod(httpParam.getHttpMethod())
                .requestHeaders(httpParam.getRequestHeaders())
                .create();
    }

    @Override
    protected void runNotifyTask(NotifyChangeParam param, List<HttpSlaveHostInfo> slaves) {
        String getParam = null;
        for (final HttpSlaveHostInfo info : slaves) {
            try {
                final HttpUtils.Request request = info.getHttpMethod().build();
                if (HttpMethod.GET == info.getHttpMethod()) {
                    if (getParam == null) {
                        getParam = String.format("cacheKey=%s&keys=%s&source=%s", param.getCacheKey(),
                                SearchStringUtils.join(param.getKeys(), ','), param.getSource());
                    }
                } else {
                    ((HttpUtils.HandleBodyRequest) request).setBody(param, true);
                }
                request.setUrl(HttpUtils.buildURL(info, info.getNotifyUrlPath(), getParam));

                if (info.getRequestHeaders() != null) {
                    request.addHeader(info.getRequestHeaders());
                }
                if (executor == null) {
                    runNotifyRequest(request, info);
                } else {
                    executor.execute(new Runnable() {
                        @Override
                        public void run() {
                            runNotifyRequest(request, info);
                        }
                    });
                }
            } catch (Throwable e) {
                log.error("通知slave机器: " + info + ", cacheKey: " + param.getCacheKey() + "变更, keys: " + param.getKeys() + "发生异常", e);
            }
        }
    }

    @Override
    protected MapResult wrapSlaveRegisterResult(LocalRegisterParam param) {
        MapResult ret = ResultUtils.mapResult("msg", "注册成功");
        if (unRegisterUrlPath != null) {
            ret.put("unRegisterUrlPath", buildFullUrlPath(unRegisterUrlPath));
        }
        if (monitorUrlPath != null) {
            ret.put("monitorUrlPath", buildFullUrlPath(monitorUrlPath));
        }
        return ret;
    }

    @Override
    protected Map<String, Object> appendHostStatusInfo(HttpSlaveHostInfo slaveHost, Map<String, Object> infoMap) {
        infoMap.put("notifyUrlPath", slaveHost.getNotifyUrlPath());
        infoMap.put("httpMethod", slaveHost.getHttpMethod());
        infoMap.put("requestHeaders", slaveHost.getRequestHeaders());
        return infoMap;
    }

    private void runNotifyRequest(HttpUtils.Request request, HostInfo slaveHost) {
        //这儿用String做转换,基本上是万能的
        String ret = request.request(StrValueConverts.getConvert(String.class));
        log.info("给slave机器: " + slaveHost + "推送变化执行完成, 返回结果: " + ret);
    }

    private String buildFullUrlPath(String urlPath) {
        return contextPath == null ? urlPath : (contextPath + '/' + urlPath);
    }

    /**
     * 执行http调用的执行器,为null则同步调用
     */
    public void setExecutor(Executor executor) {
        this.executor = executor;
    }

    public void setContextPath(String contextPath) {
        this.contextPath = HttpUtils.filterUrlPath(contextPath);
    }

    public void setMonitorUrlPath(String monitorUrlPath) {
        this.monitorUrlPath = HttpUtils.filterUrlPath(monitorUrlPath);
    }

    public void setUnRegisterUrlPath(String unRegisterUrlPath) {
        this.unRegisterUrlPath = HttpUtils.filterUrlPath(unRegisterUrlPath);
    }
}
