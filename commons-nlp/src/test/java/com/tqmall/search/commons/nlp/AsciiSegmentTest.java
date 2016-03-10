package com.tqmall.search.commons.nlp;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by xing on 16/3/10.
 * Ascii分词测试
 *
 * @author xing
 */
public class AsciiSegmentTest {

    private static final List<String> texts = new ArrayList<>();

    @BeforeClass
    public static void init() {
        texts.add("xing09 tqmall5.6ssd");
        texts.add("xing09.   tqmall5..6.9ssd");
        texts.add(".89.6xing09.  tqmall5..6.9ssd78.0.9.78");
        texts.add(".89.6xing-wang");
        texts.add(".89.6..-xing-wang");
        texts.add(".89.6..-xing--wang");
        texts.add(".89.6..-xing-wang-yan");
    }

    @AfterClass
    public static void clear() {
        texts.clear();
    }

    @Test
    public void segmentTest() {
        runTest(new AsciiSegment(SegmentType.MIN));
        runTest(new AsciiSegment(SegmentType.MAX));
        runTest(new AsciiSegment(SegmentType.FULL));
    }

    private void runTest(AsciiSegment segment) {
        System.out.println("\nAsciiSegment: " + segment.getSegmentType());
        for (String str : texts) {
            System.out.println('\"' + str + "\"-->" + segment.match(str.toCharArray()));
        }
    }
}
