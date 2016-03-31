package com.tqmall.search.commons.condition.expression;

import com.tqmall.search.commons.condition.FieldCondition;
import com.tqmall.search.commons.exception.ResolveExpressionException;
import com.tqmall.search.commons.lang.StrValueConvert;
import com.tqmall.search.commons.utils.StrValueConverts;

import java.math.BigDecimal;
import java.util.*;

/**
 * Created by xing on 16/3/31.
 *
 * @author xing
 */
abstract class AbstractResolver<T> implements Resolver {

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