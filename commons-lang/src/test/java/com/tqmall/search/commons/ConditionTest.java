package com.tqmall.search.commons;

import com.tqmall.search.commons.param.condition.ConditionContainer;
import com.tqmall.search.commons.param.condition.EqualCondition;
import org.junit.Test;

/**
 * Created by xing on 16/1/24.
 * condition test
 */
public class ConditionTest {

    @Test
    public void conditionContainerTest() {
        ConditionContainer container = new ConditionContainer(ConditionContainer.Type.MUST_NOT);
        EqualCondition<Integer> condition = EqualCondition.build("id", 23);
        container.addCondition(condition);
        container.getMust();
    }
}
