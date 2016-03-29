package com.tqmall.search.commons.param.condition;

/**
 * Created by xing on 16/1/22.
 * 范围比较, 支持设定等于区间设定, 可以实现 >, >=, >&<, >=&<= 等各个开闭区间组合
 */
public class RangeCondition<T extends Comparable<T>> extends Condition {

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

    /**
     * 默认左开右开区间比较, 即 value in [start, end]
     */
    public RangeCondition(String field, T start, T end) {
        this(field, start, false, end, false);
    }

    public RangeCondition(String field, T start, boolean excludeLower, T end, boolean excludeUpper) {
        super(field);
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
    public boolean validation(Object value) {
        if (value == null) return false;
        T t = start == null ? end : start;
        int cmpValue;
        if (t.getClass().isInstance(value)) {
            @SuppressWarnings({"rawtypes", "unchecked"})
            T tValue = (T) value;
            if (start != null) {
                cmpValue = tValue.compareTo(start);
                if (cmpValue < 0 || (excludeLower && cmpValue == 0)) return false;
            }
            if (end != null) {
                cmpValue = tValue.compareTo(end);
                if (cmpValue > 0 || (excludeUpper && cmpValue == 0)) return false;
            }
            return true;
        } else if (t instanceof Number && value instanceof Number) {
            double dValue = ((Number) value).doubleValue();
            if (start != null) {
                cmpValue = Double.compare(dValue, ((Number) start).doubleValue());
                if (cmpValue < 0 || (excludeLower && cmpValue == 0)) return false;
            }
            if (end != null) {
                cmpValue = Double.compare(dValue, ((Number) end).doubleValue());
                if (cmpValue > 0 || (excludeUpper && cmpValue == 0)) return false;
            }
            return true;
        } else return false;
    }

    @Override
    public String toString() {
        return "RangeCondition{" + super.toString() + ", start = " + start + ", end = " + end;
    }

    @Override
    public boolean equals(Object o) {
        return this == o || o instanceof RangeCondition && super.equals(o);
    }

    private final static int HASH_CODE_FACTOR = RangeCondition.class.getSimpleName().hashCode();

    @Override
    public int hashCode() {
        return 31 * super.hashCode() + HASH_CODE_FACTOR;
    }

    public static class Builder<T extends Comparable<T>> {

        private final String filed;

        private T start, end;

        private boolean excludeLower, excludeUpper;

        public Builder(String filed) {
            this.filed = filed;
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

        public RangeCondition<T> create() {
            return new RangeCondition<>(filed, start, excludeLower, end, excludeUpper);
        }
    }

}
