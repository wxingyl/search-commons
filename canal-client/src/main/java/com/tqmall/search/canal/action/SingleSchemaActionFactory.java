package com.tqmall.search.canal.action;

import com.google.common.collect.Iterators;
import com.tqmall.search.canal.Schema;

import java.util.Iterator;
import java.util.Objects;

/**
 * Created by xing on 16/2/27.
 * 只有单个schema的TableActionFactory
 */
public class SingleSchemaActionFactory<V extends Actionable> implements ActionFactory<V> {

    private final Schema<V> schema;

    public SingleSchemaActionFactory(Schema<V> schema) {
        Objects.requireNonNull(schema);
        this.schema = schema;
    }

    @Override
    public Schema<V>.Table getTable(String schemaName, String tableName) {
        return schema.getSchemaName().equals(schemaName) ? schema.getTable(tableName) : null;
    }

    @Override
    public Schema<V> getSchema(String schemaName) {
        return schema.getSchemaName().equals(schemaName) ? schema : null;
    }

    @Override
    public Iterator<Schema<V>> iterator() {
        return Iterators.singletonIterator(schema);
    }
}
