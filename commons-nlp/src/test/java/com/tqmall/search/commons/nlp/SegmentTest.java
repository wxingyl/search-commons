package com.tqmall.search.commons.nlp;

import com.tqmall.search.commons.lang.Function;
import org.junit.Test;

import java.util.List;

/**
 * Created by xing on 16/2/11.
 * segment分词测试
 */
public class SegmentTest {

    @Test
    public void segmentTest() {
        System.out.println("fullSegment");
        runSegment(new Function<String, List<Hit<Void>>>() {
            @Override
            public List<Hit<Void>> apply(String text) {
                return NlpUtils.fullSegmentText(text);
            }
        });
        System.out.println();
        System.out.println("minSegment");
        runSegment(new Function<String, List<Hit<Void>>>() {
            @Override
            public List<Hit<Void>> apply(String text) {
                return NlpUtils.minSegmentText(text);
            }
        });
        System.out.println();
        System.out.println("maxSegment");
        runSegment(new Function<String, List<Hit<Void>>>() {
            @Override
            public List<Hit<Void>> apply(String text) {
                return NlpUtils.maxSegmentText(text);
            }
        });
        System.out.println();
    }

    public void runSegment(Function<String, List<Hit<Void>>> function) {
        String text = "北京大学";
        List<Hit<Void>> list;
        list = function.apply(text);
        System.out.println(text + ": " + list);
        text = "北京的大学";
        list = function.apply(text);
        System.out.println(text + ": " + list);
        text = "商品服务";
        list = function.apply(text);
        System.out.println(text + ": " + list);
        text = "商品和服务";
        list = function.apply(text);
        System.out.println(text + ": " + list);
        text = "商品和氏璧";
        list = function.apply(text);
        System.out.println(text + ": " + list);
        text = "B-tree中的每个结点根据实际情况可以包含大量的关键字信息";
        list = function.apply(text);
        System.out.println(text + ": " + list);
        text = "东方不败笑傲江湖都是好看的电视剧";
        list = function.apply(text);
        System.out.println(text + ": " + list);
        text = "商品共和服";
        list = function.apply(text);
        System.out.println(text + ": " + list);
    }

}
