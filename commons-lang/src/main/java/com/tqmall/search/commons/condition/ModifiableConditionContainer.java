package com.tqmall.search.commons.condition;

import java.util.*;

/**
 * Created by xing on 16/2/25.
 * 可修改的ConditionContainer
 */
public class ModifiableConditionContainer extends ConditionContainer {

    private static final long serialVersionUID = 1297988145518781514L;

    public ModifiableConditionContainer addMust(Condition condition) {
        appendMust(condition);
        return this;
    }

    public ModifiableConditionContainer addMust(Iterable<? extends Condition> it) {
        for (Condition c : it) {
            appendMust(c);
        }
        return this;
    }

    public ModifiableConditionContainer addShould(Condition condition) {
        appendShould(condition);
        return this;
    }

    public ModifiableConditionContainer addShould(Iterable<? extends Condition> it) {
        for (Condition c : it) {
            appendShould(c);
        }
        return this;
    }

    /**
     * 清理must中现有条件
     * @param isShould 是否清理should 条件
     */
    public ModifiableConditionContainer clear(boolean isShould) {
        if (isShould) {
            if (should != null) should.clear();
        } else {
            if (must != null) must.clear();
        }
        return this;
    }

    private void appendMust(Condition condition) {
        if (must == null) {
            must = new LinkedList<>();
        }
        must.add(condition);
    }

    private void appendShould(Condition condition) {
        if (should == null) {
            should = new LinkedList<>();
        }
        should.add(condition);
    }

    public ModifiableConditionContainer minimumShouldMatch(int minimumShouldMatch) {
        if (minimumShouldMatch > 0) {
            this.minimumShouldMatch = minimumShouldMatch;
        }
        return this;
    }

    @Override
    public int hashCode() {
        int result = ModifiableConditionContainer.class.hashCode();
        result = 31 * result + super.hashCode();
        return result;
    }

    @Override
    public boolean equals(Object o) {
        return o instanceof ModifiableConditionContainer && super.equals(o);
    }
}
