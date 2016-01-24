package com.tqmall.search.commons.utils;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;

/**
 * Created by xing on 16/1/22.
 * 该StringUtils供Client包内部使用, 只提供简单的几个函数
 */
public final class SearchStringUtils {

    public static final String[] EMPTY_STRING_ARRAY = new String[0];

    private SearchStringUtils() {
    }

    public static boolean isEmpty(String str) {
        return str == null || str.isEmpty();
    }

    /**
     * 过滤掉值为null的value
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
     * 过滤String, 返回的String是trim过的
     * 关键字不能为null, 不能为空, 并且trim后不能为空
     *
     * @return 返回的String是trim过的
     */
    public static String filterString(String q) {
        return (q != null && !q.isEmpty() && !(q = q.trim()).isEmpty()) ? q : null;
    }

    /**
     * 该函数实现来自Apache commons.lang3 的StringUtils中的split()方法的实现, 只是用这一个方法,
     * 为了避免过多依赖(client公共基础包尽量少依赖), 我们直接把该方法代码拿过来.
     * 申明: 不是自己原创, 是人家Apache的, 当然自己有了做了微小改动
     * <p/>
     * 下面的文档也是人家的:
     * <p>Splits the provided text into an array, separator specified.
     * This is an alternative to using StringTokenizer.</p>
     * <p/>
     * <p>The separator is not included in the returned String array.
     * Adjacent separators are treated as one separator.
     * For more control over the split use the StrTokenizer class.</p>
     * <p/>
     * <p>A {@code null} input String returns {@code null}.</p>
     * <p/>
     * <pre>
     * StringUtils.split(null, *)         = null
     * StringUtils.split("", *)           = []
     * StringUtils.split("a.b.c", '.')    = ["a", "b", "c"]
     * StringUtils.split("a..b.c", '.')   = ["a", "b", "c"]
     * StringUtils.split("a:b:c", '.')    = ["a:b:c"]
     * StringUtils.split("a b c", ' ')    = ["a", "b", "c"]
     * </pre>
     *
     * @param str           the String to parse, may be null
     * @param separatorChar the character used as the delimiter
     */
    public static String[] split(final String str, final char separatorChar) {
        if (str == null) {
            return null;
        }
        final int len = str.length();
        if (len == 0) {
            return EMPTY_STRING_ARRAY;
        }
        final List<String> list = new ArrayList<>();
        int i = 0, start = 0;
        boolean match = false;
        while (i < len) {
            if (str.charAt(i) == separatorChar) {
                if (match) {
                    list.add(str.substring(start, i));
                    match = false;
                }
                start = ++i;
            } else {
                match = true;
                i++;
            }
        }
        if (match) {
            list.add(str.substring(start, i));
        } else if (list.isEmpty()){
            return EMPTY_STRING_ARRAY;
        }
        return list.toArray(new String[list.size()]);
    }

    /**
     * 数组join
     */
    public static String join(final Object[] array, final char separator) {
        if (array == null) return null;
        else if (array.length == 0) return "";
        final StringBuilder buf = new StringBuilder(array.length * 16);
        if (array[0] != null) {
            buf.append(array[0]);
        }
        for (int i = 1; i < array.length; i++) {
            buf.append(separator);
            if (array[i] != null) {
                buf.append(array[i]);
            }
        }
        return buf.toString();
    }

    public static String join(final Iterable<?> iterable, final char separator) {
        if (iterable == null) return null;
        return join(iterable.iterator(), separator);
    }

    /**
     * 为null的原先不写入, 但是分隔符会写入
     * 该代码也是来自Apache commons.lang3 的StringUtils, 同上面{@link #split(String, char)}
     */
    public static String join(final Iterator<?> iterator, final char separator) {
        // handle null, zero and one elements before building a buffer
        if (iterator == null) {
            return null;
        }
        if (!iterator.hasNext()) {
            return "";
        }
        final Object first = iterator.next();
        if (!iterator.hasNext()) {
            return Objects.toString(first);
        }

        // two or more elements
        final StringBuilder buf = new StringBuilder(256); // Java default is 16, probably too small
        if (first != null) {
            buf.append(first);
        }

        while (iterator.hasNext()) {
            buf.append(separator);
            final Object obj = iterator.next();
            if (obj != null) {
                buf.append(obj);
            }
        }
        return buf.toString();
    }

    /**
     * 对字符串数组中的每个String做{@link #filterString(String)}操作, 即每个字符串做trim操作, 如果trim后为空, 则置为null
     * 注意: 该方法会破坏入参数据
     * @param array 字符串数据, 会破坏入参的值
     * @return trim过的字符串
     */
    public static String[] stringArrayTrim(String[] array) {
        for (int i = 0; i < array.length; i++) {
            array[i] = filterString(array[i]);
        }
        return array;
    }

}
