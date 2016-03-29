package com.tqmall.search.commons.param.condition;

import com.tqmall.search.commons.lang.StrValueConvert;
import com.tqmall.search.commons.param.Param;
import com.tqmall.search.commons.utils.CommonsUtils;
import com.tqmall.search.commons.utils.SearchStringUtils;

import java.util.Arrays;
import java.util.List;

/**
 * Created by xing on 16/3/29.
 * {@link Condition}相关工具类
 *
 * @author xing
 */
public final class Conditions {

    private Conditions() {
    }

    /**
     * 该build方法对传入的values做了null过滤
     * 构造{@link InCondition}
     *
     * @param values 如果不为空, 做完{@link CommonsUtils#filterNullValue(List)}过滤为空抛出{@link IllegalArgumentException}
     * @return 如果values为空, 返回null
     * @see CommonsUtils#filterNullValue(List)
     */
    public static <T> InCondition<T> in(String field, List<T> values) {
        if (CommonsUtils.isEmpty(values)) return null;
        return new InCondition<>(field, values);
    }

    /**
     * 该build方法对传入的values做了null过滤
     * 构造{@link InCondition}
     *
     * @param values 如果不为空, 做完{@link CommonsUtils#filterNullValue(List)}过滤为空抛出{@link IllegalArgumentException}
     * @return 如果values为空, 返回null
     * @see CommonsUtils#filterNullValue(List)
     */
    @SafeVarargs
    public static <T> InCondition<T> in(String field, T... values) {
        if (values.length == 0) return null;
        return new InCondition<>(field, Arrays.asList(values));
    }

    /**
     * @param value 可以为null
     */
    public static <T> EqualCondition<T> equal(String field, T value) {
        return new EqualCondition<>(field, value);
    }

    /**
     * 默认左开右开构造, 其他条件的构造建议使用{@link #range(String)}
     *
     * @see RangeCondition(String, Comparable, Comparable)
     * @see #range(String)
     * @see RangeCondition.Builder
     */
    public static <T extends Comparable<T>> RangeCondition<T> range(String field, T start, T end) {
        return new RangeCondition<>(field, start, end);
    }

    public static <T extends Comparable<T>> RangeCondition.Builder<T> range(String field) {
        return new RangeCondition.Builder<>(field);
    }

    /**
     * @param field           range的字段, 不能为Null
     * @param rangeStr        range区间字符串, 如果isEmpty, return null.
     * @param strValueConvert 值转化器
     * @param <T>             对应类型
     * @return 构造好的RangeFilter对象
     */
    public static <T extends Comparable<T>> RangeCondition<T> range(final String field, final String rangeStr, final StrValueConvert<T> strValueConvert) {
        if (rangeStr == null || rangeStr.isEmpty()) return null;
        String[] rangeArray = SearchStringUtils.split(rangeStr, Param.RANGE_FILTER_CHAR);
        if (rangeArray.length == 0) return null;
        rangeArray = SearchStringUtils.stringArrayTrim(rangeArray);
        int startIndex = 0, endIndex = 1;
        if (rangeArray.length == 1) {
            if (rangeStr.charAt(0) == Param.RANGE_FILTER_CHAR) {
                startIndex = -1;
                endIndex = 0;
            } else {
                startIndex = 0;
                endIndex = -1;
            }
        }
        T startValue = null, endValue = null;
        if (startIndex == 0 && rangeArray[0] != null && '*' != rangeArray[0].charAt(0)) {
            startValue = strValueConvert.convert(rangeArray[0]);
        }
        if (endIndex >= 0 && rangeArray[endIndex] != null && '*' != rangeArray[endIndex].charAt(0)) {
            endValue = strValueConvert.convert(rangeArray[endIndex]);
        }
        return new RangeCondition<>(field, startValue, endValue);
    }

    public static UnmodifiableConditionContainer.Builder unmodifiableContainer() {
        return new UnmodifiableConditionContainer.Builder();
    }

    public static ModifiableConditionContainer modifiableContainer() {
        return new ModifiableConditionContainer();
    }

    public static OrConditionContainer orContainer(ConditionContainer left, ConditionContainer right) {
        return new OrConditionContainer(left, right);
    }
}
