package com.tqmall.search.commons.param.condition;

import com.tqmall.search.commons.utils.CommonsUtils;

import java.util.Collections;
import java.util.List;

/**
 * Created by xing on 16/1/24.
 * in条件
 */
public class InCondition<T> extends Condition {

    private static final long serialVersionUID = -512938955299957421L;

    private final List<T> values;

    public InCondition(String field, List<T> values) {
        super(field);
        if (CommonsUtils.isEmpty(values)) throw new IllegalArgumentException("values list is null or empty");
        this.values = Collections.unmodifiableList(values);
    }

    public List<T> getValues() {
        return values;
    }

    @Override
    public boolean validation(Object value) {
        return values.contains(value);
    }

    @Override
    public String toString() {
        return "InCondition{" + super.toString() + ", values = " + values;
    }

    @Override
    public boolean equals(Object o) {
        return this == o || o instanceof InCondition && super.equals(o);
    }
    /**
     * 该build方法对传入的values做了过滤
     * @return 如果values无效, 返回null
     *
     * @see CommonsUtils#filterNullValue(List)
     */
    public static <T> InCondition<T> build(String field, List<T> values) {
        values = CommonsUtils.filterNullValue(values);
        if (values == null) return null;
        return new InCondition<>(field, values);
    }
}
