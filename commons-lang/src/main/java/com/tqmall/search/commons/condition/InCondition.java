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
        this(field, values, convert, false);
    }

    public InCondition(String field, List<T> values, StrValueConvert<T> convert, boolean isNo) {
        super(field, convert, isNo);
        values = CommonsUtils.filterNullValue(values);
        if (values == null) throw new IllegalArgumentException("values list is null or empty");
        this.values = Collections.unmodifiableList(values);
    }

    public List<T> getValues() {
        return values;
    }

    @Override
    boolean doVerify(T value) {
        return values.contains(value);
    }

    @Override
    public String toString() {
        return super.toString() + " in " + values;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof InCondition)) return false;
        if (!super.equals(o)) return false;

        InCondition<?> that = (InCondition<?>) o;

        return values.equals(that.values);
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + values.hashCode();
        return result;
    }
}
