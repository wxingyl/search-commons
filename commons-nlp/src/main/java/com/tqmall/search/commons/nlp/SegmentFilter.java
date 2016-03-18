package com.tqmall.search.commons.nlp;

import com.tqmall.search.commons.match.Hit;
import com.tqmall.search.commons.analyzer.TokenType;

import java.util.List;

/**
 * Created by xing on 16/3/17.
 * 分词过滤器定义, 主要为待分词文本提前过滤和分词结果过滤[也就是停止词过滤了]
 *
 * @author xing
 */
public interface SegmentFilter {

    /**
     * 待分词文本过滤
     *
     * @param text     字符数组, 过滤的时候直接修改字符数组
     * @param startPos 开始position
     * @param length   需要处理的文本长度
     */
    void textFilter(final char[] text, final int startPos, final int length);

    /**
     * 分词结果过滤, 比如停止词等
     *
     * @param hits 分词结果, 按匹配的源text位置position排序, 该List必须支持修改
     */
    void hitsFilter(List<Hit<TokenType>> hits);
}
