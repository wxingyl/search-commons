package com.tqmall.search.commons.nlp;

import org.junit.Assert;
import org.junit.Test;

/**
 * Created by xing on 16/1/26.
 * 繁体转简体测试类
 */
public class TraditionToSimpleTest {

    @Test
    public void convertTest() {
        Assert.assertFalse(NlpUtils.isTraditional('简'));
        Assert.assertTrue(NlpUtils.isTraditional('簡'));
        Assert.assertTrue(NlpUtils.convert('簡') == '简');
        String str = "简体";
        Assert.assertTrue(str == NlpUtils.convert(str));
        str = "簡体";
        Assert.assertTrue("简体".equals(NlpUtils.convert(str)));
    }
}
