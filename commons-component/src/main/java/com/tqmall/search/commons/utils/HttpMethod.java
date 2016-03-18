package com.tqmall.search.commons.utils;

/**
 * Created by xing on 16/3/18.
 *
 * @author xing
 */
public enum HttpMethod {
    GET,
    POST,
    PUT,
    DELETE;

    @SuppressWarnings({"rawtypes, unchecked"})
    public <T extends HttpUtils.Request> T build() {
        HttpUtils.Request request;
        switch (this) {
            case GET:
                request = new HttpUtils.GetRequest();
                break;
            case POST:
                request = new HttpUtils.PostRequest();
                break;
            case PUT:
                request = new HttpUtils.PutRequest();
                break;
            case DELETE:
                request = new HttpUtils.DeleteRequest();
                break;
            default:
                request = null;
        }
        return (T) request;
    }
}
