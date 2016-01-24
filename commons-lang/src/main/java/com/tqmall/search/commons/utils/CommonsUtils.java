package com.tqmall.search.commons.utils;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;

/**
 * Created by xing on 16/1/24.
 * 公共utils
 */
public final class CommonsUtils {

    public static boolean isEmpty(Collection collection) {
        return collection == null || collection.isEmpty();
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
}
