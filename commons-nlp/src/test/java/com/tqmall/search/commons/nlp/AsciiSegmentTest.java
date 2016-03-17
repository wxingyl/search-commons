package com.tqmall.search.commons.nlp;

import com.tqmall.search.commons.match.Hit;
import com.tqmall.search.commons.match.TextMatch;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created by xing on 16/3/10.
 * Ascii分词测试
 *
 * @author xing
 */
public class AsciiSegmentTest {

    private static final List<TestTextEntry> textEntryList = new ArrayList<>();

    @BeforeClass
    public static void init() {
        TestTextEntry entry = new TestTextEntry("xing09 78tqmall5.6ssd");
        Collections.addAll(entry.expect1, new Hit<>(0, "xing", TokenType.EN), new Hit<>(4, "09", TokenType.NUM),
                new Hit<>(7, "78", TokenType.NUM), new Hit<>(9, "tqmall", TokenType.EN), new Hit<>(15, "5.6", TokenType.DECIMAL),
                new Hit<>(18, "ssd", TokenType.EN));
        entry.expect2.addAll(entry.expect1);
        entry.expect3.addAll(entry.expect1);
        Collections.addAll(entry.expectMax, new Hit<>(0, "xing09", TokenType.EN_MIX), new Hit<>(7, "78tqmall5.6ssd", TokenType.EN_MIX));
        textEntryList.add(entry);

        entry = new TestTextEntry("xing09.  tqmall5..6.9ssd ");
        Collections.addAll(entry.expect1, new Hit<>(0, "xing", TokenType.EN), new Hit<>(4, "09", TokenType.NUM),
                new Hit<>(9, "tqmall", TokenType.EN), new Hit<>(15, "5", TokenType.NUM), new Hit<>(18, "6.9", TokenType.DECIMAL),
                new Hit<>(21, "ssd", TokenType.EN));
        entry.expect2.addAll(entry.expect1);
        entry.expect3.addAll(entry.expect1);
        Collections.addAll(entry.expectMax, new Hit<>(0, "xing09", TokenType.EN_MIX), new Hit<>(9, "tqmall5", TokenType.EN_MIX),
                new Hit<>(18, "6.9ssd", TokenType.EN_MIX));
        textEntryList.add(entry);

        entry = new TestTextEntry(".89.6xing tqmall5..6.9ssd78.0.9.78");
        Collections.addAll(entry.expect1, new Hit<>(1, "89.6", TokenType.DECIMAL), new Hit<>(5, "xing", TokenType.EN),
                new Hit<>(10, "tqmall", TokenType.EN), new Hit<>(16, "5", TokenType.NUM), new Hit<>(19, "6.9", TokenType.DECIMAL),
                new Hit<>(22, "ssd", TokenType.EN), new Hit<>(25, "78.0", TokenType.EN), new Hit<>(30, "9.78", TokenType.DECIMAL));
        entry.expect2.addAll(entry.expect1);
        entry.expect3.addAll(entry.expect1);
        Collections.addAll(entry.expectMax, new Hit<>(1, "89.6xing", TokenType.EN_MIX), new Hit<>(10, "tqmall5", TokenType.EN_MIX),
                new Hit<>(19, "6.9ssd78.0.9.78", TokenType.EN_MIX));
        textEntryList.add(entry);

        entry = new TestTextEntry(".89.6..-xing-wang");
        Collections.addAll(entry.expect1, new Hit<>(1, "89.6", TokenType.DECIMAL), new Hit<>(8, "xing", TokenType.EN), new Hit<>(13, "wang", TokenType.EN));
        Collections.addAll(entry.expect2, new Hit<>(1, "89.6", TokenType.DECIMAL), new Hit<>(8, "xing-wang", TokenType.EN_MIX));
        Collections.addAll(entry.expect3, new Hit<>(1, "89.6", TokenType.DECIMAL), new Hit<>(8, "xing", TokenType.EN),
                new Hit<>(8, "xing-wang", TokenType.EN), new Hit<>(13, "wang", TokenType.EN));
        Collections.addAll(entry.expectMax, new Hit<>(1, "89.6", TokenType.EN_MIX), new Hit<>(8, "xing", TokenType.EN_MIX),
                new Hit<>(13, "wang", TokenType.EN_MIX));
        textEntryList.add(entry);

        entry = new TestTextEntry(".89.6..-xing--wang");
        Collections.addAll(entry.expect1, new Hit<>(1, "89.6", TokenType.DECIMAL), new Hit<>(8, "xing", TokenType.EN), new Hit<>(14, "wang", TokenType.EN));
        entry.expect2.addAll(entry.expect1);
        entry.expect3.addAll(entry.expect1);
        Collections.addAll(entry.expectMax, new Hit<>(1, "89.6", TokenType.EN_MIX), new Hit<>(8, "xing", TokenType.EN_MIX), new Hit<>(14, "wang", TokenType.EN_MIX));
        textEntryList.add(entry);

        entry = new TestTextEntry(".89.6..-xing-05-yan");
        Collections.addAll(entry.expect1, new Hit<>(1, "89.6", TokenType.DECIMAL), new Hit<>(8, "xing", TokenType.EN),
                new Hit<>(13, "05", TokenType.NUM), new Hit<>(16, "yan", TokenType.EN));
        Collections.addAll(entry.expect2, new Hit<>(1, "89.6", TokenType.DECIMAL), new Hit<>(8, "xing-05", TokenType.EN_MIX), new Hit<>(16, "yan", TokenType.EN));
        Collections.addAll(entry.expect3, new Hit<>(1, "89.6", TokenType.DECIMAL), new Hit<>(8, "xing", TokenType.EN),
                new Hit<>(8, "xing-05", TokenType.EN_MIX), new Hit<>(13, "05", TokenType.EN), new Hit<>(16, "yan", TokenType.EN));
        Collections.addAll(entry.expectMax, new Hit<>(1, "89.6", TokenType.EN_MIX), new Hit<>(8, "xing", TokenType.EN_MIX),
                new Hit<>(13, "05", TokenType.EN_MIX), new Hit<>(16, "yan", TokenType.EN_MIX));
        textEntryList.add(entry);
    }

    @AfterClass
    public static void clear() {
        textEntryList.clear();
    }

    @Test
    public void min_1_SegmentTest() {
        TextMatch<TokenType> segment = AsciiSegment.build().create();
        for (TestTextEntry e : textEntryList) {
            Assert.assertEquals(e.text, e.expect1, segment.match(e.textArray));
        }
    }

    @Test
    public void min_2_SegmentTest() {
        TextMatch<TokenType> segment = AsciiSegment.build()
                .enMixAppend(false)
                .create();
        for (TestTextEntry e : textEntryList) {
            Assert.assertEquals(e.text, e.expect2, segment.match(e.textArray));
        }
    }

    @Test
    public void min_3_SegmentTest() {
        TextMatch<TokenType> segment = AsciiSegment.build()
                .enMixAppend(true)
                .create();
        for (TestTextEntry e : textEntryList) {
            Assert.assertEquals(e.text, e.expect3, segment.match(e.textArray));
        }
    }

    @Test
    public void maxSegmentTest() {
        for (TestTextEntry e : textEntryList) {
            Assert.assertEquals(e.text, e.expectMax, MaxAsciiSegment.INSTANCE.match(e.textArray));
        }
    }

    static class TestTextEntry {

        private final String text;

        private final char[] textArray;

        private final List<Hit<TokenType>> expect1 = new ArrayList<>();

        private final List<Hit<TokenType>> expect2 = new ArrayList<>();

        private final List<Hit<TokenType>> expect3 = new ArrayList<>();

        private final List<Hit<TokenType>> expectMax = new ArrayList<>();

        TestTextEntry(String text) {
            this.text = text;
            this.textArray = text.toCharArray();
        }

    }

}
