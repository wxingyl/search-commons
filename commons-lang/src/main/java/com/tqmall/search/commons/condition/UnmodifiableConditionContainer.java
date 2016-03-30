package com.tqmall.search.commons.condition;

import com.tqmall.search.commons.utils.CommonsUtils;

import java.util.*;

/**
 * Created by xing on 16/2/25.
 * 不能修改的ConditionContainer
 */
public class UnmodifiableConditionContainer extends ConditionContainer {

    private static final long serialVersionUID = -8399218221802294566L;

    public UnmodifiableConditionContainer(Collection<? extends Condition> must,
                                          Collection<? extends Condition> should,
                                          Collection<? extends Condition> mustNot,
                                          int minimumShouldMatch) {
        if (!CommonsUtils.isEmpty(must)) this.must = Collections.unmodifiableList(new ArrayList<>(must));
        if (!CommonsUtils.isEmpty(should)) this.should = Collections.unmodifiableList(new ArrayList<>(should));
        if (!CommonsUtils.isEmpty(mustNot)) this.mustNot = Collections.unmodifiableList(new ArrayList<>(mustNot));
        if (minimumShouldMatch > 1) this.minimumShouldMatch = minimumShouldMatch;
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

        /**
         * 添加到 must 条件
         */
        public Builder addMustCondition(Condition condition) {
            must.add(condition);
            return this;
        }

        /**
         * 根据type 添加条件, 默认{@link Type#MUST}
         */
        public Builder addCondition(Condition.Type type, Condition condition) {
            Objects.requireNonNull(type);
            if (type == Type.SHOULD) should.add(condition);
            else if (type == Type.MUST_NOT) mustNot.add(condition);
            else must.add(condition);
            return this;
        }

        /**
         * 根据type 添加条件, 默认{@link Type#MUST}
         */
        public Builder addCondition(Condition.Type type, Collection<? extends Condition> conditions) {
            Objects.requireNonNull(type);
            if (!CommonsUtils.isEmpty(conditions)) {
                if (type == Type.SHOULD) should.addAll(conditions);
                else if (type == Type.MUST_NOT) mustNot.addAll(conditions);
                else must.addAll(conditions);
            }
            return this;
        }

        public UnmodifiableConditionContainer create() {
            return new UnmodifiableConditionContainer(must, should, mustNot, minimumShouldMatch);
        }
    }
}
