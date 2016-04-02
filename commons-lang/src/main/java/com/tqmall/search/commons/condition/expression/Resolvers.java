package com.tqmall.search.commons.condition.expression;

import com.tqmall.search.commons.condition.FieldCondition;
import com.tqmall.search.commons.condition.Operator;
import com.tqmall.search.commons.exception.ResolveExpressionException;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created by xing on 16/4/2.
 * Resolver工具类
 *
 * @author xing
 */
public final class Resolvers {

    static final List<Resolver> RESOLVERS;

    static {
        List<Resolver> list = new ArrayList<>();
        list.add(EqualsResolver.Eq.INSTANCE);
        list.add(EqualsResolver.Ne.INSTANCE);
        list.add(new CmpSingleResolver(Operator.GT));
        list.add(new CmpSingleResolver(Operator.GE));
        list.add(new CmpSingleResolver(Operator.LT));
        list.add(new CmpSingleResolver(Operator.LE));
        list.add(ListResolver.In.INSTANCE);
        list.add(ListResolver.Nin.INSTANCE);
        list.add(RangeResolver.INSTANCE);
        RESOLVERS = Collections.unmodifiableList(list);
    }

    private Resolvers() {
    }


    /**
     * 条件解析, 从条件语句{@link ExpressionToken} 获取具体的字段条件表达式
     *
     * @param expressionToken 条件语句初步解析结果
     * @return 字段条件包装对象
     */
    public static FieldCondition resolveCondition(ExpressionToken expressionToken) {
        Operator op = expressionToken.getOp();
        FieldCondition fieldCondition = null;
        for (Resolver r : RESOLVERS) {
            if (r.supportOp(op)) {
                try {
                    fieldCondition = r.resolve(expressionToken.getField(), expressionToken.getValue());
                    break;
                } catch (ResolveExpressionException e) {
                    throw new IllegalArgumentException("condition expression: " + expressionToken + " format is invalid", e);
                }
            }
        }
        if (fieldCondition == null) {
            throw new IllegalStateException("condition expression: " + expressionToken + " can not resolve");
        }
        return fieldCondition;
    }
}
