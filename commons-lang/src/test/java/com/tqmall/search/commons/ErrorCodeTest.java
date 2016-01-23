package com.tqmall.search.commons;

import com.tqmall.search.commons.lang.ErrorCodeEntry;
import com.tqmall.search.commons.utils.ErrorCodeUtils;
import org.junit.Test;

/**
 * Created by xing on 16/1/4.
 * ErrorCode相关测试
 */
public class ErrorCodeTest {

    @Test
    public void parseTest() {
        ErrorCodeEntry entry = ErrorCodeUtils.parseCode("00010023");
        System.out.println(entry);
        entry = ErrorCodeUtils.parseCode("80121023");
        System.out.println(entry);
    }

}
