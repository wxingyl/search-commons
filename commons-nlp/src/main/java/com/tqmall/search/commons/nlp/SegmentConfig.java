package com.tqmall.search.commons.nlp;

import com.tqmall.search.commons.analyzer.*;

import java.util.Objects;

/**
 * Created by xing on 16/3/18.
 * 分词配置bean
 *
 * @author xing
 * @see Segment
 * @see AsciiAnalyzer
 * @see MaxAsciiAnalyzer
 * @see CjkAnalyzer
 * @see NumQuantifierMerge
 * @see SegmentFilters
 */
public class SegmentConfig {

    private final String name;

    /**
     * 分词过滤器, 默认为mull
     *
     * @see SegmentFilters#textFilter()
     * @see SegmentFilters#hitsFilter()
     */
    private SegmentFilter segmentFilter;

    /**
     * 是否使用{@link MaxAsciiAnalyzer}
     * 如果为true,下面的3个配置项: {@link #asciiAnalyzerParseDecimal}, {@link #asciiAnalyzerParseEnMix}
     * {@link #asciiAnalyzerAppendEnMix} 无效
     * <p/>
     * 默认false, 使用{@link AsciiAnalyzer}
     *
     * @see MaxAsciiAnalyzer
     */
    private boolean maxAsciiAnalyzer = false;

    /**
     * 默认true
     *
     * @see AsciiAnalyzer#parseDecimal
     */
    private boolean asciiAnalyzerParseDecimal = true;

    /**
     * 默认false
     *
     * @see AsciiAnalyzer#parseEnMix
     */
    private boolean asciiAnalyzerParseEnMix = false;

    /**
     * 默认false
     *
     * @see AsciiAnalyzer#enMixAppend
     */
    private boolean asciiAnalyzerAppendEnMix = false;

    /**
     * CjkAnalyzer分词类型
     * 默认最小分词{@link CjkAnalyzer.Type#MIN}
     *
     * @see CjkAnalyzer
     */
    private CjkAnalyzer.Type cjkAnalyzerType = CjkAnalyzer.Type.MIN;

    /**
     * 是否开启数量词合并
     * 默认false, 不开启
     *
     * @see NumQuantifierMerge
     */
    private boolean mergeNumQuantifier = false;

    /**
     * 如果开启数量词合并, 即{@link #mergeNumQuantifier} = true 才有效
     * 默认false, 即替换原先相连数词和量词
     *
     * @see NumQuantifierMerge#appendNumQuantifier
     */
    private boolean appendNumQuantifier = false;

    public SegmentConfig(String name) {
        this.name = name;
    }

    /**
     * 根据当前配置创建一个分词器
     *
     * @param cjkLexicon cjk词库对象
     * @return 新建的分词器{@link Segment}对象
     */
    public Segment createSegment(CjkLexiconFactory cjkLexicon) {
        Objects.requireNonNull(cjkLexicon);
        Segment.Builder builder = Segment.build(name);
        if (segmentFilter != null) {
            builder.segmentFilter(segmentFilter);
        }
        if (maxAsciiAnalyzer) {
            builder.asciiAnalyzer(MaxAsciiAnalyzer.INSTANCE);
        } else {
            AsciiAnalyzer.Builder asciiBuilder = AsciiAnalyzer.build();
            if (asciiAnalyzerParseEnMix) {
                asciiBuilder.enMixAppend(asciiAnalyzerAppendEnMix);
            }
            builder.asciiAnalyzer(asciiBuilder.parseDecimal(asciiAnalyzerParseDecimal)
                    .create());
        }
        builder.cjkSegmentType(cjkAnalyzerType);
        if (mergeNumQuantifier) {
            builder.appendNumQuantifier(appendNumQuantifier);
        }
        return builder.create(cjkLexicon);
    }

    public void setAppendNumQuantifier(boolean appendNumQuantifier) {
        this.appendNumQuantifier = appendNumQuantifier;
    }

    public void setAsciiAnalyzerAppendEnMix(boolean asciiAnalyzerAppendEnMix) {
        this.asciiAnalyzerAppendEnMix = asciiAnalyzerAppendEnMix;
    }

    public void setAsciiAnalyzerParseDecimal(boolean asciiAnalyzerParseDecimal) {
        this.asciiAnalyzerParseDecimal = asciiAnalyzerParseDecimal;
    }

    public void setAsciiAnalyzerParseEnMix(boolean asciiAnalyzerParseEnMix) {
        this.asciiAnalyzerParseEnMix = asciiAnalyzerParseEnMix;
    }

    public void setCjkAnalyzerType(CjkAnalyzer.Type cjkAnalyzerType) {
        this.cjkAnalyzerType = cjkAnalyzerType;
    }

    public void setMaxAsciiAnalyzer(boolean maxAsciiAnalyzer) {
        this.maxAsciiAnalyzer = maxAsciiAnalyzer;
    }

    public void setMergeNumQuantifier(boolean mergeNumQuantifier) {
        this.mergeNumQuantifier = mergeNumQuantifier;
    }

    public void setSegmentFilter(SegmentFilter segmentFilter) {
        this.segmentFilter = segmentFilter;
    }

    public String getName() {
        return name;
    }

    public boolean isAppendNumQuantifier() {
        return appendNumQuantifier;
    }

    public boolean isAsciiAnalyzerAppendEnMix() {
        return asciiAnalyzerAppendEnMix;
    }

    public boolean isAsciiAnalyzerParseDecimal() {
        return asciiAnalyzerParseDecimal;
    }

    public boolean isAsciiAnalyzerParseEnMix() {
        return asciiAnalyzerParseEnMix;
    }

    public CjkAnalyzer.Type getCjkAnalyzerType() {
        return cjkAnalyzerType;
    }

    public boolean isMaxAsciiAnalyzer() {
        return maxAsciiAnalyzer;
    }

    public boolean isMergeNumQuantifier() {
        return mergeNumQuantifier;
    }

    public SegmentFilter getSegmentFilter() {
        return segmentFilter;
    }
}
