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
        TraditionToSimple traditionToSimple = new TraditionToSimple();
        Assert.assertFalse(traditionToSimple.isTraditional('简'));
        Assert.assertTrue(traditionToSimple.isTraditional('簡'));
        Assert.assertTrue(traditionToSimple.convert('簡') == '简');
        String str = "简体";
        Assert.assertTrue(str == traditionToSimple.convert(str));
        str = "簡体";
        Assert.assertEquals("简体", traditionToSimple.convert(str));
        str = "电费測試繁體轉簡體";
        Assert.assertEquals("电费测试繁体转简体", traditionToSimple.convert(str));
        str = "电费as測試繁12體轉xing簡體";
        Assert.assertEquals("电费as测试繁12体转xing简体", traditionToSimple.convert(str));
        str = "head电费as測試12繁體轉xing簡體";
        Assert.assertEquals("head电费as测试12繁体转xing简体", traditionToSimple.convert(str));
    }
}
