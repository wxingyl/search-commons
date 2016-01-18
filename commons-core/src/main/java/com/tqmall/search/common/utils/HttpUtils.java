package com.tqmall.search.common.utils;

import com.tqmall.search.common.param.Param;
import com.tqmall.search.common.result.MapResult;
import com.tqmall.search.common.result.PageResult;
import com.tqmall.search.common.result.Result;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
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
     * 如果是jdk1.8就好了, 接口里面默认实现toString()方法
     */
    public static String hostInfoToString(HostInfo hostInfo) {
        return hostInfo.getIp() + ':' + hostInfo.getPort();
    }

    /**
     * hostInfo 检查, 如果检查不通过, 抛出{@link IllegalArgumentException}
     *
     * @param hostInfo 入参, host
     * @param wrapHost 如果host.getIp()为空, 是否需要使用本地Ip包装
     * @return hostInfo, 如果需要包装, 则返回本地ip的包装
     */
    public static HostInfo hostInfoCheck(final HostInfo hostInfo, boolean wrapHost) {
        if (hostInfo == null) {
            throw new IllegalArgumentException("hostInfoCheck: hostInfo is null");
        }
        if (hostInfo.getPort() <= 0) {
            throw new IllegalArgumentException("hostInfoCheck: localPort " + hostInfo.getPort() + " <= 0 is invalid");
        }
        if (StringUtils.isEmpty(hostInfo.getIp())) {
            if (wrapHost) {
                return new HostInfo() {

                    //传进来的ip不好使就用默认的
                    @Override
                    public String getIp() {
                        return HttpUtils.LOCAL_IP;
                    }

                    @Override
                    public int getPort() {
                        return hostInfo.getPort();
                    }
                };
            } else {
                throw new IllegalArgumentException("hostInfoCheck: ip is empty");
            }
        } else {
            return hostInfo;
        }
    }

    /**
     * 两个HostInfo 比较是否相同
     */
    public static boolean isEquals(HostInfo a, HostInfo b) {
        if (a == b) return true;
        else if (a == null || b == null) return false;
        else return Objects.equals(a.getIp(), b.getIp()) && a.getPort() == b.getPort();
    }

    /**
     * 过滤urlPath, 如果其以'/'开头或者结尾, 则去掉
     * eg: /goods/convert/ ---> goods/convert
     *
     * @param urlPath 不能为null或者为空
     * @return 过滤结果
     */
    public static String filterUrlPath(String urlPath) {
        urlPath = Param.filterString(urlPath);
        Objects.requireNonNull(urlPath);
        int start = 0, end = urlPath.length();
        if (urlPath.charAt(0) == '/') {
            start++;
        }
        if (urlPath.charAt(end - 1) == '/') {
            end--;
        }
        if (start > 0 || end < urlPath.length()) {
            urlPath = urlPath.substring(start, end);
        }
        return urlPath;
    }

    /**
     * 构建一个StrValueConvert对象, 输入Json字符串, 根据Class对象, 通过Json转换得到该实例
     */
    public static <T> StrValueConvert<T> jsonStrValueConvert(final Class<T> cls) {
        return new StrValueConvert<T>() {
            @Override
            public T convert(String input) {
                if (input == null) return null;
                return JsonUtils.jsonStrToObj(input, cls);
            }
        };
    }


    public static URL buildURL(HostInfo host, String path) {
        return buildURL(host, path, "");
    }

    public static URL buildURL(HostInfo host, String path, Map<String, Object> param) {
        return buildURL(hostInfoToString(host), path, param);
    }

    public static URL buildURL(HostInfo host, String path, String param) {
        return buildURL(hostInfoToString(host), path, param);
    }

    public static URL buildURL(String host, String path) {
        return buildURL(host, path, "");
    }

    public static URL buildURL(String host, String path, Map<String, Object> param) {
        if (param == null || param.isEmpty()) {
            return buildURL(host, path, "");
        } else {
            StringBuilder sb = new StringBuilder(128); //default is 16, maybe too small
            for (Map.Entry<String, Object> e : param.entrySet()) {
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
            urlBuild.append(filterUrlPath(path));
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
            throw new IllegalArgumentException(e);
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
     * 默认的http get请求, 以json格式发送数据, 返回结果为{@link Result}格式
     */
    public static <T> Result<T> requestGetResult(URL url, Class<T> cls) {
        return requestGet(url, ResultJsonConverts.resultConvert(cls));
    }

    /**
     * 默认的http get请求, 以json格式发送数据, 返回结果为{@link PageResult}格式
     */
    public static <T> PageResult<T> requestGetPageResult(URL url, Class<T> cls) {
        return requestGet(url, ResultJsonConverts.pageResultConvert(cls));
    }

    /**
     * 默认的http get请求, 以json格式发送数据, 返回结果为{@link MapResult}格式
     */
    public static MapResult requestGetMapResult(URL url) {
        return requestGet(url, ResultJsonConverts.mapResultConvert());
    }

    /**
     * 默认的http get请求
     */
    public static <T> T requestGet(URL url, StrValueConvert<T> convert) {
        GetRequest getRequest = new GetRequest();
        return getRequest.setUrl(url).request(convert);
    }


    /**
     * 默认的http post请求, 以json格式发送数据, 返回结果为{@link Result}格式
     *
     * @param body 可以为null
     */
    public static <T> Result<T> requestPostResult(URL url, Object body, Class<T> cls) {
        return requestPost(url, body, ResultJsonConverts.resultConvert(cls));
    }

    /**
     * 默认的http post请求, 以json格式发送数据, 返回结果为{@link PageResult}格式
     *
     * @param body 可以为null
     */
    public static <T> PageResult<T> requestPostPageResult(URL url, Object body, Class<T> cls) {
        return requestPost(url, body, ResultJsonConverts.pageResultConvert(cls));
    }

    /**
     * 默认的http post请求, 以json格式发送数据, 返回结果为{@link MapResult}格式
     *
     * @param body 可以为null
     */
    public static MapResult requestPostMapResult(URL url, Object body) {
        return requestPost(url, body, ResultJsonConverts.mapResultConvert());
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
                throw new IllegalArgumentException("Nonsupport for http method: " + method);
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

    /**
     * 注意: 同一个该类实例对象,执行request时线程不安全的
     */
    public static abstract class RequestBase {

        private Map<String, String> headerMap = new HashMap<>();

        private URL url;

        private Config config;

        /**
         * 记录Http请求log, 通过{@link Logger#info(String)}记录, 默认开启
         */
        private boolean requestLogSwitch = true;
        /**
         * http返回状态码
         */
        private int responseCode;

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

        /**
         * 请求的url通过方法{@link #setUrl(URL)}设定
         * 请求的addHeader通过方法{@link #addHeader(Map)}或者{@link #addHeader(String, String)}设定
         * 请求的一些其他配置通过方法{@link #setConfig(Config)}设定, 提供了默认配置{@link Config#DEFAULT}
         * 请求时log打印通过{@link #setRequestLogSwitch(boolean)}设定, 默认是开启的
         *
         * @param convert 如果为null, 说明不需要返回结果, 则返回null
         * @return 参数convert为null或者请求发生异常则返回null, 判断请求是否成功执行,可通过方法{@link #getResponseCode()} ()}查看
         * @see #getResponseCode()
         * @see #setUrl(URL)
         * @see #setConfig(Config)
         * @see #setRequestLogSwitch(boolean)
         * @see com.tqmall.search.common.utils.HttpUtils.Config#DEFAULT
         */
        public <T> T request(StrValueConvert<T> convert) {
            Objects.requireNonNull(url);
            HttpURLConnection httpConnection = null;
            try {
                if (requestLogSwitch) {
                    log.info("Http请求, url: " + url + ", Method: " + getMethod());
                }
                responseCode = -1;
                //都是使用的http协议调用,所以可以直接强转
                httpConnection = (HttpURLConnection) url.openConnection();
                httpConnection.setRequestMethod(getMethod());
                for (Map.Entry<String, String> e : headerMap.entrySet()) {
                    httpConnection.addRequestProperty(e.getKey(), e.getValue());
                }
                Config localConfig = getConfig();
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
                responseCode = httpConnection.getResponseCode();
                httpConnection = null;
                if (convert == null) {
                    if (requestLogSwitch) {
                        log.info("Http请求结果: " + response.toString());
                    }
                    return null;
                } else {
                    return convert.convert(response.toString());
                }
            } catch (IOException e) {
                if (responseCode == -1) {
                    if (e instanceof ConnectException) {
                        responseCode = 404;
                    } else if (httpConnection != null) {
                        try {
                            responseCode = httpConnection.getResponseCode();
                        } catch (IOException e1) {
                            log.warn("获取HttpResponseCode 异常: " + e.getMessage());
                            //野蛮暴力一把吧!!!
                            responseCode = 404;
                        }
                    }
                }
                log.error("Http请求异常: " + url + ", responseCode: " + responseCode + ", Method: " + getMethod(), e);
                //如果失败了, convert不为null则返回转换结果, 为null也就不需要返回结果了
                return convert == null ? null : convert.convert(null);
            }
        }

        protected Config getConfig() {
            return config == null ? Config.DEFAULT : config;
        }

        public int getResponseCode() {
            return responseCode;
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
                addHeader("Content-Type", "application/json;charset=UTF-8");
            }
            return this;
        }

        @Override
        protected void doHttpURLConnection(HttpURLConnection httpUrlConnection) throws IOException {
            httpUrlConnection.setDoOutput(true);
            if (body != null) {
                //其会自动掉用httpUrlConnection.connect()方法
                try (PrintStream out = new PrintStream(httpUrlConnection.getOutputStream(), true, getConfig().getCharsetName())) {
                    out.print(body);
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
