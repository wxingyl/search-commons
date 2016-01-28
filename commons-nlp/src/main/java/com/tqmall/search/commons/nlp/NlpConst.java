package com.tqmall.search.commons.nlp;

/**
 * Created by xing on 16/1/26.
 * nlp 相关的常量定义
 */
public interface NlpConst {
    /**
     * Unicode标准, 第一个中文字符:'一'
     */
    char CJK_UNIFIED_IDEOGRAPHS_FIRST = '\u4E00';
    /**
     * Unicode标准, 最后一个中文字符:'龥'
     */
    char CJK_UNIFIED_IDEOGRAPHS_LAST = '\u9FA5';
    /**
     * cjk字符个数
     */
    int CJK_UNIFIED_SIZE = CJK_UNIFIED_IDEOGRAPHS_LAST - CJK_UNIFIED_IDEOGRAPHS_FIRST + 1;

}
