package com.tqmall.search.common.utils;

import org.apache.commons.lang3.CharUtils;
import org.apache.commons.lang3.StringUtils;

/**
 * Created by xing on 15/12/31.
 * 一些{@link String} 和 {@link Character} 相关的公共方法
 * 其实很多方法在{@link StringUtils} 和 {@link CharUtils} 中有定义,
 * 该类中的方法只是对人家的一点点补充
 * 另外根据{@link String}转确定类型的{@link Number}, 可以参考{@link StrValueConvert} 以及 {@link StrValueConverts}
 * 建议优先使用{@link StringUtils} 和 {@link CharUtils} 中的方法, 如果没有再用这的
 */
public abstract class SearchStringUtils {

    /**
     * 对字符串数组中的每个String做trim, 注意: 该方法会破坏入参数据
     * @param array 字符串数据, 会破坏入参的值
     * @return trim过的字符串
     */
    public static String[] stringArrayTrim(String[] array) {
        for (int i = 0; i < array.length; i++) {
            array[i] = array[i].trim();
        }
        return array;
    }
}
