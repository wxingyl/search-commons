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
                .segmentFilter(SegmentFilters.hitsFilter())
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
        for (String text : texts) {
            System.out.println("text: " + text);
            char[] array = text.toCharArray();
            System.out.println("fullSegment: " + Hits.valueOf(array, fullSegment.match(array, 0, array.length)));
            System.out.println("maxSegment: " + Hits.valueOf(array, maxSegment.match(array, 0, array.length)));
            System.out.println("minSegment: " + Hits.valueOf(array, minSegment.match(array, 0, array.length)));
        }
    }
}
