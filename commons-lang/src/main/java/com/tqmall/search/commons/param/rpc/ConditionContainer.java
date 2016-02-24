package com.tqmall.search.commons.param.rpc;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created by xing on 16/1/24.
 * 条件容器, 默认Type为{@link Type#MUST}
 * 该条件容器是各个查询条件的集合
 *
 * @see InCondition
 * @see GtCondition
 * @see RangeCondition
 * @see EqualCondition
 */
public class ConditionContainer implements Serializable {

    private static final long serialVersionUID = 8536199694404404344L;

    private List<Condition> conditionList = new ArrayList<>();

    private final Type type;

    public ConditionContainer() {
        this(Type.MUST);
    }

    public ConditionContainer(Type type) {
        this.type = type;
    }

    public void addCondition(Condition condition) {
        conditionList.add(condition);
    }

    public List<Condition> getConditionList() {
        return Collections.unmodifiableList(conditionList);
    }

    public Type getType() {
        return type;
    }

    /**
     * 容器中条件类型
     */
    public enum Type {
        //且关系
        MUST,
        //或关系, 至少匹配一个, 类型外部调用基本上不会使用,Search内部qp可能会用到
        SHOULD,
        //非
        MUST_NOT
    }
}
