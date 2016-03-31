package com.tqmall.search.commons.condition.expression;

import com.tqmall.search.commons.condition.Conditions;
import com.tqmall.search.commons.condition.FieldCondition;
import com.tqmall.search.commons.condition.Operator;
import com.tqmall.search.commons.lang.StrValueConvert;
import com.tqmall.search.commons.utils.CommonsUtils;
import com.tqmall.search.commons.utils.StrValueConverts;

import java.util.Map;

/**
 * Created by xing on 16/3/31.
 *
 * @author xing
 */
class EqResolver extends AbstractResolver<Object> {

    static final EqResolver INSTANCE = new EqResolver();

    @Override
    Map.Entry<StrValueConvert, Object> resolveValue(String value) {
        Class cls = parseValueClass(value);
        if (cls == null) {
            return CommonsUtils.<StrValueConvert, Object>newImmutableMapEntry(StrValueConverts.getBasicConvert(String.class), null);
        } else {
            StrValueConvert convert = StrValueConverts.getBasicConvert(cls);
            if (cls == String.class) value = filterStrValue(value);
            return CommonsUtils.newImmutableMapEntry(convert, convert.convert(value));
        }
    }

    @Override
    FieldCondition createFieldCondition(String field, Map.Entry<StrValueConvert, Object> value) {
        return Conditions.equal(field, value.getValue(), value.getKey());
    }

    @Override
    public boolean supportOp(Operator op) {
        return Operator.EQ == op || Operator.NE == op;
    }
}
