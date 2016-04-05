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
abstract class EqualsResolver extends AbstractResolver<Object> {

    @Override
    final Map.Entry<StrValueConvert, Object> resolveValue(String value) {
        Class cls = parseValueClass(value);
        if (cls == null) {
            return CommonsUtils.<StrValueConvert, Object>newImmutableMapEntry(StrValueConverts.getBasicConvert(String.class), null);
        } else {
            StrValueConvert convert = StrValueConverts.getBasicConvert(cls);
            if (cls == String.class) value = filterStrValue(value);
            return CommonsUtils.newImmutableMapEntry(convert, convert.convert(value));
        }
    }

    final static class Eq extends EqualsResolver {

        static final Eq INSTANCE = new Eq();

        @Override
        FieldCondition createFieldCondition(String field, Map.Entry<StrValueConvert, Object> value) {
            return Conditions.equal(field, value.getValue(), value.getKey());
        }

        @Override
        public boolean supportOp(Operator op) {
            return Operator.EQ == op;
        }
    }

    final static class Ne extends EqualsResolver {

        static final Ne INSTANCE = new Ne();

        @Override
        FieldCondition createFieldCondition(String field, Map.Entry<StrValueConvert, Object> value) {
            return Conditions.nEqual(field, value.getValue(), value.getKey());
        }

        @Override
        public boolean supportOp(Operator op) {
            return Operator.NE == op;
        }
    }

}
