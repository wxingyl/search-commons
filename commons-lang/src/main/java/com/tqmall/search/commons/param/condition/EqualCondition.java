package com.tqmall.search.commons.param.condition;

import com.tqmall.search.commons.lang.StrValueConvert;

import java.util.Objects;

/**
 * Created by xing on 16/1/24.
 * 等值比较条件, {@link #value}可以为null
 */
public class EqualCondition<T> extends FieldCondition<T> {

    private static final long serialVersionUID = 4180473296298181745L;

    /**
     * value可以为null
     */
    private final T value;

    public EqualCondition(String field, T value, StrValueConvert<T> convert) {
        super(field, convert);
        this.value = value;
    }

    public T getValue() {
        return value;
    }

    @Override
    public boolean validation(T value) {
        return Objects.equals(this.value, value);
    }

    @Override
    public String toString() {
        return "EqualCondition{" + super.toString() + ", value = " + value;
    }

}
