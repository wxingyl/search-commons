package com.tqmall.search.commons.condition.expression;

import com.tqmall.search.commons.condition.Conditions;
import com.tqmall.search.commons.condition.FieldCondition;
import com.tqmall.search.commons.condition.Operator;
import com.tqmall.search.commons.condition.RangeCondition;
import com.tqmall.search.commons.exception.ResolveExpressionException;
import com.tqmall.search.commons.lang.StrValueConvert;
import com.tqmall.search.commons.utils.CommonsUtils;
import com.tqmall.search.commons.utils.StrValueConverts;

import java.util.Map;

/**
 * Created by xing on 16/3/31.
 *
 * @author xing
 */
class CmpSingleResolver extends AbstractResolver<Comparable> {

    private final Operator cmpOp;

    CmpSingleResolver(Operator cmpOp) {
        if (cmpOp == Operator.GT || cmpOp == Operator.GE || cmpOp == Operator.LT || cmpOp == Operator.LE) {
            this.cmpOp = cmpOp;
        } else {
            throw new IllegalArgumentException(cmpOp + " is not single comparable operator");
        }
    }

    @Override
    final Map.Entry<StrValueConvert, Comparable> resolveValue(String value) throws ResolveExpressionException {
        Class cls = parseValueClass(value);
        if (cls == null) {
            throw new ResolveExpressionException("compare condition value can not be null");
        }
        StrValueConvert convert = StrValueConverts.getBasicConvert(cls);
        if (cls == String.class) value = filterStrValue(value);
        return CommonsUtils.newImmutableMapEntry(convert, (Comparable) convert.convert(value));
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    final FieldCondition createFieldCondition(String field, Map.Entry<StrValueConvert, Comparable> value) {
        RangeCondition.Builder builder = Conditions.range(field, value.getKey());
        if (cmpOp == Operator.GT) {
            builder.gt(value.getValue());
        } else if (cmpOp == Operator.GE) {
            builder.ge(value.getValue());
        } else if (cmpOp == Operator.LT) {
            builder.lt(value.getValue());
        } else {
            builder.le(value.getValue());
        }
        return builder.create();
    }

    @Override
    public boolean supportOp(Operator op) {
        return cmpOp == op;
    }
}
