package com.tqmall.search.commons.param.condition;

import java.io.Serializable;
import java.util.Objects;

/**
 * Created by xing on 16/1/23.
 * 条件抽象类
 */
public abstract class Condition implements Serializable {

    private static final long serialVersionUID = 1L;

    private final String field;

    public Condition(String field) {
        Objects.requireNonNull(field);
        this.field = field;
    }

    public final String getField() {
        return field;
    }

    public abstract boolean validation(Object value);

    @Override
    public String toString() {
        return "field = " + field;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Condition)) return false;

        Condition condition = (Condition) o;

        return field.equals(condition.field);
    }

    @Override
    public int hashCode() {
        return field.hashCode();
    }

}
