package com.tqmall.search.commons.param.rpc;

import java.util.Objects;

/**
 * Created by xing on 16/2/24.
 * 大于条件, >
 */
public class GtCondition<T extends Comparable<T>> extends Condition {

    private static final long serialVersionUID = 7025020405930822443L;

    private final T value;

    public GtCondition(String field, T value) {
        super(field);
        Objects.requireNonNull(value);
        this.value = value;
    }

    public T getValue() {
        return value;
    }

    @Override
    public boolean validation(Object value) {
        if (value == null) return false;
        if (this.value.getClass().isAssignableFrom(value.getClass())) {
            @SuppressWarnings({"rawtypes", "unchecked"})
            T tValue = (T) value;
            return tValue.compareTo(this.value) > 0;
        } else return false;
    }

    @Override
    public String toString() {
        return "GtCondition{" + "value=" + value + '}';
    }

    /**
     * 如果value无效, 返回null
     */
    public static <T extends Comparable<T>> GtCondition<T> build(String field, T value) {
        if (value == null) return null;
        else return new GtCondition<>(field, value);
    }
}
