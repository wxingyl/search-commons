package com.tqmall.search.commons.condition;

import com.tqmall.search.commons.lang.Function;
import com.tqmall.search.commons.lang.StrValueConvert;

import java.io.Serializable;
import java.util.Collections;
import java.util.Objects;
import java.util.Set;

/**
 * Created by xing on 16/1/23.
 * 条件抽象类
 */
public abstract class FieldCondition<T> implements Condition, Serializable {

    private static final long serialVersionUID = 1L;

    private final String field;

    private final StrValueConvert<T> valueConvert;

    public FieldCondition(String field, StrValueConvert<T> valueConvert) {
        Objects.requireNonNull(field);
        Objects.requireNonNull(valueConvert);
        this.field = field;
        this.valueConvert = valueConvert;
    }

    @Override
    public final Set<String> fields() {
        return Collections.singleton(field);
    }

    @Override
    public final boolean validation(Function<String, String> values) {
        return validation(valueConvert.convert(values.apply(field)));
    }

    public abstract boolean validation(T value);

    @Override
    public String toString() {
        return "field = " + field;
    }
}
