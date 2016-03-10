package com.tqmall.search.commons.nlp;

/**
 * Created by xing on 16/3/10.
 * 分词方式定义
 *
 * @author xing
 */
public enum SegmentType {
    //小粒度分词, 根据词库最小匹配
    MIN,
    //大粒度分词, 根据词库最大匹配
    MAX,
    //尽可能多的分词, 根据词典匹配所有结果
    FULL
}
