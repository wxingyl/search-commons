package com.tqmall.search.commons.nlp;

import org.junit.BeforeClass;
import org.junit.Test;

import java.io.IOException;
import java.io.InputStream;
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
        CjkLexicon cjkLexicon;
        try (InputStream in = CjkLexiconTest.class.getResourceAsStream("/segment.txt")) {
            cjkLexicon = new CjkLexicon(in);
        } catch (IOException e) {
            throw new RuntimeException("词库文件加载失败", e);
        }
        fullSegment = Segment.build()
                .segmentFilter(SegmentFilters.hitsFilter())
                .asciiSegment(AsciiSegment.build()
                        .enMixAppend(true)
                        .create())
                .appendNumQuantifier(true)
                .cjkSegmentType(SegmentType.FULL)
                .create(cjkLexicon);
        maxSegment = Segment.build()
                .segmentFilter(SegmentFilters.textFilter())
                .asciiSegment(AsciiSegment.build()
                        .enMixAppend(false)
                        .create())
                .appendNumQuantifier(false)
                .cjkSegmentType(SegmentType.MAX)
                .create(cjkLexicon);
        minSegment = Segment.build()
                .segmentFilter(SegmentFilters.hitsFilter())
                //asciiSegment使用默认的
                //不使用数量词合并
//                .appendNumQuantifier(false)
                .cjkSegmentType(SegmentType.MIN)
                .create(cjkLexicon);
    }

    @Test
    public void segmentTest() {
        List<String> texts = new ArrayList<>();
        texts.add("Xing-Wang0.5元, 大連理工大学六十年校庆, 500人参加华中科技大学");
        for (String text : texts) {
            System.out.println("text: " + text);
            char[] t = text.toCharArray();
            System.out.println("fullSegment: " + fullSegment.match(t));
            System.out.println("maxSegment: " + maxSegment.match(t));
            System.out.println("minSegment: " + minSegment.match(t));
        }
    }
}
