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

    /**
     * 拼音, 分词等转化时添加空白字符flag, 具体通过{@link Character#isWhitespace(char)}判断
     */
    int APPEND_CHAR_WHITESPACE = 1;
    /**
     * 拼音, 分词等转化时添加ASCII英文字母flag, 英文字母判断'a' - 'z' / 'A' - 'Z'
     */
    int APPEND_CHAR_LETTER = 1 << 1;
    /**
     * 拼音, 分词等转化时添加ASCII数字flag, 数字判断'0' - '9'
     */
    int APPEND_CHAR_DIGIT = 1 << 2;
    /**
     * 拼音, 分词等转化时添加其他字符, 非空白, ASCII英文字母, 数字字符为其他字符
     */
    int APPEND_CHAR_OTHER = 1 << 3;

    /**
     * 无法识别的词
     */
    int TOKEN_TYPE_UNKNOWN = 0;
    /**
     * 阿拉伯数字
     */
    int TOKEN_TYPE_NUM = TOKEN_TYPE_UNKNOWN + 1;
    /**
     * 英文单词
     */
    int TOKEN_TYPE_EN = TOKEN_TYPE_NUM + 1;
    /**
     * 通过'-'连接的英文混合词, '-'左边是英文字符才会识别为英文混合词
     */
    int TOKEN_TYPE_EN_MIX = TOKEN_TYPE_EN + 1;
    /**
     * 中文
     */
    int TOKEN_TYPE_CN = TOKEN_TYPE_EN_MIX + 1;
    /**
     * 数量词
     */
    int TOCKE_TYPE_NUM_QUANTIFIER = TOKEN_TYPE_CN + 1;
    /**
     * 词类型
     */
    String[] TOKEN_TYPES = {"<UNKNOWN>", "<NUM>", "<EN>", "<EN_MIX>", "<CN>", "<NUM_QUANTIFIER>"};
}
