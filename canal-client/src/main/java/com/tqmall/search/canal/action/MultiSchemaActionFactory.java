package com.tqmall.search.canal.action;

import com.tqmall.search.canal.Schema;

import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * Created by xing on 16/2/27.
 * 多个schema的TableActionFactory
 */
public class MultiSchemaActionFactory<T extends Actionable> implements ActionFactory<T> {

    private final Map<String, Schema<T>> schemaMap;

    public MultiSchemaActionFactory(Iterable<Schema<T>> schemas) {
        Map<String, Schema<T>> schemaMap = new HashMap<>();
        for (Schema<T> s : schemas) {
            schemaMap.put(s.getSchemaName(), s);
        }
        if (schemaMap.isEmpty()) throw new IllegalArgumentException("schemas is null or empty: " + schemas);
        this.schemaMap = Collections.unmodifiableMap(schemaMap);
    }

    @Override
    public Schema<T>.Table getTable(String schemaName, String tableName) {
        Schema<T> schema = schemaMap.get(schemaName);
        return schema == null ? null : schema.getTable(tableName);
    }

    @Override
    public Schema<T> getSchema(String schemaName) {
        return schemaMap.get(schemaName);
    }

    @Override
    public Iterator<Schema<T>> iterator() {
        return schemaMap.values().iterator();
    }
}
