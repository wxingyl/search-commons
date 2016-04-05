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
                                          int minimumShouldMatch) {
        if (!CommonsUtils.isEmpty(must)) this.must = Collections.unmodifiableList(new ArrayList<>(must));
        if (!CommonsUtils.isEmpty(should)) this.should = Collections.unmodifiableList(new ArrayList<>(should));
        if (minimumShouldMatch > 1) this.minimumShouldMatch = minimumShouldMatch;
    }

    @Override
    public boolean equals(Object o) {
        return o instanceof UnmodifiableConditionContainer && super.equals(o);
    }

    @Override
    public int hashCode() {
        int result = UnmodifiableConditionContainer.class.hashCode();
        result = 31 * result + super.hashCode();
        return result;
    }

    public static Builder build() {
        return new Builder();
    }

    public static class Builder {

        private int minimumShouldMatch;

        private List<Condition> must = new LinkedList<>(), should = new LinkedList<>();

        public Builder minimumShouldMatch(int minimumShouldMatch) {
            this.minimumShouldMatch = minimumShouldMatch;
            return this;
        }

        /**
         * 添加到 must 条件
         */
        public Builder mustCondition(Condition condition) {
            must.add(condition);
            return this;
        }

        /**
         * 添加到 must 条件
         */
        public Builder shouldCondition(Condition condition) {
            should.add(condition);
            return this;
        }

        /**
         * 添加到 must 条件
         */
        public Builder mustCondition(Collection<? extends Condition> conditions) {
            must.addAll(conditions);
            return this;
        }

        /**
         * 添加到 must 条件
         */
        public Builder shouldCondition(Collection<? extends Condition> conditions) {
            should.addAll(conditions);
            return this;
        }

        public UnmodifiableConditionContainer create() {
            return new UnmodifiableConditionContainer(must, should, minimumShouldMatch);
        }
    }
}
