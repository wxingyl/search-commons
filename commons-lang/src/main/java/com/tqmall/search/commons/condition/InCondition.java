package com.tqmall.search.commons.condition;

import com.tqmall.search.commons.lang.StrValueConvert;
import com.tqmall.search.commons.utils.CommonsUtils;

import java.util.Collections;
import java.util.List;

/**
 * Created by xing on 16/1/24.
 * in条件
 */
public class InCondition<T> extends FieldCondition<T> {

    private static final long serialVersionUID = -512938955299957421L;

    private final List<T> values;

    public InCondition(String field, List<T> values, StrValueConvert<T> convert) {
        super(field, convert);
        values = CommonsUtils.filterNullValue(values);
        if (values == null) throw new IllegalArgumentException("values list is null or empty");
        this.values = Collections.unmodifiableList(values);
    }

    public List<T> getValues() {
        return values;
    }

    @Override
    public boolean validation(T value) {
        return values.contains(value);
    }

    @Override
    public String toString() {
        return "InCondition{" + super.toString() + ", values = " + values;
    }
}
