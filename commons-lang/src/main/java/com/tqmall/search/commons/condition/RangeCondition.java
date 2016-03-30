package com.tqmall.search.commons.condition;

import com.tqmall.search.commons.lang.StrValueConvert;
import com.tqmall.search.commons.utils.StrValueConverts;

/**
 * Created by xing on 16/1/22.
 * 范围比较, 支持设定等于区间设定, 可以实现 >, >=, > && <, >= && <= 等各个开闭区间组合
 * <p/>
 * 只能通过{@link Builder}构建
 *
 * @see Conditions#range(String)
 * @see Conditions#range(String, Class)
 * @see Conditions#range(String, StrValueConvert)
 */
public class RangeCondition<T extends Comparable<T>> extends FieldCondition<T> {

    private static final long serialVersionUID = 8024467420617760387L;

    private final T start;

    private final T end;

    /**
     * 是否排除最小值
     */
    private final boolean excludeLower;

    /**
     * 是否排除最大值
     */
    private final boolean excludeUpper;

    RangeCondition(String field, T start, boolean excludeLower, T end,
                   boolean excludeUpper, StrValueConvert<T> convert) {
        super(field, convert);
        if (start == null && end == null) {
            throw new IllegalArgumentException("start and end can not be null value both");
        }
        this.start = start;
        this.end = end;
        this.excludeLower = excludeLower;
        this.excludeUpper = excludeUpper;
    }

    public T getEnd() {
        return end;
    }

    public T getStart() {
        return start;
    }

    @Override
    public boolean validation(T value) {
        if (value == null) return false;
        int cmpValue;
        if (start != null) {
            cmpValue = value.compareTo(start);
            if (cmpValue < 0 || (excludeLower && cmpValue == 0)) return false;
        }
        if (end != null) {
            cmpValue = value.compareTo(end);
            if (cmpValue > 0 || (excludeUpper && cmpValue == 0)) return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "RangeCondition{" + super.toString() + ", start = " + start + ", end = " + end;
    }

    public static class Builder<T extends Comparable<T>> {

        private final String filed;

        private T start, end;

        private boolean excludeLower, excludeUpper;

        private StrValueConvert<T> convert;

        public Builder(String filed) {
            this.filed = filed;
        }

        public Builder(String filed, StrValueConvert<T> convert) {
            this.filed = filed;
            this.convert = convert;
        }

        /**
         * value > start
         */
        public Builder<T> gt(T value) {
            start = value;
            excludeLower = true;
            return this;
        }

        /**
         * value >= start
         */
        public Builder<T> ge(T value) {
            start = value;
            excludeLower = false;
            return this;
        }

        /**
         * value < end
         */
        public Builder<T> lt(T value) {
            end = value;
            excludeUpper = true;
            return this;
        }

        /**
         * value <= end
         */
        public Builder<T> le(T value) {
            end = value;
            excludeUpper = false;
            return this;
        }

        public Builder<T> convert(Class<T> cls) {
            this.convert = StrValueConverts.getBasicConvert(cls);
            return this;
        }

        public Builder<T> convert(StrValueConvert<T> convert) {
            this.convert = convert;
            return this;
        }

        public RangeCondition<T> create() {
            return new RangeCondition<>(filed, start, excludeLower, end, excludeUpper, convert);
        }
    }

}
