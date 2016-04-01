package com.tqmall.search.commons.condition.expression;

import com.tqmall.search.commons.condition.ConditionContainer;
import com.tqmall.search.commons.condition.Conditions;
import com.tqmall.search.commons.condition.Operator;
import org.junit.Assert;
import org.junit.Test;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
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

        List<String> strValues = Arrays.asList("78", "xing", "wang");
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
        List<Integer> intValues = Arrays.asList(78, 34, 56);

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
        String conditionalExpression = "is_deleted != Y && (id > 12  || value range 23 <, <= 45)";
        ConditionContainer actual = Conditions.conditionalExpression(conditionalExpression);
        ConditionContainer expected = Conditions.unmodifiableContainer()
                .mustNotCondition(Conditions.equal("is_deleted", true))
                .mustCondition(Conditions.unmodifiableContainer()
                        .shouldCondition(Conditions.range("id", Integer.TYPE).gt(12).create())
                        .shouldCondition(Conditions.range("value", Integer.TYPE).gt(23).le(45).create())
                        .create())
                .create();
        Assert.assertEquals(expected, actual);

        conditionalExpression = "is_deleted != Y && ((id > 12  || value range 23 <, <= 45))";
        actual = Conditions.conditionalExpression(conditionalExpression);
        Assert.assertEquals(expected, actual);

        conditionalExpression = "(is_deleted != Y && (id > 12  || value range 23 <, <= 45))";
        actual = Conditions.conditionalExpression(conditionalExpression);
        Assert.assertEquals(expected, actual);

        //Note 这儿'N'会被识别成字符串
        conditionalExpression = "is_deleted = 'N'";
        actual = Conditions.conditionalExpression(conditionalExpression);
        expected = Conditions.unmodifiableContainer()
                .mustCondition(Conditions.equal("is_deleted", "N"))
                .create();
        Assert.assertEquals(expected, actual);

        conditionalExpression = "is_deleted = false && id in 1, 3, 5,7,9 && name nin \"xing\", wang";
        actual = Conditions.conditionalExpression(conditionalExpression);
        List<Integer> ids = Arrays.asList(1, 3, 5, 7, 9);
        List<String> names = Arrays.asList("xing", "wang");
        expected = Conditions.unmodifiableContainer()
                .mustCondition(Conditions.equal("is_deleted", false))
                .mustCondition(Conditions.in("id", Integer.TYPE, ids))
                .mustNotCondition(Conditions.in("name", String.class, names))
                .create();
        Assert.assertEquals(expected, actual);
    }

    //错误的表达式校验
    @Test
    public void invalidConditionExpressionTest() {
        String conditionalExpression;
        try {
            conditionalExpression = "(is_deleted = N";
            Conditions.conditionalExpression(conditionalExpression);
            Assert.fail("should have exception for condition expression: " + conditionalExpression);
        } catch (IllegalArgumentException ignored) {
        }
        try {
            conditionalExpression = "((is_deleted = N)";
            Conditions.conditionalExpression(conditionalExpression);
            Assert.fail("should have exception for condition expression: " + conditionalExpression);
        } catch (IllegalArgumentException ignored) {
        }
        try {
            conditionalExpression = "is_deleted =!= N";
            Conditions.conditionalExpression(conditionalExpression);
            Assert.fail("should have exception for condition expression: " + conditionalExpression);
        } catch (IllegalArgumentException ignored) {
        }
        try {
            conditionalExpression = "is_deleted in (N, 45)";
            Conditions.conditionalExpression(conditionalExpression);
            Assert.fail("should have exception for condition expression: " + conditionalExpression);
        } catch (IllegalArgumentException ignored) {
        }
    }

}
