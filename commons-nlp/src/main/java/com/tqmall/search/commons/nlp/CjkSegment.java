package com.tqmall.search.commons.nlp;

import com.sun.org.apache.xalan.internal.xsltc.dom.BitArray;
import com.tqmall.search.commons.nlp.trie.TextMatch;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * Created by xing on 16/3/8.
 * Cjk分词
 *
 * @author xing
 */
public abstract class CjkSegment implements TextMatch<TokenType> {

    protected final CjkLexicon cjkLexicon;

    protected CjkSegment(CjkLexicon cjkLexicon) {
        this.cjkLexicon = cjkLexicon;
    }

    protected abstract List<Hit<TokenType>> doMatch(char[] text, int startPos, int length);

    @Override
    public final List<Hit<TokenType>> match(char[] text) {
        Objects.requireNonNull(text);
        return match(text, 0, text.length);
    }

    public final List<Hit<TokenType>> match(char[] text, int startPos, int length) {
        List<Hit<TokenType>> hits = doMatch(text, startPos, length);
        if (hits == null) return null;
        BitArray bitArray = new BitArray(length);
        for (Hit<TokenType> h : hits) {
            int endPos = h.getEndPos();
            for (int i = h.getStartPos(); i < endPos; i++) {
                bitArray.setBit(i - startPos);
            }
        }
        //没有匹配的中文字符, 只能单独成词了
        for (int i = startPos + length - 1; i >= startPos; i--) {
            if (!bitArray.getBit(i) && NlpUtils.isCjkChar(text[i])) {
                hits.add(new Hit<>(i, String.valueOf(text[i]), TokenType.CN));
            }
        }
        //返回结果需要根据下标排序
        Collections.sort(hits);
        return hits;
    }

    /**
     * 获取分词器
     */
    public static CjkSegment createSegment(CjkLexicon cjkLexicon, SegmentType type) {
        switch (type) {
            case MIN:
                return new Min(cjkLexicon);
            case MAX:
                return new Max(cjkLexicon);
            case FULL:
                return new Full(cjkLexicon);
            default:
                throw new IllegalArgumentException("SegmentType: " + type + " value is invalid");
        }
    }

    /**
     * full匹配分词, 尽可能多的返回分词结果, 适用于索引分词
     */
    public static class Full extends CjkSegment {

        public Full(CjkLexicon cjkLexicon) {
            super(cjkLexicon);
        }

        @Override
        protected List<Hit<TokenType>> doMatch(char[] text, int startPos, int length) {
            return cjkLexicon.fullMatch(text, startPos, length);
        }
    }


    /**
     * 最小匹配分词
     */
    public static class Min extends CjkSegment {

        public Min(CjkLexicon cjkLexicon) {
            super(cjkLexicon);
        }

        @Override
        protected List<Hit<TokenType>> doMatch(char[] text, int startPos, int length) {
            return cjkLexicon.minMatch(text, startPos, length);
        }
    }

    /**
     * 最大匹配分词
     */
    public static class Max extends CjkSegment {

        public Max(CjkLexicon cjkLexicon) {
            super(cjkLexicon);
        }

        @Override
        protected List<Hit<TokenType>> doMatch(char[] text, int startPos, int length) {
            return cjkLexicon.maxMatch(text, startPos, length);
        }
    }

}
