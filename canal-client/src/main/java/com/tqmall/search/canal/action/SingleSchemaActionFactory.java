package com.tqmall.search.canal.action;

import com.google.common.collect.Iterators;
import com.tqmall.search.canal.Schema;

import java.util.Iterator;
import java.util.Objects;

/**
 * Created by xing on 16/2/27.
 * 只有单个schema的TableActionFactory
 */
public class SingleSchemaActionFactory<T extends Actionable> implements ActionFactory<T> {

    private final Schema<T> schema;

    public SingleSchemaActionFactory(Schema<T> schema) {
        Objects.requireNonNull(schema);
        this.schema = schema;
    }

    @Override
    public Schema<T>.Table getTable(String schemaName, String tableName) {
        return schema.getSchemaName().equals(schemaName) ? schema.getTable(tableName) : null;
    }

    @Override
    public Schema<T> getSchema(String schemaName) {
        return schema.getSchemaName().equals(schemaName) ? schema : null;
    }

    @Override
    public Iterator<Schema<T>> iterator() {
        return Iterators.singletonIterator(schema);
    }
}
