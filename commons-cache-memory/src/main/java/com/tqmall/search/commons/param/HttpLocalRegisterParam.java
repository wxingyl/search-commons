package com.tqmall.search.commons.param;

import com.tqmall.search.commons.utils.HttpMethod;

import java.util.Map;

/**
 * Created by xing on 15/12/23.
 * 通过Http注册cache
 */
public class HttpLocalRegisterParam extends LocalRegisterParam {

    private static final long serialVersionUID = 7844126899989685640L;

    /**
     * 接收处理缓存变化的path, 一个host只用一个,不支持不同的缓存key对应不同的urlPath, 那样太麻烦了
     */
    private String notifyUrlPath;

    /**
     * 说明那中Http方法,目前只支持GET, POST, PUT, DELETE
     */
    private HttpMethod httpMethod;

    public Map<String, String> requestHeaders;


    public String getNotifyUrlPath() {
        return notifyUrlPath;
    }

    public HttpMethod getHttpMethod() {
        return httpMethod;
    }

    public Map<String, String> getRequestHeaders() {
        return requestHeaders;
    }

    public void setNotifyUrlPath(String notifyUrlPath) {
        this.notifyUrlPath = notifyUrlPath;
    }

    public void setHttpMethod(HttpMethod httpMethod) {
        this.httpMethod = httpMethod;
    }

    public void setRequestHeaders(Map<String, String> requestHeaders) {
        this.requestHeaders = requestHeaders;
    }
}
