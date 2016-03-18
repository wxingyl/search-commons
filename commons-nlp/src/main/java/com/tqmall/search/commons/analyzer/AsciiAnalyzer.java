package com.tqmall.search.commons.analyzer;


import com.tqmall.search.commons.match.AbstractTextMatch;
import com.tqmall.search.commons.match.Hit;
import com.tqmall.search.commons.nlp.NlpUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;

/**
 * Created by xing on 16/3/8.
 * ascii最小分词, 数字和字母分开, 支持小数数字识别和英文合成词识别, 默认识别小数数字
 * 通过构造函数{@link #AsciiAnalyzer(boolean, boolean, boolean)}构造容易出错, 3个参数顺序搞错就跪了,
 * 通过{@link Builder}构造即安全又优雅, 何乐而不为了!!!
 *
 * @author xing
 * @see TokenType#DECIMAL
 * @see TokenType#EN_MIX
 * @see #build()
 * @see Builder
 */
public class AsciiAnalyzer extends AbstractTextMatch<TokenType> {

    /**
     * 是否识别小数
     *
     * @see TokenType#DECIMAL
     */
    private final boolean parseDecimal;
    /**
     * 是否识别通过'-'连接的英文单词
     *
     * @see TokenType#EN_MIX
     */
    private final boolean parseEnMix;

    /**
     * 上面的变量{@link #parseEnMix}为true才有意义, 对于识别到的EnMix词是作为新词添加还是替换原先的词
     */
    private final boolean enMixAppend;

    /**
     * 如果parseEnMix = false, 但是enMixAppend = true抛出{@link IllegalArgumentException}
     * 通过该构造函数直接初始化容易出错, 3个参数顺序搞错就跪了,
     * 通过{@link Builder}构造即安全又优雅, 何乐而不为了!!!
     *
     * @param parseDecimal 是否识别小数数字
     * @param parseEnMix   是否识别英文合成词
     * @param enMixAppend  英文合成词是否作为新词添加, parseEnMix 为true该值才有意义
     */
    AsciiAnalyzer(boolean parseDecimal, boolean parseEnMix, boolean enMixAppend) {
        this.parseDecimal = parseDecimal;
        this.parseEnMix = parseEnMix;
        if (!parseEnMix && enMixAppend) {
            throw new IllegalArgumentException("parseEnMix is false, enMixAppend is useless, which value should be false");
        }
        this.enMixAppend = enMixAppend;
    }

    /**
     * 不识别小数数字, 获取当前字符类型
     */
    private TokenType tokenType(char c) {
        if (c >= 'a' && c <= 'z') return TokenType.EN;
        else if (c >= '0' && c <= '9') {
            return TokenType.NUM;
        } else return TokenType.UNKNOWN;
    }

    /**
     * 数字和字母分离, 数字识别不考虑小数
     *
     * @param text     字符串
     * @param startPos 需要匹配的开始pos
     * @param endPos   需要匹配的终止pos
     * @return 匹配结果
     */
    private List<Hit<TokenType>> enNumSplit(final char[] text, final int startPos, final int endPos) {
        TokenType preCharType = TokenType.UNKNOWN, curType;
        int start = -1;
        List<Hit<TokenType>> hits = new ArrayList<>();
        for (int i = startPos; i < endPos; i++) {
            curType = tokenType(text[i]);
            if (curType == TokenType.UNKNOWN) {
                if (start != -1) {
                    hits.add(new Hit<>(text, start, i, preCharType));
                    start = -1;
                    preCharType = TokenType.UNKNOWN;
                }
            } else {
                if (curType != preCharType) {
                    if (start != -1) hits.add(new Hit<>(text, start, i, preCharType));
                    start = i;
                    preCharType = curType;
                } else if (start == -1) start = i;
            }
        }
        if (start != -1) {
            hits.add(new Hit<>(text, start, endPos, preCharType));
        }
        return hits;
    }

    private void merge(final char[] text, List<Hit<TokenType>> hits, TokenType matchType,
                       TokenType newType, final char matchChar, final boolean append) {
        ListIterator<Hit<TokenType>> it = hits.listIterator();
        while (it.hasNext()) {
            Hit<TokenType> hit = it.next();
            if (hit.getValue() != matchType || !it.hasNext()
                    || text[hit.getEndPos()] != matchChar) continue;
            Hit<TokenType> nextHit = it.next();
            if (matchType == TokenType.NUM && nextHit.getValue() != matchType) continue;
            if (hit.getEndPos() + 1 != nextHit.getStartPos()) {
                it.previous();
                continue;
            }
            if (append) {
                it.previous();
                it.add(new Hit<>(hit.getStartPos(), hit.getKey() + matchChar + nextHit.getKey(), newType));
                it.next();
            } else {
                hit.changeKey(hit.getStartPos(), hit.getKey() + matchChar + nextHit.getKey());
                hit.changeValue(newType);
                it.remove();
            }
        }
    }

    /**
     * 匹配数字, 英文单词, 或者通过'-'的英文连接词
     *
     * @return Hit中的value对应词的类型
     */
    @Override
    public List<Hit<TokenType>> match(char[] text, int startPos, int length) {
        final int endPos = startPos + length;
        NlpUtils.arrayIndexCheck(text, startPos, endPos);
        if (length == 0) return null;
        List<Hit<TokenType>> hits = enNumSplit(text, startPos, endPos);
        if (hits.isEmpty()) return hits;
        if (parseDecimal) {
            merge(text, hits, TokenType.NUM, TokenType.DECIMAL, '.', false);
        }
        if (parseEnMix) {
            merge(text, hits, TokenType.EN, TokenType.EN_MIX, '-', enMixAppend);
        }
        return hits;
    }

    @Override
    public String toString() {
        return "AsciiSegment{" + "parseDecimal=" + parseDecimal + ", parseEnMix=" + parseEnMix
                + ", enMixAppend=" + enMixAppend + '}';
    }

    /**
     * 数字和字母分开, 支持小数数字识别和英文合成词识别, 默认识别小数数字
     */
    public static Builder build() {
        return new Builder();
    }

    public static class Builder {

        private boolean parseDecimal = true;

        private boolean parseEnMix;

        private boolean enMixAppend;

        /**
         * 默认true
         */
        public Builder parseDecimal(boolean parseDecimal) {
            this.parseDecimal = parseDecimal;
            return this;
        }

        /**
         * 是否扩展EnMix词
         */
        public Builder enMixAppend(boolean enMixAppend) {
            this.parseEnMix = true;
            this.enMixAppend = enMixAppend;
            return this;
        }

        public AsciiAnalyzer create() {
            return new AsciiAnalyzer(parseDecimal, parseEnMix, enMixAppend);
        }
    }
}
