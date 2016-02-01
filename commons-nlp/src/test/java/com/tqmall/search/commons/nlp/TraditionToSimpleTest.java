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
        Assert.assertTrue(NlpUtils.cjkConvert('簡') == '简');
        String str = "简体";
        Assert.assertTrue(str == NlpUtils.traditionalConvert(str));
        str = "簡体";
        Assert.assertEquals("简体", NlpUtils.traditionalConvert(str));
        str = "电费測試繁體轉簡體";
        Assert.assertEquals("电费测试繁体转简体", NlpUtils.traditionalConvert(str));
        str = "电费as測試繁12體轉xing簡體";
        Assert.assertEquals("电费as测试12繁体xing简体", NlpUtils.traditionalConvert(str));
        str = "head电费as測試繁12體轉xing簡體";
        Assert.assertEquals("head电费as测试12繁体xing简体", NlpUtils.traditionalConvert(str));
        str = "12电费as測試繁12體轉xing簡體";
        Assert.assertEquals("12电费as测试12繁体xing简体", NlpUtils.traditionalConvert(str));
    }
}
