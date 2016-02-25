package com.tqmall.search.commons.param.condition;

import com.tqmall.search.commons.utils.CommonsUtils;

import java.util.*;

/**
 * Created by xing on 16/2/25.
 * 不能修改的ConditionContainer
 */
public class UnmodifiableConditionContainer extends ConditionContainer {

    /**
     * 内部的{@link #must}, {@link #should}, {@link #mustNot}直接使用入参对象, 不重新创建了
     */
    public UnmodifiableConditionContainer(Collection<? extends Condition> must,
                                          Collection<? extends Condition> should,
                                          Collection<? extends Condition> mustNot,
                                          int minimumShouldMatch) {
        if (!CommonsUtils.isEmpty(must)) this.must = Collections.unmodifiableCollection(must);
        if (!CommonsUtils.isEmpty(should)) this.should = Collections.unmodifiableCollection(should);
        if (!CommonsUtils.isEmpty(mustNot)) this.mustNot = Collections.unmodifiableCollection(mustNot);
        if (minimumShouldMatch > 1) this.minimumShouldMatch = minimumShouldMatch;
    }

    @Override
    public Collection<Condition> getMust() {
        return must;
    }

    @Override
    public Collection<Condition> getShould() {
        return should;
    }

    @Override
    public Collection<Condition> getMustNot() {
        return mustNot;
    }

    public static Builder build() {
        return new Builder();
    }

    public static class Builder {

        private int minimumShouldMatch;

        private Set<Condition> must = new LinkedHashSet<>(), should = new LinkedHashSet<>(), mustNot = new LinkedHashSet<>();

        public Builder minimumShouldMatch(int minimumShouldMatch) {
            this.minimumShouldMatch = minimumShouldMatch;
            return this;
        }

        public Builder addMust(Condition... conditions) {
            if (conditions.length > 0) {
                Collections.addAll(must, conditions);
            }
            return this;
        }

        public Builder addMust(Collection<? extends Condition> conditions) {
            if (!CommonsUtils.isEmpty(conditions)) {
                must.addAll(conditions);
            }
            return this;
        }

        public Builder addShould(Condition... conditions) {
            if (conditions.length > 0) {
                Collections.addAll(should, conditions);
            }
            return this;
        }

        public Builder addShould(Collection<? extends Condition> conditions) {
            if (!CommonsUtils.isEmpty(conditions)) {
                should.addAll(conditions);
            }
            return this;
        }

        public Builder addMustNot(Condition... conditions) {
            if (conditions.length > 0) {
                Collections.addAll(mustNot, conditions);
            }
            return this;
        }

        public Builder addMustNot(Collection<? extends Condition> conditions) {
            if (!CommonsUtils.isEmpty(conditions)) {
                mustNot.addAll(conditions);
            }
            return this;
        }

        public UnmodifiableConditionContainer create() {
            return new UnmodifiableConditionContainer(must, should, mustNot, minimumShouldMatch);
        }
    }
}
