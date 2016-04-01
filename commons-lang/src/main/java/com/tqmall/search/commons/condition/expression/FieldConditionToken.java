package com.tqmall.search.commons.condition.expression;

import com.tqmall.search.commons.condition.Condition;
import com.tqmall.search.commons.condition.FieldCondition;
import com.tqmall.search.commons.condition.Operator;
import com.tqmall.search.commons.exception.ResolveExpressionException;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

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
    private final Condition condition;
    /**
     * 是否为非查询
     */
    private final boolean noCondition;

    FieldConditionToken(Condition condition, boolean noCondition) {
        this.condition = condition;
        this.noCondition = noCondition;
    }

    public Condition getCondition() {
        return condition;
    }

    public boolean isNoCondition() {
        return noCondition;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof FieldConditionToken)) return false;

        FieldConditionToken that = (FieldConditionToken) o;

        if (noCondition != that.noCondition) return false;
        return condition.equals(that.condition);
    }

    @Override
    public int hashCode() {
        int result = condition.hashCode();
        result = 31 * result + (noCondition ? 1 : 0);
        return result;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        if (noCondition) sb.append("! ");
        sb.append(condition);
        return sb.toString();
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
     * @param expressionToken 条件语句初步解析结果
     * @return 字段条件包装对象
     */
    public static FieldConditionToken resolveCondition(ExpressionToken expressionToken) {
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
        return new FieldConditionToken(fieldCondition, op == Operator.NE || op == Operator.NIN);
    }

}
