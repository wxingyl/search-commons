package com.tqmall.search.commons.utils;

import com.tqmall.search.commons.lang.StrValueConvert;

/**
 * Created by xing on 15/12/8.
 * 一些{@link Number} 相关的公共方法
 * 其实很多方法在org.apache.commons.lang3.math.NumberUtils中有定义, 该类中的方法只是对人家的一点点补充
 * 另外根据{@link String}转确定类型的{@link Number}, 可以参考{@link StrValueConvert} 以及 {@link StrValueConverts}
 * 建议优先使用org.apache.commons.lang3.math.NumberUtils中的方法, 如果没有再用这的
 *
 * @see StrValueConvert
 * @see StrValueConverts
 */
public final class SearchNumberUtils {

    private SearchNumberUtils() {
    }

    /**
     * 判断一个Number对象是否 == 0
     * @param num Number对象
     * @return true 等于0或者为null
     */
    public static <T extends Number> boolean isEqZero(T num) {
        return num == null || num.intValue() == 0;
    }

    /**
     * 判断一个Number对象是否 > 0
     * @param num Number对象
     * @return true 大于0
     */
    public static <T extends Number> boolean isGtZero(T num) {
        return num != null && num.intValue() > 0;
    }

    /**
     * 判断一个Number对象是否 >= 0
     * @param num Number对象
     * @return true 大于0
     */
    public static <T extends Number> boolean isGeZero(T num) {
        return isEqZero(num) || num.intValue() > 0;
    }

    /**
     * 判断一个Number对象是否 < 0
     * @param num Number对象
     * @return true <=0
     */
    public static <T extends Number> boolean isLtZero(T num) {
        return !isGtZero(num);
    }

    /**
     * 判断一个Number对象是否 <= 0
     * @param num Number对象
     * @return true <=0
     */
    public static <T extends Number> boolean isLeZero(T num) {
        return !isGeZero(num);
    }

}

