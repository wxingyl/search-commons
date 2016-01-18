package com.tqmall.search.common;

import com.tqmall.search.common.utils.HttpUtils;
import com.tqmall.search.common.utils.SearchStringUtils;
import lombok.Data;
import org.apache.commons.lang3.StringUtils;
import org.junit.Assert;
import org.junit.Test;

import java.net.URL;
import java.util.LinkedHashMap;
import java.util.List;
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
        TinyUrl tinyUrl = HttpUtils.buildPost().setBody("url=http://help.baidu.com/question?prod_en=webmaster", false)
                .setUrl(HttpUtils.buildURL("dwz.cn", "create.php"))
                .addHeader("Content-Type", "application/x-www-form-urlencoded; charset=UTF-8")
                .request(HttpUtils.jsonStrValueConvert(TinyUrl.class));
        System.out.println(tinyUrl);
        Assert.assertTrue(tinyUrl.getStatus() == 0);
    }

    @Data
    static class TinyUrl {

        private String tinyurl;

        private Integer status;

        private String longurl;

        private String err_msg;

    }

    @Data
    static class CategoryEntry {

        private Integer id;

        private String name;

        private String vehicleCode;

        private List<CategoryEntry> list;
    }

    @Test
    public void stringArrayTrimTest() {
        for (String s : StringUtils.split(" xing ,wang ", ',')) {
            System.out.println("s: " + s + ", length: " + s.length());
        }
        for (String s : SearchStringUtils.stringArrayTrim(StringUtils.split(" xing ,wang ", ','))) {
            Assert.assertTrue(s.length() == 4);
        }
    }

}
