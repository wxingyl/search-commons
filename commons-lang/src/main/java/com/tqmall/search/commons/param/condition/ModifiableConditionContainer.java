package com.tqmall.search.commons.param.condition;

import com.tqmall.search.commons.utils.CommonsUtils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * Created by xing on 16/2/25.
 * 可修改的ConditionContainer
 */
public class ModifiableConditionContainer extends ConditionContainer {

    private static final long serialVersionUID = 1297988145518781514L;

    public ModifiableConditionContainer addMust(Condition... conditions) {
        if (conditions.length > 0) {
            if (must == null) {
                must = new ArrayList<>();
            }
            Collections.addAll(must, conditions);
        }
        return this;
    }

    public ModifiableConditionContainer addMust(Collection<? extends Condition> conditions) {
        if (!CommonsUtils.isEmpty(conditions)) {
            if (must == null) {
                must = new ArrayList<>();
            }
            must.addAll(conditions);
        }
        return this;
    }

    /**
     * 清理现有条件, 通过flag控制
     * 可以自由组合, 比如:
     * 清除 must和mustNot, {@link #MUST_TYPE} | {@link #MUST_NOT_TYPE}
     * 清楚所有, {@link #MUST_TYPE} | {@link #SHOULD_TYPE} {@link #MUST_NOT_TYPE}
     *
     * @param flag 标记位
     * @see #MUST_TYPE
     * @see #SHOULD_TYPE
     * @see #MUST_NOT_TYPE
     */
    public void clear(byte flag) {
        if (must != null && (flag & MUST_TYPE) != 0) must.clear();
        if (should != null && (flag & SHOULD_TYPE) != 0) should.clear();
        if (mustNot != null && (flag & MUST_NOT_TYPE) != 0) mustNot.clear();
    }

    public ModifiableConditionContainer setMinimumShouldMatch(int minimumShouldMatch) {
        if (minimumShouldMatch > 0) {
            this.minimumShouldMatch = minimumShouldMatch;
        }
        return this;
    }

    public ModifiableConditionContainer addShould(Condition... conditions) {
        if (conditions.length > 0) {
            if (should == null) {
                should = new ArrayList<>();
            }
            Collections.addAll(should, conditions);
        }
        return this;
    }

    public ModifiableConditionContainer addShould(Collection<? extends Condition> conditions) {
        if (!CommonsUtils.isEmpty(conditions)) {
            if (should == null) {
                should = new ArrayList<>();
            }
            should.addAll(conditions);
        }
        return this;
    }

    public ModifiableConditionContainer addMustNot(Condition... conditions) {
        if (conditions.length > 0) {
            if (mustNot == null) {
                mustNot = new ArrayList<>();
            }
            Collections.addAll(mustNot, conditions);
        }
        return this;
    }

    public ModifiableConditionContainer addMustNot(Collection<? extends Condition> conditions) {
        if (!CommonsUtils.isEmpty(conditions)) {
            if (mustNot == null) {
                mustNot = new ArrayList<>();
            }
            mustNot.addAll(conditions);
        }
        return this;
    }

    @Override
    public List<Condition> getMust() {
        return must == null ? null : Collections.unmodifiableList(must);
    }

    @Override
    public List<Condition> getShould() {
        return should == null ? null : Collections.unmodifiableList(should);
    }

    @Override
    public List<Condition> getMustNot() {
        return mustNot == null ? null : Collections.unmodifiableList(mustNot);
    }
}
