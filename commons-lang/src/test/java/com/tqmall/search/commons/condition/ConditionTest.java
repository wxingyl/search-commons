package com.tqmall.search.commons.condition;

import com.tqmall.search.commons.utils.CommonsUtils;
import org.junit.Assert;
import org.junit.Test;

import java.math.BigDecimal;
import java.util.HashMap;
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
}
