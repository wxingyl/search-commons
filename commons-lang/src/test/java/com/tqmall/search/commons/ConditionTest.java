package com.tqmall.search.commons;

import com.tqmall.search.commons.param.rpc.ConditionContainer;
import com.tqmall.search.commons.param.rpc.EqualCondition;
import org.junit.Test;

/**
 * Created by xing on 16/1/24.
 * condition test
 */
public class ConditionTest {

    @Test
    public void conditionContainerTest() {
        ConditionContainer container = new ConditionContainer();
        EqualCondition<Integer> condition = EqualCondition.build("id", 23);
        container.addCondition(condition);
        container.getConditionList();
    }
}
