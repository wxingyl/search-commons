package com.tqmall.search.commons.nlp;


import com.tqmall.search.commons.nlp.trie.TextMatch;
import com.tqmall.search.commons.utils.CommonsUtils;

import java.util.*;

import static com.tqmall.search.commons.nlp.NlpConst.*;

/**
 * Created by xing on 16/3/8.
 * ascii相关分词, 包括数字, 英文单词分词, 英文字母只处理小写的~~~
 *
 * @author xing
 */
public class AsciiSegment implements TextMatch<Integer> {

    /**
     * 本地新增加的小数TOKEN_TYPE, 只是在本地做处理, 不做为分词结果类型, 结果仍然是{@link NlpConst#TOKEN_TYPE_NUM}
     */
    private final static int TOKEN_TYPE_DECIMAL = TOKEN_TYPE_EN_MIX + 1;

    private final SegmentType segmentType;

    private final boolean isMaxSegment;

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
    public List<Hit<Integer>> match(char[] text) {
        Objects.requireNonNull(text);
        return match(text, 0, text.length);
    }

    /**
     * 获取当前字符类型, 并且考虑上个字符
     */
    private int tokenType(char c, int preType) {
        if (c >= 'a' && c <= 'z') return TOKEN_TYPE_EN;
        else if (c >= '0' && c <= '9') {
            return preType == TOKEN_TYPE_DECIMAL ? TOKEN_TYPE_DECIMAL : TOKEN_TYPE_NUM;
        } else if (c == '.' && preType == TOKEN_TYPE_NUM) return TOKEN_TYPE_DECIMAL;
        else return TOKEN_TYPE_UNKNOWN;
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
    private List<Hit<Integer>> minMatch(final char[] text, final int startPos,
                                        final int endPos, final List<Integer> mixSymbolPositions) {
        int preType = TOKEN_TYPE_UNKNOWN, curType;
        int start = -1;
        List<Hit<Integer>> hits = new ArrayList<>();
        for (int i = startPos; i < endPos; i++) {
            curType = tokenType(text[i], preType);
            if (curType != TOKEN_TYPE_DECIMAL && curType != preType) {
                if (start != -1) {
                    int count = i - start;
                    if (preType == TOKEN_TYPE_DECIMAL) {
                        preType = TOKEN_TYPE_NUM;
                        if (text[i - 1] == '.') count--;
                    }
                    hits.add(new Hit<>(start, new String(text, start, count), preType));
                    start = -1;
                }
            }
            if (start == -1 && curType != TOKEN_TYPE_UNKNOWN) start = i;
            if (mixSymbolPositions != null && preType == TOKEN_TYPE_EN && text[i] == '-') mixSymbolPositions.add(i);
            preType = curType;
        }
        if (start != -1) {
            int count = endPos - start;
            if (preType == TOKEN_TYPE_DECIMAL) {
                preType = TOKEN_TYPE_NUM;
                if (text[endPos - 1] == '.') count--;
            }
            hits.add(new Hit<>(start, new String(text, start, count), preType));
        }
        return hits;
    }

    /**
     * 英文连接词处理
     */
    private void hitsMixHandle(List<Hit<Integer>> hits, List<Integer> mixSymbolPositions) {
        ListIterator<Hit<Integer>> it = hits.listIterator();
        for (int pos : mixSymbolPositions) {
            Hit<Integer> suffixHit = null;
            while (it.hasNext()) {
                Hit<Integer> hit = it.next();
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
            Hit<Integer> prefixHit = it.previous();
            //如果前一个词是合成词, 不干~~~
            if (prefixHit.getValue() == TOKEN_TYPE_EN_MIX) continue;
            Hit<Integer> mixHit = new Hit<>(prefixHit.getStartPos(), prefixHit.getKey() + '-' + suffixHit.getKey(), TOKEN_TYPE_EN_MIX);
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
    public List<Hit<Integer>> match(char[] text, int startPos, int length) {
        final int endPos = startPos + length;
        NlpUtils.arrayIndexCheck(text, startPos, endPos);
        if (length == 0) return null;
        List<Integer> mixSymbolPositions = segmentType == SegmentType.MIN ? null : new ArrayList<Integer>();
        List<Hit<Integer>> hits = minMatch(text, startPos, endPos, mixSymbolPositions);
        if (!hits.isEmpty() && !CommonsUtils.isEmpty(mixSymbolPositions)) {
            hitsMixHandle(hits, mixSymbolPositions);
        }
        return hits;
    }

}
