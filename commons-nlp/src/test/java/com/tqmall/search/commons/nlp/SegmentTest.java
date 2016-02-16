package com.tqmall.search.commons.nlp;

import org.junit.Test;

import java.util.List;

/**
 * Created by xing on 16/2/11.
 * segment分词测试
 */
public class SegmentTest {

    @Test
    public void segmentTest() {
        String text = "北京大学";
        List<Hit<Void>> list;
        list = NlpUtils.segmentText(text);
        System.out.println(text + ": " + list);
        text = "北京的大学";
        list = NlpUtils.segmentText(text);
        System.out.println(text + ": " + list);
        text = "商品和服务";
        list = NlpUtils.segmentText(text);
        System.out.println(text + ": " + list);
        text = "商品和氏璧";
        list = NlpUtils.segmentText(text);
        System.out.println(text + ": " + list);
        text = "B-tree中的每个结点根据实际情况可以包含大量的关键字信息";
        list = NlpUtils.segmentText(text);
        System.out.println(text + ": " + list);
    }
}
