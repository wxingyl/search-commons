package com.tqmall.search.commons.condition;

import com.tqmall.search.commons.utils.CommonsUtils;
import org.junit.Assert;
import org.junit.Test;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by xing on 16/1/24.
 * condition test
 */
public class ConditionTest {

    @Test
    public void conditionContainerTest() {
        EqualCondition<Integer> equalCondition = Conditions.equal("id", 4);
        Assert.assertTrue(equalCondition.validation(4));
        Assert.assertFalse(equalCondition.validation(5));

        RangeCondition<BigDecimal> rangeCondition = Conditions.range("price", "1~10", BigDecimal.class);
        Assert.assertTrue(rangeCondition.validation(BigDecimal.ONE));
        Assert.assertTrue(rangeCondition.validation(BigDecimal.TEN));
        Assert.assertTrue(rangeCondition.validation(BigDecimal.valueOf(2.0)));
        Assert.assertFalse(rangeCondition.validation(BigDecimal.valueOf(-1.0)));

        InCondition<String> inCondition = Conditions.in("name", String.class, "xing", "wang", "yan", "lin");
        Assert.assertNotNull(inCondition);
        Assert.assertTrue(inCondition.validation("xing"));
        Assert.assertTrue(inCondition.validation("yan"));
        Assert.assertFalse(inCondition.validation("Xing"));

        ModifiableConditionContainer conditionContainer = new ModifiableConditionContainer()
                .addMust(equalCondition)
                .addMust(rangeCondition)
                .add(Condition.Type.SHOULD, inCondition);

        Map<String, String> dataMap = new HashMap<>();
        dataMap.put("id", "3");
        dataMap.put("price", "3.0");
        dataMap.put("value", "4");
        dataMap.put("name", "yan");
        Assert.assertFalse(conditionContainer.validation(CommonsUtils.convertToFunction(dataMap)));

        dataMap.put("id", "4");
        Assert.assertTrue(conditionContainer.validation(CommonsUtils.convertToFunction(dataMap)));
        dataMap.put("value", "2");
        conditionContainer.minimumShouldMatch(2);
        Assert.assertFalse(conditionContainer.validation(CommonsUtils.convertToFunction(dataMap)));

        conditionContainer.minimumShouldMatch(1);
        Assert.assertTrue(conditionContainer.validation(CommonsUtils.convertToFunction(dataMap)));

        conditionContainer.add(Condition.Type.MUST_NOT, equalCondition);
        Assert.assertFalse(conditionContainer.validation(CommonsUtils.convertToFunction(dataMap)));
    }

    /**
     * 测试 A && B || C 与 A || C && B是否相同, 是否满足交换律
     * 结果不等效, 只有当A = false, B = false, C = true时结果不同
     */
    @Test
    public void conditionPositionTest() {
        boolean[][] values = new boolean[8][3];
        for (int i = 0; i < 8; i++) {
            for (int j = 0; j < 3; j++) {
                values[i][j] = (i & 1 << j) != 0;
            }
            boolean a = values[i][0] && values[i][1] || values[i][2];
            boolean b = values[i][0] || values[i][2] && values[i][1];
            System.out.println("" + values[i][0] + ',' + values[i][1] + ',' + values[i][2] + ": a = " + a + ", b = " + b);
        }
    }

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
        List<FieldConditionToken> conditionTokens = FieldConditionToken.resolveCondition(ExpressionToken.resolveSentence(conditionalExpression));
        Assert.assertNotNull(conditionTokens);
        Assert.assertEquals(3, conditionTokens.size());
        Assert.assertEquals(new FieldConditionToken(Conditions.equal("is_deleted", false), true,
                conditionTokens.get(0).getTokenExtInfo()), conditionTokens.get(0));
        Assert.assertEquals(new FieldConditionToken(Conditions.range("id", Integer.class).gt(12).create(), false,
                conditionTokens.get(1).getTokenExtInfo()), conditionTokens.get(1));
        Assert.assertEquals(new FieldConditionToken(Conditions.range("value", Integer.class).gt(23).le(45).create(), false,
                conditionTokens.get(2).getTokenExtInfo()), conditionTokens.get(2));

        conditionalExpression = "is_deleted != 'N' && (id > 12.45  || value range 23.6 <, <= 45) && name in 78, xing, wang";
        conditionTokens = FieldConditionToken.resolveCondition(ExpressionToken.resolveSentence(conditionalExpression));
        Assert.assertNotNull(conditionTokens);
        Assert.assertEquals(4, conditionTokens.size());
        Assert.assertEquals(new FieldConditionToken(Conditions.equal("is_deleted", "N"), true,
                conditionTokens.get(0).getTokenExtInfo()), conditionTokens.get(0));
        Assert.assertEquals(new FieldConditionToken(Conditions.range("id", BigDecimal.class)
                .gt(BigDecimal.valueOf(12.45))
                .create(),
                false, conditionTokens.get(1).getTokenExtInfo()), conditionTokens.get(1));
        Assert.assertEquals(new FieldConditionToken(Conditions.range("value", BigDecimal.class)
                .gt(BigDecimal.valueOf(23.6))
                .le(BigDecimal.valueOf(45))
                .create(),
                false, conditionTokens.get(2).getTokenExtInfo()), conditionTokens.get(2));

        List<String> strValues = new ArrayList<>();
        strValues.add("78");
        strValues.add("xing");
        strValues.add("wang");
        Assert.assertEquals(new FieldConditionToken(Conditions.in("name", String.class, strValues), false,
                conditionTokens.get(3).getTokenExtInfo()), conditionTokens.get(3));

        conditionalExpression = "is_deleted != 'N' && (id > 1234567890  || value range 23.4 <, <= 45.5.6) && name in 78, 34, 56";
        conditionTokens = FieldConditionToken.resolveCondition(ExpressionToken.resolveSentence(conditionalExpression));
        Assert.assertNotNull(conditionTokens);
        Assert.assertEquals(4, conditionTokens.size());
        Assert.assertEquals(new FieldConditionToken(Conditions.equal("is_deleted", "N"), true,
                conditionTokens.get(0).getTokenExtInfo()), conditionTokens.get(0));
        Assert.assertEquals(new FieldConditionToken(Conditions.range("id", Long.class)
                .gt(1234567890L).create(),
                false, conditionTokens.get(1).getTokenExtInfo()), conditionTokens.get(1));
        Assert.assertEquals(new FieldConditionToken(Conditions.range("value", String.class)
                .gt("23.4")
                .le("45.5.6")
                .create(),
                false, conditionTokens.get(2).getTokenExtInfo()), conditionTokens.get(2));
        List<Integer> intValues = new ArrayList<>();
        intValues.add(78);
        intValues.add(34);
        intValues.add(56);
        Assert.assertEquals(new FieldConditionToken(Conditions.in("name", Integer.TYPE, intValues), false,
                conditionTokens.get(3).getTokenExtInfo()), conditionTokens.get(3));


    }
}
