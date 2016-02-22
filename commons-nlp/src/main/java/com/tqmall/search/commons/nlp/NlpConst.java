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
     * Unicode标准, 最后一个中文字符:'龥', 最新版本的Unicode向后还有一些汉字, 哪些就算了, 平常根本就不用, 并且很多系统都不支持
     * 这些新加的字符显示, 所以砸门还是用工人的9FA5吧
     */
    char CJK_UNIFIED_IDEOGRAPHS_LAST = '\u9FA5';
    /**
     * cjk字符个数
     */
    int CJK_UNIFIED_SIZE = CJK_UNIFIED_IDEOGRAPHS_LAST - CJK_UNIFIED_IDEOGRAPHS_FIRST + 1;

    String F2J_FILE_NAME = "tradition-simple.txt";

    String PINYIN_FILE_NAME = "pinyin.txt";

    String STOPWORD_FILE_NAME = "stopword.txt";

}
