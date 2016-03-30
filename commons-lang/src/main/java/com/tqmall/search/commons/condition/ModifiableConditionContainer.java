package com.tqmall.search.commons.condition;

import java.util.*;

/**
 * Created by xing on 16/2/25.
 * 可修改的ConditionContainer
 */
public class ModifiableConditionContainer extends ConditionContainer {

    private static final long serialVersionUID = 1297988145518781514L;

    /**
     * must用的比较多, 单独搞一个
     */
    public ModifiableConditionContainer addMust(Condition condition) {
        return add(Type.MUST, condition);
    }

    /**
     * must用的比较多, 单独搞一个
     */
    public ModifiableConditionContainer addMust(Iterable<? extends Condition> it) {
        return add(Type.MUST, it);
    }

    public ModifiableConditionContainer add(Type type, Iterable<? extends Condition> it) {
        for (Condition c : it) {
            add(type, c);
        }
        return this;
    }

    public ModifiableConditionContainer add(Type type, Condition condition) {
        switch (type) {
            case MUST:
                if (must == null) {
                    must = new ArrayList<>();
                }
                must.add(condition);
                break;
            case SHOULD:
                if (should == null) {
                    should = new ArrayList<>();
                }
                should.add(condition);
                break;
            case MUST_NOT:
                if (mustNot == null) {
                    mustNot = new ArrayList<>();
                }
                mustNot.add(condition);
                break;
            default:
                throw new IllegalArgumentException("type: " + type + " value is invalid");
        }
        return this;
    }

    /**
     * 清理现有条件, 通过types控制要清除哪些
     *
     * @param types 需要清除的类型
     * @see Condition.Type
     * @see EnumSet#of(Enum)
     */
    public void clear(Set<Type> types) {
        if (must != null && types.contains(Type.MUST)) must.clear();
        if (should != null && types.contains(Type.SHOULD)) should.clear();
        if (mustNot != null && types.contains(Type.MUST_NOT)) mustNot.clear();
    }

    public ModifiableConditionContainer minimumShouldMatch(int minimumShouldMatch) {
        if (minimumShouldMatch > 0) {
            this.minimumShouldMatch = minimumShouldMatch;
        }
        return this;
    }
}
