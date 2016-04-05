package com.tqmall.search.commons.condition.expression;

import com.tqmall.search.commons.condition.Conditions;
import com.tqmall.search.commons.condition.FieldCondition;
import com.tqmall.search.commons.condition.Operator;
import com.tqmall.search.commons.exception.ResolveExpressionException;
import com.tqmall.search.commons.lang.StrValueConvert;
import com.tqmall.search.commons.utils.CommonsUtils;
import com.tqmall.search.commons.utils.SearchStringUtils;
import com.tqmall.search.commons.utils.StrValueConverts;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created by xing on 16/3/31.
 *
 * @author xing
 */
abstract class ListResolver extends AbstractResolver<List<Object>> {

    @Override
    final Map.Entry<StrValueConvert, List<Object>> resolveValue(String value) throws ResolveExpressionException {
        String[] values = SearchStringUtils.splitTrim(value, ',');
        int clsType = -1;
        for (String v : values) {
            int cType = CLASS_PRIORITY.indexOf(parseValueClass(v));
            if (cType > clsType) clsType = cType;
        }
        if (clsType == -1) {
            throw new ResolveExpressionException("list expression value: " + value + " is empty, can not find value class type");
        }
        List<Object> list = new ArrayList<>();
        StrValueConvert convert = StrValueConverts.getBasicConvert(CLASS_PRIORITY.get(clsType));
        boolean isStrType = isStringClassType(clsType);
        for (String v : values) {
            list.add(convert.convert(isStrType ? filterStrValue(v) : v));
        }
        return CommonsUtils.newImmutableMapEntry(convert, list);
    }

    static final class In extends ListResolver {

        static final In INSTANCE = new In();

        @SuppressWarnings({"rawtypes", "unchecked"})
        @Override
        FieldCondition createFieldCondition(String field, Map.Entry<StrValueConvert, List<Object>> value) {
            return Conditions.in(field, value.getValue(), value.getKey());
        }

        @Override
        public boolean supportOp(Operator op) {
            return Operator.IN == op;
        }
    }

    static final class Nin extends ListResolver {

        static final Nin INSTANCE = new Nin();

        @SuppressWarnings({"rawtypes", "unchecked"})
        @Override
        FieldCondition createFieldCondition(String field, Map.Entry<StrValueConvert, List<Object>> value) {
            return Conditions.nin(field, value.getValue(), value.getKey());
        }

        @Override
        public boolean supportOp(Operator op) {
            return Operator.NIN == op;
        }
    }
}


