package com.tqmall.search.commons.param.condition;

import com.tqmall.search.commons.lang.Function;
import com.tqmall.search.commons.utils.CommonsUtils;

import java.util.*;

/**
 * Created by xing on 16/3/29.
 * 两个条件容器做或判断的条件容器
 * 容器最好建议嵌套一次就行了, 再复杂的就算了~~~搞不了的
 *
 * @author xing
 * @see OrConditionContainer.MarkCondition
 */
public class OrConditionContainer extends ConditionContainer {

    private static final long serialVersionUID = 4089560784690809779L;

    private final ConditionContainer leftContainer;

    private final ConditionContainer rightContainer;

    public OrConditionContainer(ConditionContainer leftContainer, ConditionContainer rightContainer) {
        Objects.requireNonNull(leftContainer);
        Objects.requireNonNull(rightContainer);
        this.leftContainer = leftContainer;
        this.rightContainer = rightContainer;
    }

    @Override
    public boolean validation(Function<String, ?> valueSup) {
        Objects.requireNonNull(valueSup);
        return leftContainer.validation(valueSup) || rightContainer.validation(valueSup);
    }

    /**
     *
     */
    @Override
    public List<MarkCondition> getMust() {
        List<MarkCondition> markConditions = new ArrayList<>();
        appendMark(markConditions, leftContainer.getMust(), false);
        appendMark(markConditions, rightContainer.getMust(), true);
        return markConditions.isEmpty() ? null : Collections.unmodifiableList(markConditions);
    }

    @Override
    public List<MarkCondition> getShould() {
        List<MarkCondition> markConditions = new ArrayList<>();
        appendMark(markConditions, leftContainer.getShould(), false);
        appendMark(markConditions, rightContainer.getShould(), true);
        return markConditions.isEmpty() ? null : Collections.unmodifiableList(markConditions);
    }

    @Override
    public List<MarkCondition> getMustNot() {
        List<MarkCondition> markConditions = new ArrayList<>();
        appendMark(markConditions, leftContainer.getMustNot(), false);
        appendMark(markConditions, rightContainer.getMustNot(), true);
        return markConditions.isEmpty() ? null : Collections.unmodifiableList(markConditions);
    }

    @Override
    public Set<String> allConditionFields() {
        Set<String> left = leftContainer.allConditionFields();
        Set<String> right = rightContainer.allConditionFields();
        if (left == null) return right;
        else if (right == null) return left;
        else {
            left = new HashSet<>(left);
            left.addAll(right);
            return left;
        }
    }

    public ConditionContainer getLeftContainer() {
        return leftContainer;
    }

    public ConditionContainer getRightContainer() {
        return rightContainer;
    }

    private void appendMark(List<MarkCondition> appendConditions, List<? extends Condition> list, final boolean isRight) {
        if (CommonsUtils.isEmpty(list)) return;
        for (Condition c : list) {
            appendConditions.add(new MarkCondition(c, isRight));
        }
    }

    /**
     * 标识当前条件具体在{@link #leftContainer}还是{@link #rightContainer}
     */
    public class MarkCondition extends Condition {

        private static final long serialVersionUID = 7475138299916350662L;

        private final Condition source;

        private final boolean isRight;

        public MarkCondition(Condition source, boolean isRight) {
            super(source.getField());
            this.source = source;
            this.isRight = isRight;
        }

        @Override
        public boolean validation(Object value) {
            return source.validation(value);
        }

        public Condition getSource() {
            return source;
        }

        public boolean isRight() {
            return isRight;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof MarkCondition)) return false;
            MarkCondition that = (MarkCondition) o;

            return source.equals(that.source);
        }

        @Override
        public int hashCode() {
            return source.hashCode();
        }
    }
}
