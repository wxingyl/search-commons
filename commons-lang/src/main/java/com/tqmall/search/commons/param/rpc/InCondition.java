package com.tqmall.search.commons.param.rpc;

import com.tqmall.search.commons.utils.CommonsUtils;

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
        this.values = values;
    }

    public List<T> getValues() {
        return values;
    }

    @Override
    public String toString() {
        return "InCondition{" + super.toString() + ", values = " + values;
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
