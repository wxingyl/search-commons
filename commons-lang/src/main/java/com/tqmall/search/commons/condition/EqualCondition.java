package com.tqmall.search.commons.condition;

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
        this(field, value, convert, false);
    }

    public EqualCondition(String field, T value, StrValueConvert<T> convert, boolean isNo) {
        super(field, convert, isNo);
        this.value = value;
    }

    public T getValue() {
        return value;
    }

    @Override
    boolean doVerify(T value) {
        return Objects.equals(this.value, value);
    }

    @Override
    public String toString() {
        return super.toString() + " = " + value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof EqualCondition)) return false;
        if (!super.equals(o)) return false;

        EqualCondition<?> that = (EqualCondition<?>) o;

        return value != null ? value.equals(that.value) : that.value == null;
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + (value != null ? value.hashCode() : 0);
        return result;
    }
}
