package com.tqmall.search.common.cache;

import com.tqmall.search.common.param.HttpSlaveRegisterParam;
import com.tqmall.search.common.param.NotifyChangeParam;
import com.tqmall.search.common.param.SlaveRegisterParam;
import com.tqmall.search.common.utils.HttpUtils;
import com.tqmall.search.common.utils.StrValueConverts;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.concurrent.Executor;

/**
 * Created by xing on 15/12/23.
 * Http implement RtCacheNotify
 * 请求方法都是GET请求
 */
public class HttpRtCacheNotify extends AbstractRtCacheNotify<HttpSlaveRegisterInfo> {

    private static final Logger log = LoggerFactory.getLogger(HttpRtCacheNotify.class);

    /**
     * 具体的Http执行通过异步执行, 如果没有设置, 只能同步发送了
     * 当然建议异步执行
     */
    private Executor executor;

    @Override
    protected HttpSlaveRegisterInfo createSlaveInfo(SlaveRegisterParam param) {
        HttpSlaveRegisterParam httpParam = (HttpSlaveRegisterParam) param;
        //只是检查Http Method是否OK
        if (httpParam.getMethod() != null) {
            HttpUtils.build(httpParam.getMethod());
        } else {
            httpParam.setMethod(HttpUtils.GET_METHOD);
        }
        return HttpSlaveRegisterInfo.build(param.getSlaveHost())
                .urlPath(httpParam.getUrlPath())
                .method(httpParam.getMethod())
                .requestHeaders(httpParam.getRequestHeaders())
                .create();
    }

    @Override
    protected void runNotifyTask(NotifyChangeParam param, List<HttpSlaveRegisterInfo> slaves) {
        String getParam = null;
        for (HttpSlaveRegisterInfo info : slaves) {
            final HttpUtils.RequestBase requestBase = HttpUtils.build(info.getMethod());
            if (HttpUtils.GET_METHOD.equals(info.getMethod())) {
                if (getParam == null) {
                    getParam = String.format("cacheKey=%s&keys=%s&source=%s", param.getCacheKey(),
                            StringUtils.join(param.getKeys(), ','), param.getSource());
                }
            } else {
                ((HttpUtils.HandleBodyRequest) requestBase).setBody(param, true);
            }
            requestBase.setUrl(HttpUtils.buildURL(info.getSlaveHost(), info.getUrlPath(), getParam));

            if (info.getRequestHeaders() != null) {
                requestBase.addHeader(info.getRequestHeaders());
            }
            final String slaveHost = info.getSlaveHost();
            if (executor == null) {
                runNotifyRequest(requestBase, slaveHost);
            } else {
                executor.execute(new Runnable() {
                    @Override
                    public void run() {
                        runNotifyRequest(requestBase, slaveHost);
                    }
                });
            }
        }
    }

    private void runNotifyRequest(HttpUtils.RequestBase requestBase, String slaveHost) {
        String ret = requestBase.request(StrValueConverts.getConvert(String.class));
        log.info("给slave机器: " + slaveHost + "推送缓存变化keys执行完成, 返回结果: " + ret);
    }

    /**
     * 执行http调用的执行器,为null则同步调用
     */
    public void setExecutor(Executor executor) {
        this.executor = executor;
    }
}
