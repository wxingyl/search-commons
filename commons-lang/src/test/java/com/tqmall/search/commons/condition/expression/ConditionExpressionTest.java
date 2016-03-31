package com.tqmall.search.commons.condition.expression;

import com.tqmall.search.commons.condition.ConditionContainer;
import com.tqmall.search.commons.condition.Conditions;
import com.tqmall.search.commons.condition.Operator;
import org.junit.Assert;
import org.junit.Test;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created by xing on 16/3/31.
 * 条件表达式junit test
 *
 * @author xing
 */
public class ConditionExpressionTest {

    @Test
    public void resolveExpressionSentenceTest() {
        String conditionalExpression = "is_deleted = 'N' && (id > 12  || value range 23 <, <= 45)";
        List<ExpressionToken> expressionTokens = ExpressionToken.resolveSentence(conditionalExpression);
        List<ExpressionToken> expectedTokens = new ArrayList<>();
        expectedTokens.add(new ExpressionToken("is_deleted", Operator.EQ, "'N'", TokenExtInfo.ZERO_PARENTHESIS_AND));
        expectedTokens.add(new ExpressionToken("id", Operator.GT, "12", TokenExtInfo.valueOf(1, 0, false)));
        expectedTokens.add(new ExpressionToken("value", Operator.RANGE, "23 <, 45 <=", TokenExtInfo.valueOf(0, 1, false)));
        Assert.assertEquals(expectedTokens, expressionTokens);

        conditionalExpression = "is_deleted = 'N' && (id > \" 12  || value range  34~45\" )";
        expressionTokens = ExpressionToken.resolveSentence(conditionalExpression);
        expectedTokens = new ArrayList<>();
        expectedTokens.add(new ExpressionToken("is_deleted", Operator.EQ, "'N'", TokenExtInfo.ZERO_PARENTHESIS_AND));
        expectedTokens.add(new ExpressionToken("id", Operator.GT, "\" 12  || value range  34~45\"", TokenExtInfo.ZERO_PARENTHESIS_OR));
        Assert.assertEquals(expectedTokens, expressionTokens);
    }

    @Test
    public void resolveFieldConditionToken() {
        String conditionalExpression = "is_deleted != N && (id > 12  || value range 23 <, <= 45)";
        List<FieldConditionToken> conditionTokens = resolveCondition(conditionalExpression);
        Assert.assertNotNull(conditionTokens);
        Assert.assertEquals(3, conditionTokens.size());
        Assert.assertEquals(new FieldConditionToken(Conditions.equal("is_deleted", false), true), conditionTokens.get(0));
        Assert.assertEquals(new FieldConditionToken(Conditions.range("id", Integer.class).gt(12).create(), false),
                conditionTokens.get(1));
        Assert.assertEquals(new FieldConditionToken(Conditions.range("value", Integer.class).gt(23).le(45).create(), false),
                conditionTokens.get(2));

        conditionalExpression = "is_deleted != 'N' && (id > 12.45  || value range 23.6 <, <= 45) && name in 78, xing, wang";
        conditionTokens = resolveCondition(conditionalExpression);
        Assert.assertNotNull(conditionTokens);
        Assert.assertEquals(4, conditionTokens.size());
        Assert.assertEquals(new FieldConditionToken(Conditions.equal("is_deleted", "N"), true), conditionTokens.get(0));
        Assert.assertEquals(new FieldConditionToken(Conditions.range("id", BigDecimal.class)
                .gt(BigDecimal.valueOf(12.45))
                .create(), false), conditionTokens.get(1));
        Assert.assertEquals(new FieldConditionToken(Conditions.range("value", BigDecimal.class)
                .gt(BigDecimal.valueOf(23.6))
                .le(BigDecimal.valueOf(45))
                .create(), false), conditionTokens.get(2));

        List<String> strValues = new ArrayList<>();
        strValues.add("78");
        strValues.add("xing");
        strValues.add("wang");
        Assert.assertEquals(new FieldConditionToken(Conditions.in("name", String.class, strValues), false), conditionTokens.get(3));

        conditionalExpression = "is_deleted != 'N' && (id > 1234567890  || value range 23.4 <, <= 45.5.6) && name in 78, 34, 56";
        conditionTokens = resolveCondition(conditionalExpression);
        Assert.assertNotNull(conditionTokens);
        Assert.assertEquals(4, conditionTokens.size());
        Assert.assertEquals(new FieldConditionToken(Conditions.equal("is_deleted", "N"), true), conditionTokens.get(0));
        Assert.assertEquals(new FieldConditionToken(Conditions.range("id", Long.class)
                .gt(1234567890L)
                .create(), false), conditionTokens.get(1));
        Assert.assertEquals(new FieldConditionToken(Conditions.range("value", String.class)
                .gt("23.4")
                .le("45.5.6")
                .create(), false), conditionTokens.get(2));
        List<Integer> intValues = new ArrayList<>();
        intValues.add(78);
        intValues.add(34);
        intValues.add(56);
        Assert.assertEquals(new FieldConditionToken(Conditions.in("name", Integer.TYPE, intValues), false), conditionTokens.get(3));
    }

    private List<FieldConditionToken> resolveCondition(String conditionalExpression) {
        List<ExpressionToken> ets = ExpressionToken.resolveSentence(conditionalExpression);
        if (ets == null) return Collections.emptyList();
        List<FieldConditionToken> list = new ArrayList<>();
        for (ExpressionToken et : ets) {
            list.add(FieldConditionToken.resolveCondition(et));
        }
        return list;
    }

    @Test
    public void conditionExpressionTest() {
        String conditionalExpression = "is_deleted != N && (id > 12  || value range 23 <, <= 45)";
        ConditionContainer container = Conditions.conditionalExpression(conditionalExpression);
        System.out.println(container);
    }
}
