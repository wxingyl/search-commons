package com.tqmall.search.common.cache.notify;

import com.tqmall.search.common.lang.HostInfo;

import java.util.Collections;
import java.util.Map;

/**
 * Created by xing on 15/12/25.
 * Http slave 机器注册信息
 */
public class HttpSlaveHostInfo extends AbstractSlaveHostInfo {

    /**
     * 接收处理缓存变化的path, 一个host只用一个,不支持不同的缓存key对应不同的urlPath, 那样太麻烦了
     */
    private String notifyUrlPath;

    /**
     * 说明那中Http方法,目前只支持GET, POST, PUT, DELETE
     */
    private String httpMethod;

    public Map<String, String> requestHeaders;

    private HttpSlaveHostInfo(HostInfo slaveHost){
        super(slaveHost);
    }

    public String getHttpMethod() {
        return httpMethod;
    }

    public Map<String, String> getRequestHeaders() {
        return requestHeaders;
    }

    public String getNotifyUrlPath() {
        return notifyUrlPath;
    }

    public static Build build(HostInfo slaveHost) {
        return new Build(slaveHost);
    }

    public static class Build {

        private HostInfo slaveHost;

        private String notifyUrlPath, httpMethod;

        private Map<String, String> requestHeaders;

        public Build(HostInfo slaveHost) {
            this.slaveHost = slaveHost;
        }

        public Build notifyUrlPath(String urlPath) {
            this.notifyUrlPath = urlPath;
            return this;
        }

        public Build httpMethod(String httpMethod) {
            this.httpMethod = httpMethod;
            return this;
        }

        public Build requestHeaders(Map<String, String> requestHeaders) {
            if (requestHeaders != null && !requestHeaders.isEmpty()) {
                this.requestHeaders = requestHeaders;
            }
            return this;
        }

        public HttpSlaveHostInfo create() {
            HttpSlaveHostInfo info = new HttpSlaveHostInfo(slaveHost);
            info.notifyUrlPath = notifyUrlPath;
            info.httpMethod = httpMethod;
            if (requestHeaders != null) {
                info.requestHeaders = Collections.unmodifiableMap(requestHeaders);
            }
            return info;
        }
    }
}
