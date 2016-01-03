package com.tqmall.search.common;

import com.tqmall.search.common.result.PageResult;
import com.tqmall.search.common.result.Result;
import com.tqmall.search.common.result.ResultUtils;
import com.tqmall.search.common.utils.ResultJsonConverts;
import org.junit.Test;

import java.util.List;

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
}
