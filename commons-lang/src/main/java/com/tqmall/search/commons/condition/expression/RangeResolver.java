package com.tqmall.search.commons.condition.expression;

import com.tqmall.search.commons.condition.Conditions;
import com.tqmall.search.commons.condition.FieldCondition;
import com.tqmall.search.commons.condition.Operator;
import com.tqmall.search.commons.condition.RangeCondition;
import com.tqmall.search.commons.exception.ResolveExpressionException;
import com.tqmall.search.commons.lang.StrValueConvert;
import com.tqmall.search.commons.utils.CommonsUtils;
import com.tqmall.search.commons.utils.SearchStringUtils;
import com.tqmall.search.commons.utils.StrValueConverts;

import java.util.Map;

/**
 * Created by xing on 16/3/31.
 *
 * @author xing
 */
final class RangeResolver extends AbstractResolver<RangeResolver.Info> {

    static final RangeResolver INSTANCE = new RangeResolver();

    static class Info {

        final boolean lowerGe;

        final boolean upperLe;

        final Comparable lowerValue;

        final Comparable upperValue;

        Info(boolean lowerGe, boolean upperLe, Comparable lowerValue, Comparable upperValue) {
            this.lowerGe = lowerGe;
            this.upperLe = upperLe;
            this.lowerValue = lowerValue;
            this.upperValue = upperValue;
        }
    }

    @Override
    Map.Entry<StrValueConvert, Info> resolveValue(String value) throws ResolveExpressionException {
        String[] values = SearchStringUtils.splitTrim(value, ',');
        if (values.length != 2) {
            throw new ResolveExpressionException("list expression value: " + value + " have " + values.length
                    + " parts, it should be left and right parts");
        }
        Boolean lowerGe = null, upperLe = null;
        if (values[0].endsWith(Operator.LE.getOp())) {
            lowerGe = true;
        } else if (values[0].endsWith(Operator.LT.getOp())) {
            lowerGe = false;
        }
        if (values[1].startsWith(Operator.LE.getOp())) {
            upperLe = true;
        } else if (values[1].startsWith(Operator.LT.getOp())) {
            upperLe = false;
        }
        //两边的符号不能缺失
        if (lowerGe == null || upperLe == null) return null;
        String lowerStrValue = values[0].substring(0, values[0].length() - (lowerGe ? Operator.LE : Operator.LT).getOp().length()).trim();
        String upperStrValue = values[1].substring((upperLe ? Operator.LE : Operator.LT).getOp().length()).trim();
        //非法, 两个都必须要有值
        if (lowerStrValue.isEmpty() || upperStrValue.isEmpty()) {
            throw new ResolveExpressionException("list expression value: " + value + " leftValue: " + lowerStrValue
                    + ", rightValue: " + upperStrValue + " have empty value");
        }
        int clsType = CLASS_PRIORITY.indexOf(parseValueClass(lowerStrValue));
        int rightClsType = CLASS_PRIORITY.indexOf(parseValueClass(upperStrValue));
        if (rightClsType > clsType) clsType = rightClsType;
        //基础类型都实现了Comparable接口
        StrValueConvert convert = StrValueConverts.getBasicConvert(CLASS_PRIORITY.get(clsType));
        if (isStringClassType(clsType)) {
            lowerStrValue = filterStrValue(lowerStrValue);
            upperStrValue = filterStrValue(upperStrValue);
        }
        return CommonsUtils.newImmutableMapEntry(convert, new Info(lowerGe, upperLe,
                (Comparable) convert.convert(lowerStrValue), (Comparable) convert.convert(upperStrValue)));
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    @Override
    FieldCondition createFieldCondition(String field, Map.Entry<StrValueConvert, Info> value) {
        RangeCondition.Builder builder = Conditions.range(field, value.getKey());
        RangeResolver.Info info = value.getValue();
        if (info.lowerGe) {
            builder.ge(info.lowerValue);
        } else {
            builder.gt(info.lowerValue);
        }
        if (info.upperLe) {
            builder.le(info.upperValue);
        } else {
            builder.lt(info.upperValue);
        }
        return builder.create();
    }

    @Override
    public boolean supportOp(Operator op) {
        return Operator.RANGE == op;
    }

}
