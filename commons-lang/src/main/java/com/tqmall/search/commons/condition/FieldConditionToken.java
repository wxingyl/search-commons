package com.tqmall.search.commons.condition;

import com.tqmall.search.commons.exception.ResolveExpressionException;
import com.tqmall.search.commons.lang.StrValueConvert;
import com.tqmall.search.commons.utils.CommonsUtils;
import com.tqmall.search.commons.utils.SearchStringUtils;
import com.tqmall.search.commons.utils.StrValueConverts;

import java.math.BigDecimal;
import java.util.*;

/**
 * Created by xing on 16/3/30.
 * {@link FieldCondition} 具体的一个条件, 其从{@link ExpressionToken} 初始化过来
 *
 * @author xing
 */
public class FieldConditionToken {

    /**
     * 具体的FieldCondition
     */
    private final FieldCondition fieldCondition;
    /**
     * 是否为非查询
     */
    private final boolean isNot;
    /**
     * 条件附加信息
     */
    private final TokenExtInfo tokenExtInfo;

    FieldConditionToken(FieldCondition fieldCondition, boolean isNot, TokenExtInfo tokenExtInfo) {
        this.fieldCondition = fieldCondition;
        this.isNot = isNot;
        this.tokenExtInfo = tokenExtInfo;
    }

    public FieldCondition getFieldCondition() {
        return fieldCondition;
    }

    public boolean isNot() {
        return isNot;
    }

    public TokenExtInfo getTokenExtInfo() {
        return tokenExtInfo;
    }

    static final List<Resolver> RESOLVERS;

    static {
        List<Resolver> list = new ArrayList<>();
        list.add(EqResolver.INSTANCE);
        list.add(new CmpSingleResolver(Operator.GT));
        list.add(new CmpSingleResolver(Operator.GE));
        list.add(new CmpSingleResolver(Operator.LT));
        list.add(new CmpSingleResolver(Operator.LE));
        list.add(InResolver.INSTANCE);
        list.add(RangeResolver.INSTANCE);
        RESOLVERS = Collections.unmodifiableList(list);
    }


    /**
     * 条件解析, 从条件语句{@link ExpressionToken} 获取具体的字段条件表达式
     *
     * @param expressionTokens 条件语句初步解析结果
     * @return 条件列表
     */
    public static List<FieldConditionToken> resolveCondition(List<ExpressionToken> expressionTokens) {
        if (CommonsUtils.isEmpty(expressionTokens)) return null;
        List<FieldConditionToken> tokens = new ArrayList<>();
        for (ExpressionToken et : expressionTokens) {
            Operator op = et.getOp();
            FieldCondition fieldCondition = null;
            for (Resolver r : RESOLVERS) {
                if (r.supportOp(op)) {
                    try {
                        fieldCondition = r.resolve(et.getField(), et.getValue());
                    } catch (ResolveExpressionException e) {
                        throw new IllegalArgumentException("condition expression: " + et + " format is invalid", e);
                    }
                }
            }
            if (fieldCondition == null) {
                throw new IllegalStateException("condition expression: " + et + " can not resolve");
            }
            tokens.add(new FieldConditionToken(fieldCondition, op == Operator.NE || op == Operator.NIN, et.getTokenExtInfo()));
        }
        return tokens;
    }

    interface Resolver {

        FieldCondition resolve(String filed, String strValue) throws ResolveExpressionException;

        boolean supportOp(Operator op);
    }

    static abstract class AbstractResolver<T> implements Resolver {

        /**
         * 解析表达式的值部分, 返回null表示表达式值部分存在问题, 调用者应该抛出异常
         *
         * @param value 表达式的值部分, 需要提前做过term操作
         * @return 返回null表示表达式值部分存在问题, 调用者应该抛出异常
         */
        abstract Map.Entry<StrValueConvert, T> resolveValue(String value) throws ResolveExpressionException;

        abstract FieldCondition createFieldCondition(String field, Map.Entry<StrValueConvert, T> value);

        @Override
        public final FieldCondition resolve(String filed, String strValue) throws ResolveExpressionException {
            Map.Entry<StrValueConvert, T> value = resolveValue(strValue);
            return createFieldCondition(filed, value);
        }

        static final Set<String> BOOLEAN_VALUES;
        static final List<Class> CLASS_PRIORITY;

        static {
            Set<String> sets = new HashSet<>();
            sets.add("true");
            sets.add("1");
            sets.add("on");
            sets.add("yes");
            sets.add("y");
            sets.add("false");
            sets.add("0");
            sets.add("off");
            sets.add("no");
            sets.add("n");
            BOOLEAN_VALUES = Collections.unmodifiableSet(sets);
            List<Class> list = new ArrayList<>();
            list.add(Boolean.class);
            list.add(Integer.class);
            list.add(Long.class);
            list.add(BigDecimal.class);
            list.add(String.class);
            list.add(Date.class);
            CLASS_PRIORITY = Collections.unmodifiableList(list);
        }

        static boolean isStringClassType(int typeIndex) {
            return String.class == CLASS_PRIORITY.get(typeIndex);
        }

        /**
         * 识别该字符串表示的class
         *
         * @param value 字符串值, 左右都是经过trim的
         * @return 对应类型的class, 如果为null则value就是null了
         */
        static Class parseValueClass(String value) {
            if (value.equals("null")) return null;
            if (BOOLEAN_VALUES.contains(value.toLowerCase())) return Boolean.class;
            if (StrValueConverts.getBasicConvert(Date.class).convert(value) != null) return Date.class;
            boolean hadPoint = false;
            for (int i = value.length() - 1; i >= 0; i--) {
                char c = value.charAt(i);
                //非字符, 很定时字符串了
                if (c == '.') {
                    if (hadPoint) return String.class;
                    hadPoint = true;
                } else if (c < '0' || c > '9') {
                    return String.class;
                }
            }
            //超过10位的数字, 则为long
            return hadPoint ? BigDecimal.class : (value.length() >= 10 ? Long.class : Integer.class);
        }

        static String filterStrValue(String value) {
            int endPos = value.length() - 1;
            if (endPos > 0 && ((value.charAt(0) == '"' && value.charAt(endPos) == '"')
                    || (value.charAt(0) == '\'' && value.charAt(endPos) == '\''))) {
                return value.substring(1, endPos);
            } else {
                return value;
            }
        }
    }

    static class EqResolver extends AbstractResolver<Object> {

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

    static class CmpSingleResolver extends AbstractResolver<Comparable> {

        private final Operator cmpOp;

        CmpSingleResolver(Operator cmpOp) {
            if (cmpOp == Operator.GT || cmpOp == Operator.GE || cmpOp == Operator.LT || cmpOp == Operator.LE) {
                this.cmpOp = cmpOp;
            } else {
                throw new IllegalStateException(cmpOp + " is not single comparable operator");
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

    static class InResolver extends AbstractResolver<List<Object>> {

        static final InResolver INSTANCE = new InResolver();

        @Override
        Map.Entry<StrValueConvert, List<Object>> resolveValue(String value) throws ResolveExpressionException {
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

        @SuppressWarnings({"rawtypes", "unchecked"})
        @Override
        FieldCondition createFieldCondition(String field, Map.Entry<StrValueConvert, List<Object>> value) {
            return Conditions.in(field, value.getValue(), value.getKey());
        }

        @Override
        public boolean supportOp(Operator op) {
            return Operator.IN == op || Operator.NIN == op;
        }
    }

    static class RangeResolver extends AbstractResolver<RangeResolver.Info> {

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
            int clsType = CLASS_PRIORITY.indexOf(parseValueClass(values[0]));
            int rightClsType = CLASS_PRIORITY.indexOf(parseValueClass(values[1]));
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

}
