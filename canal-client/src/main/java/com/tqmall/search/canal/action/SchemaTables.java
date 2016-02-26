package com.tqmall.search.canal.action;

import com.google.common.collect.Iterators;
import com.tqmall.search.commons.utils.CommonsUtils;

import java.util.Collection;
import java.util.Iterator;
import java.util.Objects;

/**
 * Created by xing on 16/2/24.
 * schema, table对应处理事件收集
 * V 类型为对应的Action, 即{@link TableAction}或者{@link EventTypeAction}
 *
 * @see TableAction
 * @see EventTypeAction
 */
public class SchemaTables<V> implements Iterable<Schema<V>> {

    private final Schema<V>[] schemaArray;

    @SuppressWarnings({"rawtypes", "unchecked"})
    public SchemaTables(Schema<V> schema) {
        Objects.requireNonNull(schema);
        this.schemaArray = new Schema[1];
        this.schemaArray[0] = schema;
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    public SchemaTables(Collection<Schema<V>> schemas) {
        if (CommonsUtils.isEmpty(schemas)) throw new IllegalArgumentException("schemas is null or empty: " + schemas);
        this.schemaArray = schemas.toArray(new Schema[schemas.size()]);
    }

    public Schema<V>.Table getTable(String schemaName, String tableName) {
        for (int i = schemaArray.length - 1; i >= 0; i--) {
            if (schemaArray[i].getSchemaName().equals(schemaName)) {
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

}
