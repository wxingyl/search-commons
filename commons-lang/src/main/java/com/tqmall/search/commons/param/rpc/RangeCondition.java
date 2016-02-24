package com.tqmall.search.commons.param.rpc;

import com.tqmall.search.commons.lang.CommonsConst;
import com.tqmall.search.commons.lang.StrValueConvert;
import com.tqmall.search.commons.utils.SearchStringUtils;

/**
 * Created by xing on 16/1/22.
 * 区间选择条件, 左开右开: [start, end], 类似SQL中的 BETWEEN A AND B
 * {@link #start} 为null, 同 <= end
 * {@link #end} 为null, 同 >= start
 */
public class RangeCondition<T extends Comparable<T>> extends Condition {

    private static final long serialVersionUID = 8024467420617760387L;

    private final T start;

    private final T end;

    public RangeCondition(String field, T start, T end) {
        super(field);
        if (start == null && end == null) {
            throw new IllegalArgumentException("start and end can not be null value both");
        }
        this.start = start;
        this.end = end;
    }

    public T getEnd() {
        return end;
    }

    public T getStart() {
        return start;
    }

    @Override
    public boolean validation(Object value) {
        if (value == null) return false;
        T t = start == null ? end : start;
        if (t.getClass().isAssignableFrom(value.getClass())) {
            @SuppressWarnings({"rawtypes", "unchecked"})
            T tValue = (T) value;
            return !((start != null && tValue.compareTo(start) < 0) || (end != null && tValue.compareTo(end) > 0));
        } else return false;
    }

    @Override
    public String toString() {
        return "RangeCondition{" + super.toString() + ", start = " + start + ", end = " + end;
    }

    public static <T extends Comparable<T>> Builder<T> build(String field) {
        return new Builder<>(field);
    }

    /**
     * 对于多参数构造, 通过Build构造更好, 最起码能避免参数顺序错误
     *
     * @param <T>
     */
    public static class Builder<T extends Comparable<T>> {

        private T start, end;

        private String field;

        public Builder(String field) {
            this.field = field;
        }

        public Builder start(T start) {
            this.start = start;
            return this;
        }

        public Builder end(T end) {
            this.end = end;
            return this;
        }

        public RangeCondition<T> create() {
            return new RangeCondition<>(field, start, end);
        }

    }

    /**
     * @param field           range的字段, 不能为Null
     * @param rangeStr        range区间字符串, 如果isEmpty, return null.
     * @param strValueConvert 值转化器
     * @param <T>             对应类型
     * @return 构造好的RangeFilter对象
     */
    public static <T extends Comparable<T>> RangeCondition<T> build(final String field, final String rangeStr, final StrValueConvert<T> strValueConvert) {
        if (rangeStr == null || rangeStr.isEmpty()) return null;
        String[] rangeArray = SearchStringUtils.split(rangeStr, CommonsConst.RANGE_FILTER_CHAR);
        if (rangeArray.length == 0) return null;
        rangeArray = SearchStringUtils.stringArrayTrim(rangeArray);
        int startIndex = 0, endIndex = 1;
        if (rangeArray.length == 1) {
            if (rangeStr.charAt(0) == CommonsConst.RANGE_FILTER_CHAR) {
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

}
