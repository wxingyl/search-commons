package com.tqmall.search.commons;

import com.tqmall.search.commons.utils.HttpMethod;
import com.tqmall.search.commons.utils.HttpUtils;
import org.junit.Assert;
import org.junit.Test;

import java.net.URL;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Created by xing on 15/12/25.
 * HttpUtils test class
 */
public class HttpUtilsTest {

    @Test
    public void buildUrlTest() {
        URL uri = HttpUtils.buildURL("www.baidu.com", null);
        Assert.assertTrue(uri.toString().equals("http://www.baidu.com"));
        uri = HttpUtils.buildURL("www.baidu.com/", "/search/");
        Assert.assertTrue(uri.toString().equals("http://www.baidu.com/search"));
        Map<String, Object> param = new LinkedHashMap<>();
        param.put("key", "大连");
        param.put("args", "search");
        uri = HttpUtils.buildURL("http://www.baidu.com/", "/search/", param);
        Assert.assertTrue(uri.toString().equals("http://www.baidu.com/search?key=大连&args=search"));
        uri = HttpUtils.buildURL("http://www.baidu.com/", "/search", "key=大连&args=search");
        Assert.assertTrue(uri.toString().equals("http://www.baidu.com/search?key=大连&args=search"));
    }

    @Test
    public void httpMethodTest() {
        HttpUtils.requestGet(HttpUtils.buildURL("www.baidu.com", null));
        TinyUrl tinyUrl = HttpMethod.POST.<HttpUtils.PostRequest>build().setBody("url=http://help.baidu.com/question?prod_en=webmaster", false)
                .setUrl(HttpUtils.buildURL("dwz.cn", "create.php"))
                .addHeader("Content-Type", "application/x-www-form-urlencoded; charset=UTF-8")
                .request(HttpUtils.jsonStrValueConvert(TinyUrl.class));
        System.out.println(tinyUrl);
        Assert.assertNotNull(tinyUrl);
        Assert.assertTrue(tinyUrl.status == 0);
    }

    static class TinyUrl {

        private String tinyurl;

        private Integer status;

        private String longurl;

        private String err_msg;

        @Override
        public String toString() {
            return "TinyUrl{" +
                    "err_msg='" + err_msg + '\'' +
                    ", tinyurl='" + tinyurl + '\'' +
                    ", status=" + status +
                    ", longurl='" + longurl + '\'' +
                    '}';
        }
    }

}
