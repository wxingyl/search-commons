package com.tqmall.search.commons.condition.expression;

import com.tqmall.search.commons.condition.FieldCondition;
import com.tqmall.search.commons.condition.Operator;
import com.tqmall.search.commons.condition.TokenExtInfo;
import com.tqmall.search.commons.exception.ResolveExpressionException;
import com.tqmall.search.commons.utils.CommonsUtils;

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
    private final FieldCondition fieldCondition;
    /**
     * 是否为非查询
     */
    private final boolean noCondition;
    /**
     * 条件附加信息
     */
    private final TokenExtInfo tokenExtInfo;

    FieldConditionToken(FieldCondition fieldCondition, boolean noCondition, TokenExtInfo tokenExtInfo) {
        this.fieldCondition = fieldCondition;
        this.noCondition = noCondition;
        this.tokenExtInfo = tokenExtInfo;
    }

    public FieldCondition getFieldCondition() {
        return fieldCondition;
    }

    public boolean isNoCondition() {
        return noCondition;
    }

    public TokenExtInfo getTokenExtInfo() {
        return tokenExtInfo;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof FieldConditionToken)) return false;

        FieldConditionToken that = (FieldConditionToken) o;

        if (noCondition != that.noCondition) return false;
        if (!fieldCondition.equals(that.fieldCondition)) return false;
        return tokenExtInfo.equals(that.tokenExtInfo);
    }

    @Override
    public int hashCode() {
        int result = fieldCondition.hashCode();
        result = 31 * result + (noCondition ? 1 : 0);
        result = 31 * result + tokenExtInfo.hashCode();
        return result;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        if (noCondition) sb.append("! ");
        sb.append(fieldCondition).append(" ext: ").append(tokenExtInfo);
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
     * @param expressionTokens 条件语句初步解析结果
     * @return 条件列表 list, 按照解析条件表达式的顺序返回
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
                        break;
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

}
