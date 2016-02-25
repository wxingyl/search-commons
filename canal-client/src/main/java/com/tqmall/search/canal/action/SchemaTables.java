package com.tqmall.search.canal.action;

import com.google.common.base.Function;
import com.google.common.collect.Collections2;
import com.google.common.collect.Iterators;
import com.tqmall.search.commons.utils.CommonsUtils;

import java.util.*;

/**
 * Created by xing on 16/2/24.
 * schema, table对应处理事件收集
 * V 类型为对应的Action, 即{@link TableAction}或者{@link EventTypeAction}
 *
 * @see TableAction
 * @see EventTypeAction
 */
public class SchemaTables<V> implements Iterable<SchemaTables.Schema<V>> {

    private final Schema<V>[] schemaArray;

    @SuppressWarnings({"rawtypes", "unchecked"})
    public SchemaTables(Collection<Schema<V>> schemas) {
        if (CommonsUtils.isEmpty(schemas)) throw new IllegalArgumentException("schemas is null or empty: " + schemas);
        this.schemaArray = schemas.toArray(new Schema[schemas.size()]);
    }

    public Table<V> getTable(String schemaName, String tableName) {
        for (int i = schemaArray.length - 1; i >= 0; i--) {
            if (schemaArray[i].schemaName.equals(schemaName)) {
                return schemaArray[i].getTable(tableName);
            }
        }
        return null;
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    @Override
    public Iterator<Schema<V>> iterator() {
        return Iterators.forArray(schemaArray);
    }

    public int size() {
        return schemaArray.length;
    }

    public Schema<V> get(int index) {
        if (index >= schemaArray.length)
            throw new IndexOutOfBoundsException("index: " + index + ", size: " + schemaArray.length);
        return schemaArray[index];
    }

    public static class Schema<V> implements Iterable<Table<V>> {

        private final String schemaName;

        /**
         * key tableName
         */
        private final Map<String, Table<V>> tableMap = new HashMap<>();

        public Schema(String schemaName, Collection<Table<V>> tables) {
            if (CommonsUtils.isEmpty(tables)) throw new IllegalArgumentException("tables is null or empty: " + tables);
            this.schemaName = schemaName;
            for (Table<V> t : tables) {
                tableMap.put(t.getTableName(), t);
            }
        }

        public String getSchemaName() {
            return schemaName;
        }

        public Table<V> getTable(String tableName) {
            return tableMap.get(tableName);
        }

        /**
         * 不可更改
         */
        public Collection<Table<V>> tables() {
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
        public Iterator<Table<V>> iterator() {
            return Iterators.unmodifiableIterator(tableMap.values().iterator());
        }

    }

    /**
     * table 对象实例
     *
     * @param <V>
     */
    public static class Table<V> {

        private final String tableName;

        /**
         * 该表中感兴趣的列, 不指定默认不做过滤
         * 目前还没有添加列过滤支持, 后续很快支持
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

        public Table(String tableName, V action, Collection<String> columns, TableColumnCondition columnCondition) {
            Objects.requireNonNull(action);
            Objects.requireNonNull(tableName);
            this.tableName = tableName;
            this.action = action;
            this.columnCondition = columnCondition;
            this.columns = CommonsUtils.isEmpty(columns) ? null
                    : Collections.unmodifiableSet(new HashSet<>(columns));
        }

        public String getTableName() {
            return tableName;
        }

        public V getAction() {
            return action;
        }

        /**
         * unmodifiableSet
         *
         * @see Collections#unmodifiableSet(Set)
         */
        public Set<String> getColumns() {
            return columns;
        }

        public TableColumnCondition getColumnCondition() {
            return columnCondition;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof Table)) return false;

            Table<?> table = (Table<?>) o;

            return tableName.equals(table.tableName);
        }

        @Override
        public int hashCode() {
            return tableName.hashCode();
        }

        public static <V> Builder<V> build(String tableName) {
            return new Builder<>(tableName);
        }

        public static class Builder<V> {
            private String tableName;
            private V action;
            private Set<String> columnSet = new HashSet<>();
            private TableColumnCondition columnCondition;

            public Builder(String tableName) {
                this.tableName = tableName;
            }

            public Builder<V> action(V action) {
                this.action = action;
                return this;
            }

            public Builder<V> columns(String... columns) {
                if (columns.length > 0) {
                    Collections.addAll(this.columnSet, columns);
                }
                return this;
            }

            public Builder<V> columns(Collection<String> columns) {
                if (!CommonsUtils.isEmpty(columns)) {
                    this.columnSet.addAll(columns);
                }
                return this;
            }

            public Builder<V> columnCondition(TableColumnCondition columnCondition) {
                this.columnCondition = columnCondition;
                return this;
            }

            public Table<V> create() {
                return new Table<>(tableName, action, columnSet, columnCondition);
            }
        }

    }

    public static <V> Builder<V> builder() {
        return new Builder<>();
    }

    public static class Builder<V> {

        private Map<String, Set<Table<V>>> schemaTableMap = new HashMap<>();

        private Set<Table<V>> getOrInit(String schemaName) {
            Set<Table<V>> tableList = schemaTableMap.get(schemaName);
            if (tableList == null) {
                tableList = new HashSet<>();
                schemaTableMap.put(schemaName, tableList);
            }
            return tableList;
        }

        public Builder<V> add(String schemaName, Collection<? extends Table<V>> tables) {
            if (CommonsUtils.isEmpty(tables)) throw new IllegalArgumentException("tables is null or empty: "
                    + tables);
            getOrInit(schemaName).addAll(tables);
            return this;
        }

        @SafeVarargs
        public final Builder<V> add(String schemaName, Table<V>... tables) {
            if (tables.length == 0) throw new IllegalArgumentException("tables length is 0");
            Collections.addAll(getOrInit(schemaName), tables);
            return this;
        }

        public SchemaTables<V> create() {
            return new SchemaTables<>(Collections2.transform(schemaTableMap.entrySet(), new Function<Map.Entry<String, Set<Table<V>>>, Schema<V>>() {
                @Override
                public Schema<V> apply(Map.Entry<String, Set<Table<V>>> e) {
                    return new Schema<>(e.getKey(), e.getValue());
                }
            }));
        }

    }
}
