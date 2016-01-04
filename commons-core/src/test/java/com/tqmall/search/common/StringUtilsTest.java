package com.tqmall.search.common;

import com.tqmall.search.common.result.PageResult;
import com.tqmall.search.common.result.Result;
import com.tqmall.search.common.result.ResultUtils;
import com.tqmall.search.common.utils.JsonUtils;
import com.tqmall.search.common.utils.ResultJsonConverts;
import org.junit.Assert;
import org.junit.Test;

import java.util.List;
import java.util.Map;

/**
 * Created by xing on 16/1/3.
 * 字符串处理的相关测试
 */
public class StringUtilsTest {

    @Test
    public void simpleResultTest() {
        String input = "{\"success\":true,\"code\":\"0\",\"message\":\"\",\"data\":2016}";
        Result<Integer> result = ResultJsonConverts.resultConvert(Integer.class).convert(input);
        System.out.println(ResultUtils.resultToString(result));
        input = "{\"success\":true,\"code\":\"0\",\"message\":\"\",\"data\":[\"2016\", \"2017\"]}";
        Result<List> resultList = ResultJsonConverts.resultConvert(List.class).convert(input);
        System.out.println(ResultUtils.resultToString(resultList));
        input = "{\"success\":true,\"code\":\"0\",\"message\":\"\",\"data\":[2016,1025],\"total\":20}";
        PageResult<Integer> pageResult = ResultJsonConverts.pageResultConvert(Integer.class).convert(input);
        System.out.println(ResultUtils.resultToString(pageResult));
    }

    @Test
    public void jsonTest() {
        TestBean bean = new TestBean();
        bean.name = "xingxing.wang";
        bean.id = 1;
        Map<String, Object> map = JsonUtils.objToMap(bean);
        System.out.println(map);
        Assert.assertTrue(map != null);
    }

    static class TestBean {

        private String name;

        private Integer id;

        public void setId(Integer id) {
            this.id = id;
        }

        public void setName(String name) {
            this.name = name;
        }

        public Integer getId() {
            return id;
        }

        public String getName() {
            return name;
        }
    }

}
