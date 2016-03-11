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
 * ascii相关分词, 包括数字, 英文单词分词, 英文字母只处理小写的~~~
 *
 * @author xing
 */
public class AsciiSegment implements TextMatch<TokenType> {

    private final SegmentType segmentType;

    private final boolean isMaxSegment;

    /**
     * 默认最小分词{@link SegmentType#MIN}
     */
    public AsciiSegment() {
        this(SegmentType.MIN);
    }

    public AsciiSegment(SegmentType segmentType) {
        this.segmentType = segmentType;
        isMaxSegment = segmentType == SegmentType.MAX;
    }

    public SegmentType getSegmentType() {
        return segmentType;
    }

    @Override
    public List<Hit<TokenType>> match(char[] text) {
        Objects.requireNonNull(text);
        return match(text, 0, text.length);
    }

    /**
     * 获取当前字符类型, 并且考虑上个字符
     */
    private TokenType tokenType(char c, TokenType preType) {
        if (c >= 'a' && c <= 'z') return TokenType.EN;
        else if (c >= '0' && c <= '9') {
            return preType == TokenType.DECIMAL ? TokenType.DECIMAL : TokenType.NUM;
        } else if (c == '.' && preType == TokenType.NUM) return TokenType.DECIMAL;
        else return TokenType.UNKNOWN;
    }

    /**
     * 对英文, 数字做最小分词
     *
     * @param text               字符串
     * @param startPos           需要匹配的开始pos
     * @param endPos             需要匹配的终止pos
     * @param mixSymbolPositions 记录通过'-'连接的单词
     * @return 匹配结果
     */
    private List<Hit<TokenType>> minMatch(final char[] text, final int startPos,
                                          final int endPos, final List<Integer> mixSymbolPositions) {
        TokenType preType = TokenType.UNKNOWN, curType;
        int start = -1;
        List<Hit<TokenType>> hits = new ArrayList<>();
        for (int i = startPos; i < endPos; i++) {
            curType = tokenType(text[i], preType);
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
            if (isMaxSegment) {
                it.remove();
                //移动到下一个
                it.next();
                //将前缀hit替换掉
                it.set(mixHit);
            } else {
                //移动带后缀hit, 在其后面添加mixHit
                it.next();
                it.next();
                it.add(mixHit);
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
        List<Integer> mixSymbolPositions = segmentType == SegmentType.MIN ? null : new ArrayList<Integer>();
        List<Hit<TokenType>> hits = minMatch(text, startPos, endPos, mixSymbolPositions);
        if (!hits.isEmpty() && !CommonsUtils.isEmpty(mixSymbolPositions)) {
            hitsMixHandle(hits, mixSymbolPositions);
        }
        return hits;
    }

}
