package com.tqmall.search.commons.nlp;

import com.tqmall.search.commons.analyzer.*;
import com.tqmall.search.commons.lang.Supplier;
import com.tqmall.search.commons.match.AbstractTextMatch;
import com.tqmall.search.commons.match.Hit;

import java.util.List;
import java.util.ListIterator;
import java.util.Objects;

/**
 * Created by xing on 16/3/13.
 * 分词器, 也是一个{@link AbstractTextMatch}
 *
 * @author xing
 */
public final class Segment extends AbstractTextMatch<TokenType> {

    private final String name;

    private final SegmentFilter segmentFilter;

    private final AbstractTextMatch<TokenType> asciiAnalyzer;

    private final CjkAnalyzer cjkAnalyzer;

    /**
     * 如果不需要数量词merge, 则为null
     */
    private final NumQuantifierMerge numQuantifierMerge;

    /**
     * @param segmentFilter      分词过滤器
     * @param asciiAnalyzer      英文, 数字分词器
     * @param cjkAnalyzer        中文分词器
     * @param numQuantifierMerge 如果不需要数量词merge, 则为null
     */
    Segment(String name, SegmentFilter segmentFilter, AbstractTextMatch<TokenType> asciiAnalyzer,
            CjkAnalyzer cjkAnalyzer, NumQuantifierMerge numQuantifierMerge) {
        this.name = name;
        this.segmentFilter = segmentFilter;
        this.asciiAnalyzer = asciiAnalyzer;
        this.cjkAnalyzer = cjkAnalyzer;
        this.numQuantifierMerge = numQuantifierMerge;
    }

    @Override
    public List<Hit<TokenType>> match(final char[] text, final int off, final int len) {
        if (segmentFilter != null) segmentFilter.textFilter(text, off, len);
        List<Hit<TokenType>> asciiHits = asciiAnalyzer.match(text, off, len);
        List<Hit<TokenType>> cjkHits = cjkAnalyzer.match(text, off, len);
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
            numQuantifierMerge.merge(hits);
        }
        if (segmentFilter != null) segmentFilter.hitsFilter(text, hits);
        return hits;
    }

    public String getName() {
        return name;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Segment)) return false;

        Segment segment = (Segment) o;

        return name.equals(segment.name);

    }

    @Override
    public int hashCode() {
        return name.hashCode();
    }

    public static Builder build(String name) {
        return new Builder(name);
    }

    public static class Builder {

        private final String name;

        private CjkAnalyzer.Type cjkAnalyzerType;

        private NumQuantifierMerge numQuantifierMerge;

        private AbstractTextMatch<TokenType> asciiAnalyzer;

        private SegmentFilter segmentFilter;

        public Builder(String name) {
            this.name = name;
        }

        public Builder segmentFilter(SegmentFilter segmentFilter) {
            this.segmentFilter = segmentFilter;
            return this;
        }

        public Builder asciiAnalyzer(AbstractTextMatch<TokenType> asciiAnalyzer) {
            this.asciiAnalyzer = asciiAnalyzer;
            return this;
        }

        public Builder cjkSegmentType(CjkAnalyzer.Type cjkAnalyzerType) {
            this.cjkAnalyzerType = cjkAnalyzerType;
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

        public Segment create(Supplier<CjkLexicon> cjkLexicon) {
            Objects.requireNonNull(cjkLexicon);
            return new Segment(name, segmentFilter, asciiAnalyzer == null ? AsciiAnalyzer.build().create()
                    : asciiAnalyzer, CjkAnalyzer.createSegment(cjkLexicon, cjkAnalyzerType), this.numQuantifierMerge);
        }
    }
}
