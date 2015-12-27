package com.tqmall.search.common.utils;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.*;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Created by xing on 15/12/24.
 * Http 请求工具类
 * 该类的请求是通过Java的基本方法实现,没有考虑多请求下频繁建立网络连接的情况, 如果需要, 那就直接搞Apache HttpClient
 */
public abstract class HttpUtils {

    private static final Logger log = LoggerFactory.getLogger(HttpUtils.class);

    public static final String GET_METHOD = "GET";

    public static final String POST_METHOD = "POST";

    public static final String PUT_METHOD = "PUT";

    public static final String DELETE_METHOD = "DELETE";

    public static final String LOCAL_IP;

    static {
        String ip;
        try {
            ip = InetAddress.getLocalHost().getHostAddress();
        } catch (UnknownHostException e) {
            log.error("获取本地Ip失败", e);
            ip = null;
        }
        LOCAL_IP = ip;
    }


    /**
     * 构建一个StrValueConvert对象, 输入Json字符串, 根据Class对象, 通过Json转换得到该实例
     */
    public static <T> StrValueConvert<T> jsonStrValueConvert(final Class<T> cls) {
        return new StrValueConvert<T>() {
            @Override
            public T convert(String input) {
                return JsonUtils.jsonStrToObj(input, cls);
            }
        };
    }



    public static URL buildURL(String host, String path) {
        return buildURL(host, path, "");
    }

    public static URL buildURL(String host, String path, Map<String, String> param) {
        if (param == null || param.isEmpty()) {
            return buildURL(host, path, "");
        } else {
            StringBuilder sb = new StringBuilder(128); //default is 16, maybe too small
            for (Map.Entry<String, String> e : param.entrySet()) {
                sb.append(e.getKey()).append('=').append(e.getValue()).append('&');
            }
            sb.deleteCharAt(sb.length() - 1);
            return buildURL(host, path, sb.toString());
        }
    }

    /**
     * 单纯的创建URL对象
     */
    public static URL buildURL(String host, String path, String param) {
        Objects.requireNonNull(host);
        if (!host.startsWith("http://")) {
            host = "http://" + host;
        }
        StringBuilder urlBuild = new StringBuilder(host);
        if (host.charAt(host.length() - 1) != '/') {
            urlBuild.append('/');
        }
        if (!StringUtils.isEmpty(path)) {
            int start = 0, end = path.length();
            if (path.charAt(0) == '/') {
                start = 1;
            }
            if (path.charAt(end - 1) == '/') {
                end--;
            }
            if (start != 0 || end != path.length()) {
                path = path.substring(start, end);
            }
            urlBuild.append(path);
        } else {
            urlBuild.deleteCharAt(urlBuild.length() - 1);
        }
        if (!StringUtils.isEmpty(param)) {
            urlBuild.append('?').append(param);
        }
        try {
            return new URL(urlBuild.toString());
        } catch (MalformedURLException e) {
            log.warn("创建url存在异常, host: " + host + ", path: " + path + ", param: " + param + ": " + e.getMessage());
            throw new IllegalArgumentException(e.getMessage());
        }
    }

    /**
     * 只返回String的Http Get请求
     */
    public static String requestGet(URL url) {
        GetRequest getRequest = new GetRequest();
        return getRequest.setUrl(url).request(StrValueConverts.getConvert(String.class));
    }

    /**
     * 默认的http get请求
     */
    public static <T> T requestGet(URL url, StrValueConvert<T> convert) {
        GetRequest getRequest = new GetRequest();
        return getRequest.setUrl(url).request(convert);
    }

    /**
     * 默认的http post请求, 以json格式发送数据, 并且返回结果通过Json解析
     *
     * @param body 可以为null
     */
    public static <T> T requestPost(URL url, Object body, Class<T> cls) {
        return requestPost(url, body, jsonStrValueConvert(cls));
    }

    /**
     * 默认的http post请求, 以json格式发送数据
     *
     * @param body 可以为null
     */
    public static <T> T requestPost(URL url, Object body, StrValueConvert<T> convert) {
        PostRequest postRequest = new PostRequest();
        return postRequest.setBody(body, true).setUrl(url).request(convert);
    }

    public static GetRequest buildGet() {
        return new GetRequest();
    }

    public static PostRequest buildPost() {
        return new PostRequest();
    }

    public static RequestBase build(String method) {
        //HTTP 1.1 Method 是区分大小写的,所以这儿不做小写的处理
        RequestBase request;
        switch (method) {
            case GET_METHOD:
                request = new GetRequest();
                break;
            case POST_METHOD:
                request = new PostRequest();
                break;
            case PUT_METHOD:
                request = new PutRequest();
                break;
            case DELETE_METHOD:
                request = new DeleteRequest();
                break;
            default:
                throw new IllegalArgumentException("nonsupport for Http method: " + method);
        }
        return request;
    }

    /**
     * Http请求的一些配置
     */
    public static class Config {

        /**
         * 默认的连接超时时间3s,读取超时时间10s
         */
        static final Config DEFAULT = new Config(3 * 1000, 10 * 1000, 1024, "UTF-8");
        /**
         * 单位ms
         */
        private int connectTimeout;
        /**
         * 单位ms
         */
        private int readTimeout;

        private int readBufferSize;

        private String charsetName;

        public Config(int connectTimeout, int readTimeout, int readBufferSize, String charsetName) {
            this.connectTimeout = connectTimeout;
            this.readTimeout = readTimeout;
            this.readBufferSize = readBufferSize;
            if (!StringUtils.isEmpty(charsetName)) {
                this.charsetName = charsetName;
            }
        }

        public String getCharsetName() {
            return charsetName;
        }

        public int getConnectTimeout() {
            return connectTimeout;
        }

        public int getReadBufferSize() {
            return readBufferSize;
        }

        public int getReadTimeout() {
            return readTimeout;
        }
    }

    public static abstract class RequestBase {

        private Map<String, String> headerMap = new HashMap<>();

        private URL url;

        protected Config config;

        /**
         * 记录Http请求log, 通过{@link Logger#info(String)}记录, 默认开启
         */
        private boolean requestLogSwitch = true;

        /**
         * 默认都为长连接, 以json接收数据
         */
        public RequestBase() {
            addHeader("Connection", "keep-alive");
        }

        public abstract String getMethod();

        protected abstract void doHttpURLConnection(HttpURLConnection httpUrlConnection) throws IOException;

        public RequestBase setUrl(URL url) {
            Objects.requireNonNull(url);
            this.url = url;
            return this;
        }

        /**
         * @param config 为null则使用默认配置{@link Config#DEFAULT}
         */
        public RequestBase setConfig(Config config) {
            this.config = config;
            return this;
        }

        public RequestBase setRequestLogSwitch(boolean requestLogSwitch) {
            this.requestLogSwitch = requestLogSwitch;
            return this;
        }

        public RequestBase addHeader(Map<String, String> headers) {
            if (headers != null && !headers.isEmpty()) {
                for (Map.Entry<String, String> e : headers.entrySet()) {
                    addHeader(e.getKey(), e.getValue());
                }
            }
            return this;
        }

        /**
         * 如果value 为null,则删除name对应的header
         *
         * @param name HTTP 1.1 Header 头的name大小写不区分,所以我们全部转为小写
         */
        public RequestBase addHeader(String name, String value) {
            Objects.requireNonNull(name);
            if (!StringUtils.isAllLowerCase(name)) {
                name = name.toLowerCase();
            }
            if (value == null) {
                headerMap.remove(name);
            } else {
                headerMap.put(name, value);
            }
            return this;
        }

        public <T> T request(StrValueConvert<T> convert) {
            Objects.requireNonNull(url);
            try {
                if (requestLogSwitch) {
                    log.info("Http请求, url: " + url + ", Method: " + getMethod());
                }
                //都是使用的http协议调用,所以可以直接强转
                HttpURLConnection httpConnection = (HttpURLConnection) url.openConnection();
                httpConnection.setRequestMethod(getMethod());
                for (Map.Entry<String, String> e : headerMap.entrySet()) {
                    httpConnection.addRequestProperty(e.getKey(), e.getValue());
                }
                final Config localConfig = config == null ? Config.DEFAULT : config;
                httpConnection.setConnectTimeout(localConfig.getConnectTimeout());
                httpConnection.setReadTimeout(localConfig.getReadTimeout());
                doHttpURLConnection(httpConnection);
                //默认1024大小,返回结果数据还是比较多的一把情况下
                StringBuilder response = new StringBuilder(localConfig.getReadBufferSize());
                try (BufferedReader in = new BufferedReader(new InputStreamReader(
                        httpConnection.getInputStream(), localConfig.getCharsetName()))) {
                    String line;
                    while ((line = in.readLine()) != null) {
                        response.append(line);
                    }
                }
                if (convert == null) {
                    if (requestLogSwitch) {
                        log.info("Http请求结果: " + response.toString());
                    }
                    return null;
                } else {
                    return convert.convert(response.toString());
                }
            } catch (IOException e) {
                //填充log
                log.error("Http请求" + url + ", Method: " + getMethod(), e);
                return null;
            }
        }
    }

    public static class GetRequest extends RequestBase {

        @Override
        public String getMethod() {
            return GET_METHOD;
        }

        @Override
        protected void doHttpURLConnection(HttpURLConnection httpUrlConnection) throws IOException {
            httpUrlConnection.setDoInput(true);
            httpUrlConnection.connect();
        }
    }

    /**
     * 请求中带Body的Http Method
     */
    public static abstract class HandleBodyRequest extends RequestBase {

        private String body;

        /**
         * @param body   实体内容
         * @param isJson 是否为json数据,如果是,则添加Content-Type头
         */
        public HandleBodyRequest setBody(Object body, boolean isJson) {
            if (body == null) {
                this.body = null;
            } else {
                if (body instanceof CharSequence || !isJson) {
                    this.body = body.toString();
                } else {
                    this.body = JsonUtils.objToJsonStr(body);
                }
            }
            if (isJson) {
                addHeader("Content-Type", "application/json,charset=UTF-8");
            }
            return this;
        }

        @Override
        protected void doHttpURLConnection(HttpURLConnection httpUrlConnection) throws IOException {
            httpUrlConnection.setDoOutput(true);
            if (body != null) {
                //其会自动掉用httpUrlConnection.connect()方法
                try (PrintWriter out = new PrintWriter(httpUrlConnection.getOutputStream())) {
                    out.print(body);
                    out.flush();
                }
            }
        }
    }

    public static class PostRequest extends HandleBodyRequest {

        @Override
        public String getMethod() {
            return POST_METHOD;
        }

    }

    /**
     * 跟Post没啥区别了,就是个Method Name区分
     */
    public static class PutRequest extends HandleBodyRequest {
        @Override
        public String getMethod() {
            return PUT_METHOD;
        }
    }

    /**
     * 跟Post没啥区别了,就是个Method Name区分
     */
    public static class DeleteRequest extends HandleBodyRequest {
        @Override
        public String getMethod() {
            return DELETE_METHOD;
        }
    }

}
