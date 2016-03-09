package com.tqmall.search.commons.nlp;

import com.sun.org.apache.xalan.internal.xsltc.dom.BitArray;
import com.tqmall.search.commons.nlp.trie.TextMatch;

import java.util.List;
import java.util.Objects;

/**
 * Created by xing on 16/3/8.
 * Cjk分词
 *
 * @author xing
 */
public abstract class CjkSegment implements TextMatch<Integer> {

    protected final CjkLexicon cjkLexicon;

    protected CjkSegment(CjkLexicon cjkLexicon) {
        this.cjkLexicon = cjkLexicon;
    }

    protected abstract List<Hit<Integer>> doMatch(char[] text, int startPos, int length);

    @Override
    public final List<Hit<Integer>> match(char[] text) {
        Objects.requireNonNull(text);
        return match(text, 0, text.length);
    }

    public final List<Hit<Integer>> match(char[] text, int startPos, int length) {
        List<Hit<Integer>> hits = doMatch(text, startPos, length);
        if (hits == null) return null;
        BitArray bitArray = new BitArray(length);
        for (Hit<Integer> h : hits) {
            int endPos = h.getEndPos();
            for (int i = h.getStartPos(); i < endPos; i++) {
                bitArray.setBit(i - startPos);
            }
        }
        //没有匹配的中文字符, 只能单独成词了
        for (int i = startPos + length - 1; i >= startPos; i--) {
            if (!bitArray.getBit(i) && NlpUtils.isCjkChar(text[i])) {
                hits.add(new Hit<>(i, String.valueOf(text[i]), NlpConst.TOKEN_TYPE_CN));
            }
        }
        return hits;
    }

    /**
     * 获取full分词, 尽可能多的返回分词结果, 适用于索引分词
     */
    public static CjkSegment full(CjkLexicon cjkLexicon) {
        return new Full(cjkLexicon);
    }

    /**
     * 获取最小分词
     */
    public static CjkSegment min(CjkLexicon cjkLexicon) {
        return new Min(cjkLexicon);
    }

    /**
     * 获取最大分词
     */
    public static CjkSegment max(CjkLexicon cjkLexicon) {
        return new Max(cjkLexicon);
    }

    /**
     * full匹配分词, 尽可能多的返回分词结果, 适用于索引分词
     */
    public static class Full extends CjkSegment {

        public Full(CjkLexicon cjkLexicon) {
            super(cjkLexicon);
        }

        @Override
        protected List<Hit<Integer>> doMatch(char[] text, int startPos, int length) {
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
        protected List<Hit<Integer>> doMatch(char[] text, int startPos, int length) {
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
        protected List<Hit<Integer>> doMatch(char[] text, int startPos, int length) {
            return cjkLexicon.maxMatch(text, startPos, length);
        }
    }


}
