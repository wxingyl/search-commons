package com.tqmall.search.commons.nlp;

import com.sun.org.apache.xalan.internal.xsltc.dom.BitArray;
import com.tqmall.search.commons.match.Hit;
import com.tqmall.search.commons.match.TextMatch;

import java.util.*;

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

    @Override
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
        int numEndIndex = -1;
        for (int i = startPos + length - 1; i >= startPos; i--) {
            if (bitArray.getBit(i) || !NlpUtils.isCjkChar(text[i])) continue;
            char c = text[i];
            if (CjkLexicon.CN_NUM.contains(c)) {
                if (numEndIndex == -1) numEndIndex = i;
                continue;
            } else if (numEndIndex != -1) {
                //将前面的数字取出来
                int start = i + 1;
                //提取数词词组
                hits.add(new Hit<>(start, new String(text, start, numEndIndex - start + 1), TokenType.NUM));
                numEndIndex = -1;
            }
            //没有匹配的中文字符, 只能单独成词了
            String w = String.valueOf(text[i]);
            hits.add(new Hit<>(i, w, cjkLexicon.isQuantifier(w) ? TokenType.QUANTIFIER : TokenType.CN));
        }
        if (numEndIndex != -1) {
            hits.add(new Hit<>(startPos, new String(text, startPos, numEndIndex - startPos + 1), TokenType.NUM));
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
