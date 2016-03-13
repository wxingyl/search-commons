package com.tqmall.search.commons.nlp;

import org.junit.BeforeClass;
import org.junit.Test;

import java.io.IOException;
import java.io.InputStream;

/**
 * Created by xing on 16/3/14.
 *
 * @author xing
 */
public class SegmentTest {

    private static Segment fullSegment;

    @BeforeClass
    public static void init() {
        CjkLexicon cjkLexicon;
        try (InputStream in = CjkLexiconTest.class.getResourceAsStream("/segment.txt")) {
            cjkLexicon = new CjkLexicon(in);
        } catch (IOException e) {
            throw new RuntimeException("词库文件加载失败", e);
        }
        fullSegment = Segment.build()
                .parseDecimal(true)
                .enMixAppend(true)
                .appendNumQuantifier(true)
                .cjkSegmentType(SegmentType.FULL)
                .create(cjkLexicon);
    }

    @Test
    public void segmentTest() {
        System.out.println(fullSegment.match("xing-wang0.5元, 大连理工大学六十年校庆, 500人参加".toCharArray()));
    }
}
