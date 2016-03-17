package com.tqmall.search.commons.nlp;


import com.tqmall.search.commons.match.Hit;
import com.tqmall.search.commons.match.TextMatch;
import com.tqmall.search.commons.utils.CommonsUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;
import java.util.Objects;

/**
 * Created by xing on 16/3/8.
 * ascii最小分词, 数字和字母分开, 支持小数数字识别和英文合成词识别, 默认识别小数数字
 * 通过构造函数{@link #AsciiMinSegment(boolean, boolean, boolean)}构造容易出错, 3个参数顺序搞错就跪了,
 * 通过{@link Builder}构造即安全又优雅, 何乐而不为了!!!
 *
 * @author xing
 * @see TokenType#DECIMAL
 * @see TokenType#EN_MIX
 * @see #build()
 * @see Builder
 */
public class AsciiMinSegment implements TextMatch<TokenType> {

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
    AsciiMinSegment(boolean parseDecimal, boolean parseEnMix, boolean enMixAppend) {
        this.parseDecimal = parseDecimal;
        this.parseEnMix = parseEnMix;
        if (!parseEnMix && enMixAppend) {
            throw new IllegalArgumentException("parseEnMix is false, enMixAppend is useless, which value should be false");
        }
        this.enMixAppend = enMixAppend;
    }

    @Override
    public List<Hit<TokenType>> match(char[] text) {
        Objects.requireNonNull(text);
        return match(text, 0, text.length);
    }

    /**
     * 识别小数数字, 获取当前字符类型, 需要考虑上个字符
     */
    private TokenType tokenTypeOfDecimal(char c, TokenType preType) {
        if (c >= 'a' && c <= 'z') return TokenType.EN;
        else if (c >= '0' && c <= '9') {
            return preType == TokenType.DECIMAL ? TokenType.DECIMAL : TokenType.NUM;
        } else if (c == '.' && preType == TokenType.NUM) return TokenType.DECIMAL;
        else return TokenType.UNKNOWN;
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
     * @param text               字符串
     * @param startPos           需要匹配的开始pos
     * @param endPos             需要匹配的终止pos
     * @param mixSymbolPositions 记录通过'-'连接的单词
     * @return 匹配结果
     */
    private List<Hit<TokenType>> innerMatch(final char[] text, final int startPos,
                                            final int endPos, final List<Integer> mixSymbolPositions) {
        TokenType preType = TokenType.UNKNOWN, curType;
        int start = -1;
        List<Hit<TokenType>> hits = new ArrayList<>();
        for (int i = startPos; i < endPos; i++) {
            curType = parseDecimal ? tokenTypeOfDecimal(text[i], preType) : tokenType(text[i]);
            if (curType != TokenType.DECIMAL && curType != preType) {
                if (start != -1) {
                    int count = i - start;
                    //前一个字符是'.', 那就不是小数了
                    if (preType == TokenType.DECIMAL && text[i - 1] == '.') {
                        preType = TokenType.NUM;
                        count--;
                    }
                    hits.add(new Hit<>(start, new String(text, start, count), preType));
                    start = -1;
                }
            }
            if (start == -1 && curType != TokenType.UNKNOWN) start = i;
            if (mixSymbolPositions != null && preType == TokenType.EN && text[i] == '-') mixSymbolPositions.add(i);
            preType = curType;
        }
        if (start != -1) {
            int count = endPos - start;
            //这儿preType和curType基本上是相同的
            //最后一个字符是'.', 那最后一个词就不是小数了
            if (preType == TokenType.DECIMAL && text[endPos - 1] == '.') {
                preType = TokenType.NUM;
                count--;
            }
            hits.add(new Hit<>(start, new String(text, start, count), preType));
        }
        return hits;
    }

    /**
     * 英文连接词处理
     */
    private void hitsMixHandle(List<Hit<TokenType>> hits, List<Integer> mixSymbolPositions) {
        ListIterator<Hit<TokenType>> it = hits.listIterator();
        for (int pos : mixSymbolPositions) {
            Hit<TokenType> suffixHit = null;
            while (it.hasNext()) {
                Hit<TokenType> hit = it.next();
                if (hit.getStartPos() > pos) {
                    suffixHit = hit;
                    break;
                }
            }
            //没有找到需要连接的词, 说明到低了
            if (suffixHit == null) break;
            if (suffixHit.getStartPos() != pos + 1) continue;
            //往前移动, 到达suffixHit位置
            it.previous();
            //往前移动, 到达prefixHit位置
            Hit<TokenType> prefixHit = it.previous();
            //如果前一个词是合成词, 不干~~~
            if (prefixHit.getValue() == TokenType.EN_MIX) continue;
            Hit<TokenType> mixHit = new Hit<>(prefixHit.getStartPos(), prefixHit.getKey() + '-' + suffixHit.getKey(),
                    TokenType.EN_MIX);
            if (enMixAppend) {
                //移动带后缀hit, 在其后面添加mixHit
                it.next();
                it.next();
                it.add(mixHit);
            } else {
                it.remove();
                //移动到下一个
                it.next();
                //将前缀hit替换掉
                it.set(mixHit);
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
        List<Integer> mixSymbolPositions = parseEnMix ? new ArrayList<Integer>() : null;
        List<Hit<TokenType>> hits = innerMatch(text, startPos, endPos, mixSymbolPositions);
        if (!hits.isEmpty() && !CommonsUtils.isEmpty(mixSymbolPositions)) {
            hitsMixHandle(hits, mixSymbolPositions);
        }
        return hits;
    }

    @Override
    public String toString() {
        return "AsciiSegment{" + "parseDecimal=" + parseDecimal + ", enMixAppend=" + enMixAppend
                + ", parseEnMix=" + parseEnMix + '}';
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

        public AsciiMinSegment create() {
            return new AsciiMinSegment(parseDecimal, parseEnMix, enMixAppend);
        }
    }
}
