package com.tqmall.search.commons.nlp;

import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created by xing on 16/2/1.
 * 拼音test
 */
public class PyTest {

    /**
     * 获取所有的简体中文字, 保存到文件simple.txt中
     */
    @Ignore
    public void setAllZhCnCharacter() {
        List<Character> simpleList = new ArrayList<>();
        TraditionToSimple traditionToSimple = TraditionToSimple.instance();
        for (char ch = NlpConst.CJK_UNIFIED_IDEOGRAPHS_FIRST; ch <= NlpConst.CJK_UNIFIED_IDEOGRAPHS_LAST; ch++) {
            if (!traditionToSimple.isTraditional(ch)) {
                simpleList.add(ch);
            }
        }
        try (BufferedWriter writer = new BufferedWriter(new FileWriter("/Users/xing/build/nlp/simple.txt"))) {
            for (Character ch : simpleList) {
                writer.write(ch.toString());
                writer.newLine();
            }
            writer.flush();
            System.out.println("write size: " + simpleList.size());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void pyTest() {
        PinyinConvert pinyinConvert = PinyinConvert.instance();
        String text = "小时了了，大未必佳";
        String excepted = "xiaoshiliaoliao，daweibijia";
        String pyText = pinyinConvert.convert(text, NlpConst.APPEND_CHAR_OTHER);
        System.out.println("text: " + text + ": " + pyText);
        Assert.assertEquals(excepted, pyText);
        text = "长沙";
        Map.Entry<String, String> exceptedEntry = new AbstractMap.SimpleImmutableEntry<>("changsha", "cs");
        Map.Entry<String, String> flResult = pinyinConvert.firstLetterConvert(text, 0);
        System.out.println("text: " + text + ": " + exceptedEntry);
        Assert.assertEquals(exceptedEntry, flResult);
    }
}
