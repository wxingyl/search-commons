package com.tqmall.search.commons.nlp;

import com.tqmall.search.commons.analyzer.AsciiAnalyzer;
import com.tqmall.search.commons.analyzer.CjkAnalyzer;
import com.tqmall.search.commons.analyzer.CjkLexicon;
import com.tqmall.search.commons.lang.Supplier;
import com.tqmall.search.commons.match.Hits;
import com.tqmall.search.commons.trie.RootNodeType;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by xing on 16/3/14.
 *
 * @author xing
 */
public class SegmentTest {

    private static Segment fullSegment;

    private static Segment maxSegment;

    private static Segment minSegment;

    @BeforeClass
    public static void init() {
        Supplier<CjkLexicon> cjkLexicon = CjkLexicon.createAsyncSupplier(RootNodeType.CJK,
                NlpUtils.getPathOfClass(SegmentTest.class, "/segment.txt"));
        fullSegment = Segment.build("full")
                .segmentFilter(SegmentFilters.textFilter())
                .asciiAnalyzer(AsciiAnalyzer.build()
                        .enMixAppend(true)
                        .create())
                .appendNumQuantifier(true)
                .cjkSegmentType(CjkAnalyzer.Type.FULL)
                .create(cjkLexicon);
        maxSegment = Segment.build("max")
                .segmentFilter(SegmentFilters.textFilter())
                .asciiAnalyzer(AsciiAnalyzer.build()
                        .enMixAppend(false)
                        .create())
                .appendNumQuantifier(false)
                .cjkSegmentType(CjkAnalyzer.Type.MAX)
                .create(cjkLexicon);
        minSegment = Segment.build("min")
                .segmentFilter(SegmentFilters.hitsFilter())
                //asciiSegment使用默认的
                //不使用数量词合并
//                .appendNumQuantifier(false)
                .cjkSegmentType(CjkAnalyzer.Type.MIN)
                .create(cjkLexicon);
    }

    @Test
    public void segmentTest() {
        List<String> texts = new ArrayList<>();
        texts.add("Xing-Wang0.5元, 大連理工大学六十年校庆, 500人不是参加华中科技大学");
        texts.add("北京大学");
        texts.add("北京的大学");
        texts.add("商品服务");
        texts.add("商品和服务");
        texts.add("商品和氏璧");
        texts.add("B-tree中的每个结点根据实际情况可以包含大量的关键字信息");
        texts.add("东方不败笑傲江湖都是好看的电视剧");
        texts.add("商品共和服");
        texts.add("严守一把手机关了");
        texts.add("吉林省长春药店");
        texts.add("代表北大的人大代表，代表人大的北大博士");
        texts.add("江阴毛纺织厂");
        texts.add("薄熙来到重庆");
        texts.add("周杰轮周杰伦，范伟骑范玮琪");
        texts.add("结婚的和尚未结婚的");
        texts.add("北京大学生前来应聘");
        texts.add("高数学起来很难");
        texts.add("博观是什么时候加入阿里的？");
        texts.add("蚂蚁金服的员工都分布在哪些工作地点？");
        texts.add("北京大学生前来应聘");
        for (String text : texts) {
            System.out.println("text: " + text);
            char[] array = text.toCharArray();
            System.out.println("fullSegment: " + Hits.valueOf(array, fullSegment.match(array, 0, array.length)));
            System.out.println("maxSegment: " + Hits.valueOf(array, maxSegment.match(array, 0, array.length)));
            System.out.println("minSegment: " + Hits.valueOf(array, minSegment.match(array, 0, array.length)));
            break;
        }
    }

}
