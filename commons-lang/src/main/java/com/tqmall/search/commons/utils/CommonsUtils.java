package com.tqmall.search.commons.utils;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * Created by xing on 16/1/24.
 * 公共utils
 */
public final class CommonsUtils {

    public static boolean isEmpty(Collection collection) {
        return collection == null || collection.isEmpty();
    }

    public static boolean isEmpty(Map map) {
        return map == null || map.isEmpty();
    }

    /**
     * 过滤掉值为null的value
     * 参数建议使用List, Set, Map等开销较大, 不建议使用, 如果需要去重, 可以自己手动处理
     */
    public static <T> List<T> filterNullValue(List<T> list) {
        if (list == null || list.isEmpty()) return null;
        Iterator<T> it = list.iterator();
        while (it.hasNext()) {
            if (it.next() == null) it.remove();
        }
        return list.isEmpty() ? null : list;
    }

    /**
     * 判断一个Number对象是否 == 0
     *
     * @param num Number对象
     * @return true 等于0或者为null
     */
    public static <T extends Number> boolean isEqZero(T num) {
        return num == null || num.intValue() == 0;
    }

    /**
     * 判断一个Number对象是否 > 0
     *
     * @param num Number对象
     * @return true 大于0
     */
    public static <T extends Number> boolean isGtZero(T num) {
        return num != null && num.intValue() > 0;
    }

    /**
     * 判断一个Number对象是否 >= 0
     *
     * @param num Number对象
     * @return true 大于0
     */
    public static <T extends Number> boolean isGeZero(T num) {
        return isEqZero(num) || num.intValue() > 0;
    }

    /**
     * 判断一个Number对象是否 < 0
     *
     * @param num Number对象
     * @return true <=0
     */
    public static <T extends Number> boolean isLtZero(T num) {
        return !isGtZero(num);
    }

    /**
     * 判断一个Number对象是否 <= 0
     *
     * @param num Number对象
     * @return true <=0
     */
    public static <T extends Number> boolean isLeZero(T num) {
        return !isGeZero(num);
    }
}
