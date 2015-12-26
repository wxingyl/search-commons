package com.tqmall.search.common.param;

import java.util.Map;

/**
 * Created by xing on 15/12/23.
 * 通过Http注册cache
 */
public class HttpSlaveRegisterParam extends SlaveRegisterParam {

    private static final long serialVersionUID = 7844126899989685640L;

    /**
     * 接收处理缓存变化的path, 一个host只用一个,不支持不同的缓存key对应不同的urlPath, 那样太麻烦了
     */
    private String urlPath;

    /**
     * 说明那中Http方法,目前只支持GET, POST, PUT, DELETE
     */
    private String method;

    public Map<String, String> requestHeaders;


    public String getUrlPath() {
        return urlPath;
    }

    public String getMethod() {
        return method;
    }

    public Map<String, String> getRequestHeaders() {
        return requestHeaders;
    }

    public void setUrlPath(String urlPath) {
        this.urlPath = urlPath;
    }

    public void setMethod(String method) {
        this.method = method;
    }

    public void setRequestHeaders(Map<String, String> requestHeaders) {
        this.requestHeaders = requestHeaders;
    }
}
