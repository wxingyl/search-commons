package com.tqmall.search.common;

import com.google.common.collect.Maps;
import com.tqmall.search.common.utils.HttpUtils;
import com.tqmall.search.common.utils.JsonUtils;
import com.tqmall.search.common.utils.StrValueConvert;
import org.junit.Assert;
import org.junit.Test;

import java.net.URL;
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
        Map<String, String> param = Maps.newLinkedHashMap();
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
        TinyUrl tinyUrl = HttpUtils.buildPost().setBody("url=http://help.baidu.com/question?prod_en=webmaster", false)
                .setUrl(HttpUtils.buildURL("dwz.cn", "create.php"))
                .addHeader("Content-Type", "application/x-www-form-urlencoded; charset=UTF-8")
                .request(new StrValueConvert<TinyUrl>() {
                    @Override
                    public TinyUrl convert(String input) {
                        return JsonUtils.jsonStrToObj(input, TinyUrl.class);
                    }
                });
        System.out.println(tinyUrl);
        Assert.assertTrue(tinyUrl.getStatus() == 0);
    }

    static class TinyUrl {

        private String tinyurl;

        private Integer status;

        private String longurl;

        private String err_msg;

        public String getErr_msg() {
            return err_msg;
        }

        public String getLongurl() {
            return longurl;
        }

        public Integer getStatus() {
            return status;
        }

        public String getTinyurl() {
            return tinyurl;
        }

        public void setErr_msg(String err_msg) {
            this.err_msg = err_msg;
        }

        public void setLongurl(String longurl) {
            this.longurl = longurl;
        }

        public void setStatus(Integer status) {
            this.status = status;
        }

        public void setTinyurl(String tinyurl) {
            this.tinyurl = tinyurl;
        }

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
