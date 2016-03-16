package com.tqmall.search.commons.nlp;

import com.tqmall.search.commons.match.Hit;
import com.tqmall.search.commons.match.TextMatch;

import java.util.List;
import java.util.ListIterator;
import java.util.Objects;

/**
 * Created by xing on 16/3/13.
 * 分词器, 其也是一个{@link TextMatch}
 *
 * @author xing
 */
public class Segment implements TextMatch<TokenType> {

    private final TextMatch<TokenType> asciiSegment;

    private final CjkSegment cjkSegment;

    /**
     * 如果不需要数量词merge, 则为null
     */
    private final NumQuantifierMerge numQuantifierMerge;

    /**
     * 不需要数量词merge
     *
     * @param asciiSegment 英文, 数字分词器
     * @param cjkSegment   中文分词器
     */
    public Segment(TextMatch<TokenType> asciiSegment, CjkSegment cjkSegment) {
        this(asciiSegment, cjkSegment, null);
    }

    /**
     * @param asciiSegment       英文, 数字分词器
     * @param cjkSegment         中文分词器
     * @param numQuantifierMerge 如果不需要数量词merge, 则为null
     */
    public Segment(TextMatch<TokenType> asciiSegment, CjkSegment cjkSegment, NumQuantifierMerge numQuantifierMerge) {
        this.asciiSegment = asciiSegment;
        this.cjkSegment = cjkSegment;
        this.numQuantifierMerge = numQuantifierMerge;
    }

    @Override
    public List<Hit<TokenType>> match(char[] text) {
        Objects.requireNonNull(text);
        return match(text, 0, text.length);
    }

    @Override
    public List<Hit<TokenType>> match(char[] text, int startPos, int length) {
        List<Hit<TokenType>> asciiHits = asciiSegment.match(text, startPos, length);
        List<Hit<TokenType>> cjkHits = cjkSegment.match(text, startPos, length);
        List<Hit<TokenType>> hits;
        if (asciiHits == null && cjkHits == null) return null;
        else if (cjkHits == null) {
            hits = asciiHits;
        } else {
            hits = cjkHits;
            //合并ascii分词结果, 两个list都是有序的, 所以只直接顺序合并
            if (asciiHits != null) {
                ListIterator<Hit<TokenType>> hitsIt = hits.listIterator();
                for (Hit<TokenType> h : asciiHits) {
                    while (hitsIt.hasNext()) {
                        int cmp = h.compareTo(hitsIt.next());
                        if (cmp <= 0) {
                            if (cmp < 0) hitsIt.previous();
                            break;
                        }
                    }
                    hitsIt.add(h);
                }
            }
        }
        if (numQuantifierMerge != null) {
            hits = numQuantifierMerge.merge(hits);
        }
        return hits;
    }

    public static Builder build() {
        return new Builder();
    }

    public static class Builder {

        private boolean parseDecimal = true;

        private boolean parseEnMix;

        private boolean enMixAppend;
        /**
         * 分文分词的分词类型
         */
        private SegmentType cjkSegmentType;

        private NumQuantifierMerge numQuantifierMerge;

        private TextMatch<TokenType> asciiMaxSegment;

        public Builder asciiMaxSegment(TextMatch<TokenType> asciiMaxSegment) {
            this.asciiMaxSegment = asciiMaxSegment;
            return this;
        }

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

        public Builder cjkSegmentType(SegmentType cjkSegmentType) {
            this.cjkSegmentType = cjkSegmentType;
            return this;
        }

        /**
         * 数量词合并
         *
         * @param appendNumQuantifier 合成的数量词是否作为扩充的词添加, 也就是原先的数词和两次在匹配结果中是否保留
         */
        public Builder appendNumQuantifier(boolean appendNumQuantifier) {
            this.numQuantifierMerge = new NumQuantifierMerge(appendNumQuantifier);
            return this;
        }

        public Segment create(CjkLexicon cjkLexicon) {
            Objects.requireNonNull(cjkLexicon);
            return new Segment(asciiMaxSegment == null ? new AsciiMinSegment(parseDecimal, parseEnMix, enMixAppend)
                    : asciiMaxSegment, CjkSegment.createSegment(cjkLexicon, cjkSegmentType), this.numQuantifierMerge);
        }
    }
}
