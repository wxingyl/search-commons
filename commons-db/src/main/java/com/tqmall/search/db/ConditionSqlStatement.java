package com.tqmall.search.db;

import com.tqmall.search.commons.condition.*;
import com.tqmall.search.commons.utils.CommonsUtils;

import java.util.*;

/**
 * Created by xing on 16/4/5.
 * mysql 条件sql语句解析
 *
 * @author xing
 */
public class ConditionSqlStatement {

    private final List<Resolve> resolves;

    public ConditionSqlStatement() {
        this(Collections.<Resolve>emptyList());
    }

    /**
     * 扩展的resolve优先加入, 意味着解析的时候优先使用扩展resolve
     *
     * @param extResolves 扩展的resolve
     */
    public ConditionSqlStatement(List<Resolve> extResolves) {
        resolves = new ArrayList<>();
        if (!CommonsUtils.isEmpty(extResolves)) {
            resolves.addAll(extResolves);
        }
        resolves.add(new EqualConditionResolve());
        resolves.add(new ContainerResolve(this));
        resolves.add(new InConditionResolve());
        resolves.add(new RangeConditionResolve());
    }

    public void appendConditionContainer(StringBuilder sql, ConditionContainer container) {
        List<Condition> must = container.getMust(), should = container.getShould();
        if (CommonsUtils.isEmpty(must) || CommonsUtils.isEmpty(should)) return;
        ContainerResolve.append(this, sql, must, should);
    }

    final void appendCondition(StringBuilder sql, Condition condition) {
        for (Resolve r : resolves) {
            if (r.accept(condition)) {
                r.resolve(sql);
                return;
            }
        }
        throw new IllegalArgumentException("condition: " + condition + " can not found resolver");
    }

    public interface Resolve {

        /**
         * @param condition 条件对象
         * @return true 表示支持该对象解析, 可以调用{@link #resolve(StringBuilder)}, false表示还不行的
         */
        boolean accept(Condition condition);

        /**
         * sql条件语句填充
         */
        void resolve(StringBuilder sql);
    }

    public static abstract class FieldConditionResolve<T extends FieldCondition> implements Resolve {

        protected T condition;

        private final Class<T> cls;

        FieldConditionResolve(Class<T> cls) {
            this.cls = cls;
        }

        protected abstract void resolveValue(StringBuilder sql);

        @SuppressWarnings({"rawstypes", "unchecked"})
        @Override
        public final boolean accept(Condition condition) {
            if (cls.isInstance(condition)) {
                this.condition = (T) condition;
                return true;
            } else {
                return false;
            }
        }

        @Override
        public final void resolve(StringBuilder sql) {
            try {
                SqlStatements.appendField(sql, condition.getField()).append(' ');
                resolveValue(sql);
            } finally {
                this.condition = null;
            }
        }
    }

    static class EqualConditionResolve extends FieldConditionResolve<EqualCondition> {


        EqualConditionResolve() {
            super(EqualCondition.class);
        }

        @Override
        protected void resolveValue(StringBuilder sql) {
            if (condition.getValue() == null) {
                sql.append("IS ");
                if (condition.isNo()) sql.append("NOT ");
                sql.append("NULL ");
            } else {
                if (condition.isNo()) sql.append('!');
                sql.append("= ");
                SqlStatements.appendValue(sql, condition.getValue());
            }
        }

    }

    static class InConditionResolve extends FieldConditionResolve<InCondition> {

        InConditionResolve() {
            super(InCondition.class);
        }

        @Override
        protected void resolveValue(StringBuilder sql) {
            if (condition.isNo()) sql.append("NOT ");
            sql.append("IN ");
            if (CommonsUtils.isEmpty(condition.getValues())) {
                throw new IllegalArgumentException("in condition: " + condition + " values list is empty");
            }
            sql.append('(');
            for (Object o : condition.getValues()) {
                SqlStatements.appendValue(sql, o).append(", ");
            }
            sql.delete(sql.length() - 2, sql.length());
            sql.append(')');
        }
    }

    static class RangeConditionResolve extends FieldConditionResolve<RangeCondition> {

        RangeConditionResolve() {
            super(RangeCondition.class);
        }

        @Override
        protected void resolveValue(StringBuilder sql) {
            boolean haveStart = condition.getStart() != null;
            if (haveStart) {
                sql.append('>');
                if (!condition.isExcludeLower()) {
                    sql.append('=');
                }
                sql.append(' ');
                SqlStatements.appendValue(sql, condition.getStart());
            }
            if (condition.getEnd() != null) {
                if (haveStart) {
                    sql.append(" AND ");
                    SqlStatements.appendField(sql, condition.getField()).append(' ');
                }
                sql.append('<');
                if (!condition.isExcludeUpper()) {
                    sql.append('=');
                }
                SqlStatements.appendValue(sql, condition.getEnd());
            }
        }

    }

    static class ContainerResolve implements Resolve {

        private final ConditionSqlStatement conditionSqlStatement;

        private Deque<ConditionContainer> containerStack = new LinkedList<>();

        ContainerResolve(ConditionSqlStatement conditionSqlStatement) {
            this.conditionSqlStatement = conditionSqlStatement;
        }

        @Override
        public final boolean accept(Condition condition) {
            if (condition instanceof ConditionContainer) {
                containerStack.addLast((ConditionContainer) condition);
                return true;
            } else {
                return false;
            }
        }

        static void append(ConditionSqlStatement conditionSqlStatement, StringBuilder sql, List<Condition> must, List<Condition> should) {
            final boolean haveMust = !CommonsUtils.isEmpty(must);
            if (haveMust) {
                for (Condition c : must) {
                    conditionSqlStatement.appendCondition(sql, c);
                    sql.append(" AND");
                }
            }
            if (CommonsUtils.isEmpty(should)) {
                //这儿must肯定有元素, 前面已经判断了2者都为空直接return
                sql.delete(sql.length() - 4, sql.length());
            } else {
                if (haveMust) sql.append(" (");
                for (Condition c : should) {
                    conditionSqlStatement.appendCondition(sql, c);
                    sql.append(" OR");
                }
                sql.delete(sql.length() - 3, sql.length());
                if (haveMust) sql.append(')');
            }
        }

        @Override
        public void resolve(StringBuilder sql) {
            try {
                ConditionContainer container = containerStack.getLast();
                List<Condition> must = container.getMust(), should = container.getShould();
                if (CommonsUtils.isEmpty(must) || CommonsUtils.isEmpty(should)) return;
                sql.append('(');
                append(conditionSqlStatement, sql, must, should);
                sql.append(')');
            } finally {
                containerStack.removeLast();
            }
        }
    }

}
