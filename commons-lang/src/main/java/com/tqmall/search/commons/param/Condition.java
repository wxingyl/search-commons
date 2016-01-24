package com.tqmall.search.commons.param;

import java.io.Serializable;
import java.util.Objects;

/**
 * Created by xing on 16/1/23.
 * 条件抽象类
 */
abstract class Condition implements Serializable {

    private static final long serialVersionUID = 1L;

    private String field;

    public Condition(String field) {
        Objects.requireNonNull(field);
        this.field = field;
    }

    public String getField() {
        return field;
    }
}
