package com.tqmall.search.commons.param.rpc;

import java.io.Serializable;
import java.util.Objects;

/**
 * Created by xing on 16/1/23.
 * 条件抽象类, 包访问权限
 */
public abstract class Condition implements Serializable {

    private static final long serialVersionUID = 1L;

    private final String field;

    public Condition(String field) {
        Objects.requireNonNull(field);
        this.field = field;
    }

    public String getField() {
        return field;
    }

    public abstract boolean validation(Object value);

    @Override
    public String toString() {
        return "field = " + field;
    }
}
