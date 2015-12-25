package com.tqmall.search.common.cache;

import java.util.Collections;
import java.util.Map;

/**
 * Created by xing on 15/12/25.
 * Http slave 机器注册信息
 */
public class HttpSlaveRegisterInfo extends AbstractSlaveRegisterInfo {

    /**
     * 接收处理缓存变化的path, 一个host只用一个,不支持不同的缓存key对应不同的urlPath, 那样太麻烦了
     */
    private String urlPath;

    /**
     * 说明那中Http方法,目前只支持GET, POST, PUT, DELETE
     */
    private String method;

    public Map<String, String> requestHeaders;

    private HttpSlaveRegisterInfo(String slaveHost){
        super(slaveHost);
    }

    public String getMethod() {
        return method;
    }

    public Map<String, String> getRequestHeaders() {
        return requestHeaders;
    }

    public String getUrlPath() {
        return urlPath;
    }

    public static Build build(String slaveHost) {
        return new Build(slaveHost);
    }

    public static class Build {

        private String slaveHost, urlPath, method;

        private Map<String, String> requestHeaders;

        public Build(String slaveHost) {
            this.slaveHost = slaveHost;
        }

        public Build urlPath(String urlPath) {
            this.urlPath = urlPath;
            return this;
        }

        public Build method(String method) {
            this.method = method;
            return this;
        }

        public Build requestHeaders(Map<String, String> requestHeaders) {
            if (requestHeaders != null && !requestHeaders.isEmpty()) {
                this.requestHeaders = requestHeaders;
            }
            return this;
        }

        public HttpSlaveRegisterInfo create() {
            HttpSlaveRegisterInfo info = new HttpSlaveRegisterInfo(slaveHost);
            info.urlPath = urlPath;
            info.method = method;
            if (requestHeaders != null) {
                info.requestHeaders = Collections.unmodifiableMap(requestHeaders);
            }
            return info;
        }
    }
}
