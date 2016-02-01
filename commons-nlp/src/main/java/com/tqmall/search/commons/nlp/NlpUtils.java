package com.tqmall.search.commons.nlp;

/**
 * Created by xing on 16/1/26.
 * Nlp 操作Utils类
 */
public final class NlpUtils {

    private NlpUtils() {
    }

    /**
     * 是否位繁体字符
     * 该函数跟{@link #cjkConvert(char)}的性能一样一样的, 并没有
     *
     * @param ch 判断字符
     * @return true 为繁体
     */
    public static boolean isTraditional(char ch) {
        return TraditionToSimple.getInstance().isTraditional(ch);
    }

    /**
     * 如果是CJK字符, 返回对应的转换字符, 如果不是则返回入参ch
     */
    public static char cjkConvert(char ch) {
        return TraditionToSimple.getInstance().convert(ch);
    }

    /**
     * 如果传入的字符串有繁体, 则转换成简体字符串
     * 如果没有繁体, 则不做装换, 原样返回
     * 很对情况下, 砸门的字符串里面没有繁体字符的
     */
    public static String traditionalConvert(String str) {
        return TraditionToSimple.getInstance().convert(str);
    }

    /**
     * 判断是否为cjk字符
     *
     * @return true为cjk字符
     */
    public static boolean isCjkChar(char ch) {
        return ch >= NlpConst.CJK_UNIFIED_IDEOGRAPHS_FIRST && ch <= NlpConst.CJK_UNIFIED_IDEOGRAPHS_LAST;
    }

}
