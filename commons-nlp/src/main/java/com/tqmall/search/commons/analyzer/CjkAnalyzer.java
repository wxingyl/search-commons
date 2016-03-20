package com.tqmall.search.commons.analyzer;

import com.sun.org.apache.xalan.internal.xsltc.dom.BitArray;
import com.tqmall.search.commons.match.AbstractTextMatch;
import com.tqmall.search.commons.match.Hit;
import com.tqmall.search.commons.nlp.NlpUtils;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * Created by xing on 16/3/8.
 * Cjk分词
 *
 * @author xing
 */
public abstract class CjkAnalyzer extends AbstractTextMatch<TokenType> {

    protected final CjkLexicon cjkLexicon;

    protected CjkAnalyzer(CjkLexicon cjkLexicon) {
        this.cjkLexicon = cjkLexicon;
    }

    protected abstract List<Hit<TokenType>> doMatch(char[] text, int off, int len);

    private Hit<TokenType> getNumHit(int off, int numEndIndex) {
        return new Hit<>(off, numEndIndex + 1, TokenType.NUM);
    }

    @Override
    public final List<Hit<TokenType>> match(char[] text, int off, int len) {
        List<Hit<TokenType>> hits = doMatch(text, off, len);
        if (hits == null) return null;
        BitArray bitArray = new BitArray(len);
        for (Hit<TokenType> h : hits) {
            int endPos = h.getEnd();
            for (int i = h.getStart(); i < endPos; i++) {
                bitArray.setBit(i - off);
            }
        }
        //数词提取, 未匹配的cjk字符单个成词
        int numEndIndex = -1;
        for (int i = off + len - 1; i >= off; i--) {
            if (bitArray.getBit(i) || !NlpUtils.isCjkChar(text[i])) {
                if (numEndIndex != -1) {
                    hits.add(getNumHit(i + 1, numEndIndex));
                    numEndIndex = -1;
                }
                continue;
            }
            char c = text[i];
            if (CjkLexicon.CN_NUM.contains(c)) {
                if (numEndIndex == -1) numEndIndex = i;
                continue;
            } else if (numEndIndex != -1) {
                //提取数词词组
                hits.add(getNumHit(i + 1, numEndIndex));
                numEndIndex = -1;
            }
            //没有匹配的中文字符, 只能单独成词了
            String w = String.valueOf(text[i]);
            hits.add(new Hit<>(i, i + 1, cjkLexicon.isQuantifier(w) ? TokenType.QUANTIFIER : TokenType.CN));
        }
        if (numEndIndex != -1) {
            hits.add(getNumHit(off, numEndIndex));
        }
        //返回结果需要根据下标排序
        Collections.sort(hits);
        return hits;
    }

    /**
     * 获取分词器
     */
    public static CjkAnalyzer createSegment(CjkLexicon cjkLexicon, Type type) {
        Objects.requireNonNull(type);
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
    public static class Full extends CjkAnalyzer {

        public Full(CjkLexicon cjkLexicon) {
            super(cjkLexicon);
        }

        @Override
        protected List<Hit<TokenType>> doMatch(char[] text, int off, int len) {
            return cjkLexicon.fullMatch(text, off, len);
        }
    }


    /**
     * 最小匹配分词
     */
    public static class Min extends CjkAnalyzer {

        public Min(CjkLexicon cjkLexicon) {
            super(cjkLexicon);
        }

        @Override
        protected List<Hit<TokenType>> doMatch(char[] text, int off, int len) {
            return cjkLexicon.minMatch(text, off, len);
        }
    }

    /**
     * 最大匹配分词
     */
    public static class Max extends CjkAnalyzer {

        public Max(CjkLexicon cjkLexicon) {
            super(cjkLexicon);
        }

        @Override
        protected List<Hit<TokenType>> doMatch(char[] text, int off, int len) {
            return cjkLexicon.maxMatch(text, off, len);
        }
    }

    /**
     * cjk分词方式定义
     */
    public enum Type {
        //小粒度分词, 根据词库最小匹配
        MIN,
        //大粒度分词, 根据词库最大匹配
        MAX,
        //尽可能多的分词, 根据词典匹配所有结果
        FULL
    }

}
