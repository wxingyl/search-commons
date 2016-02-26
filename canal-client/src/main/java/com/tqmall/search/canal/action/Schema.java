package com.tqmall.search.canal.action;

import com.google.common.base.Function;
import com.google.common.collect.Lists;
import com.tqmall.search.commons.param.condition.Condition;
import com.tqmall.search.commons.param.condition.ConditionContainer;
import com.tqmall.search.commons.utils.CommonsUtils;

import java.util.*;

/**
 * Created by xing on 16/2/26.
 * schema 对象封装
 */
public class Schema<V> implements Iterable<Schema.Table> {

    private final String schemaName;

    /**
     * key tableName
     */
    private final Map<String, Table> tableMap = new HashMap<>();

    public Schema(String schemaName) {
        this.schemaName = schemaName;
    }

    public String getSchemaName() {
        return schemaName;
    }

    public Table getTable(String tableName) {
        return tableMap.get(tableName);
    }

    /**
     * 不可更改
     */
    public Collection<Table> tables() {
        return Collections.unmodifiableCollection(tableMap.values());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Schema)) return false;

        Schema<?> schema = (Schema<?>) o;

        return schemaName.equals(schema.schemaName);

    }

    @Override
    public int hashCode() {
        return schemaName.hashCode();
    }

    @Override
    public Iterator<Table> iterator() {
        return tableMap.values().iterator();
    }

    /**
     * 创建一个Table对象
     */
    public Schema<V> addTable(TableBuilder<V> builder) {
        tableMap.put(builder.tableName, new Table(builder.tableName, builder.action, builder.columns, builder.columnCondition));
        return this;
    }

    public class Table {

        private final String tableName;

        /**
         * 该表中感兴趣的列, 不指定默认不做过滤
         * 建议设置上该值, 防止大量无关数据更新拖累
         */
        private final Set<String> columns;
        /**
         * 该表对应事件
         */
        private final V action;

        /**
         * 列条件筛选容器
         */
        private final TableColumnCondition columnCondition;

        Table(String tableName, V action, Collection<String> columns, TableColumnCondition columnCondition) {
            Objects.requireNonNull(action);
            Objects.requireNonNull(tableName);
            this.tableName = tableName;
            this.action = action;
            this.columnCondition = columnCondition;
            if (!CommonsUtils.isEmpty(columns)) {
                Set<String> columnSet = new HashSet<>(columns);
                if (columnCondition != null) {
                    /**
                     * 要保证在判断条件中的column添加到{@link #columns}
                     */
                    ConditionContainer conditionContainer = columnCondition.getConditionContainer();
                    Function<Condition, String> function = new Function<Condition, String>() {
                        @Override
                        public String apply(Condition input) {
                            return input.getField();
                        }
                    };
                    if (!CommonsUtils.isEmpty(conditionContainer.getMust())) {
                        columnSet.addAll(Lists.transform(conditionContainer.getMust(), function));
                    }
                    if (!CommonsUtils.isEmpty(conditionContainer.getShould())) {
                        columnSet.addAll(Lists.transform(conditionContainer.getShould(), function));
                    }
                    if (!CommonsUtils.isEmpty(conditionContainer.getMustNot())) {
                        columnSet.addAll(Lists.transform(conditionContainer.getMustNot(), function));
                    }
                }
                this.columns = Collections.unmodifiableSet(columnSet);
            } else this.columns = null;
        }

        public String getSchemaName() {
            return schemaName;
        }

        public String getTableName() {
            return tableName;
        }

        public V getAction() {
            return action;
        }

        /**
         * unmodifiableSet, 如果没有过滤字段, 或者{@link #columns} isEmpty() 为true, 则返回null,
         * 即返回结果不为null肯定存在感兴趣column
         *
         * @see Collections#unmodifiableSet(Set)
         */
        public Set<String> getColumns() {
            return columns;
        }

        public TableColumnCondition getColumnCondition() {
            return columnCondition;
        }

    }


    public static <V> TableBuilder<V> buildTable(String tableName) {
        return new TableBuilder<>(tableName);
    }

    public static class TableBuilder<V> {
        private String tableName;
        private V action;
        private Set<String> columns = new HashSet<>();
        private TableColumnCondition columnCondition;

        public TableBuilder(String tableName) {
            this.tableName = tableName;
        }

        public TableBuilder<V> action(V action) {
            this.action = action;
            return this;
        }

        public TableBuilder<V> columns(String... columns) {
            if (columns.length > 0) {
                Collections.addAll(this.columns, columns);
            }
            return this;
        }

        public TableBuilder<V> columns(Collection<String> columns) {
            if (!CommonsUtils.isEmpty(columns)) {
                this.columns.addAll(columns);
            }
            return this;
        }

        public TableBuilder<V> columnCondition(TableColumnCondition columnCondition) {
            this.columnCondition = columnCondition;
            return this;
        }
    }

}
