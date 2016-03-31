package com.tqmall.search.commons.condition;

import com.tqmall.search.commons.lang.StrValueConvert;
import com.tqmall.search.commons.param.Param;
import com.tqmall.search.commons.utils.CommonsUtils;
import com.tqmall.search.commons.utils.SearchStringUtils;
import com.tqmall.search.commons.utils.StrValueConverts;

import java.util.Arrays;
import java.util.List;

/**
 * Created by xing on 16/3/29.
 * {@link FieldCondition}相关工具类
 *
 * @author xing
 */
public final class Conditions {

    private Conditions() {
    }

    public static <T> InCondition<T> in(String field, List<T> values, Class<T> cls) {
        if (CommonsUtils.isEmpty(values)) return null;
        return new InCondition<>(field, values, StrValueConverts.getConvert(cls));
    }

    public static <T> InCondition<T> in(String field, List<T> values, StrValueConvert<T> convert) {
        if (CommonsUtils.isEmpty(values)) return null;
        return new InCondition<>(field, values, convert);
    }

    @SafeVarargs
    public static <T> InCondition<T> in(String field, Class<T> cls, T... values) {
        if (values.length == 0) return null;
        return new InCondition<>(field, Arrays.asList(values), StrValueConverts.getConvert(cls));
    }

    /**
     * @param value 不可以为null
     */
    @SuppressWarnings({"rawstype", "unchecked"})
    public static <T> EqualCondition<T> equal(String field, T value) {
        return new EqualCondition<>(field, value, StrValueConverts.getBasicConvert((Class<T>) value.getClass()));
    }

    /**
     * @param value 可以为null
     */
    public static <T> EqualCondition<T> equal(String field, T value, Class<T> cls) {
        return new EqualCondition<>(field, value, StrValueConverts.getBasicConvert(cls));
    }

    /**
     * @param value 可以为null
     */
    public static <T> EqualCondition<T> equal(String field, T value, StrValueConvert<T> convert) {
        return new EqualCondition<>(field, value, convert);
    }

    public static <T extends Comparable<T>> RangeCondition.Builder<T> range(String field) {
        return new RangeCondition.Builder<>(field);
    }

    public static <T extends Comparable<T>> RangeCondition.Builder<T> range(String field, Class<T> cls) {
        return new RangeCondition.Builder<>(field, StrValueConverts.getConvert(cls));
    }

    public static <T extends Comparable<T>> RangeCondition.Builder<T> range(String field, StrValueConvert<T> convert) {
        return new RangeCondition.Builder<>(field, convert);
    }

    public static <T extends Comparable<T>> RangeCondition<T> range(String field, String rangeStr, Class<T> cls) {
        return range(field, rangeStr, StrValueConverts.getBasicConvert(cls));
    }

    /**
     * @param field        range的字段, 不能为Null
     * @param rangeStr     range区间字符串, 如果isEmpty, return null.
     * @param valueConvert 值转化器
     * @param <T>          对应类型
     * @return 构造好的RangeFilter对象
     */
    public static <T extends Comparable<T>> RangeCondition<T> range(String field, String rangeStr, StrValueConvert<T> valueConvert) {
        if (SearchStringUtils.isEmpty(rangeStr)) return null;
        String[] rangeArray = SearchStringUtils.splitTrim(rangeStr, Param.RANGE_FILTER_CHAR);
        if (rangeArray.length == 0) return null;
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
            startValue = valueConvert.convert(rangeArray[0]);
        }
        if (endIndex >= 0 && rangeArray[endIndex] != null && '*' != rangeArray[endIndex].charAt(0)) {
            endValue = valueConvert.convert(rangeArray[endIndex]);
        }
        return new RangeCondition<>(field, startValue, false, endValue, false, valueConvert);
    }

    public static UnmodifiableConditionContainer.Builder unmodifiableContainer() {
        return new UnmodifiableConditionContainer.Builder();
    }

    public static ModifiableConditionContainer modifiableContainer() {
        return new ModifiableConditionContainer();
    }

    /**
     * 解析条件表达式, 等到条件容器对象
     *
     * @param conditionalExpression 条件表达式
     * @return 解析的容器集合对象
     */
    public static ConditionContainer parseConditionalExpression(String conditionalExpression) {
        List<ExpressionToken> tokenList = ExpressionToken.resolveSentence(conditionalExpression);
        return null;
    }

}
