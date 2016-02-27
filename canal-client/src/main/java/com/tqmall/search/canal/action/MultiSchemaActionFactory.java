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
public class MultiSchemaActionFactory<V extends Actionable> implements ActionFactory<V> {

    private final Map<String, Schema<V>> schemaMap;

    public MultiSchemaActionFactory(Iterable<Schema<V>> schemas) {
        Map<String, Schema<V>> schemaMap = new HashMap<>();
        for (Schema<V> s : schemas) {
            schemaMap.put(s.getSchemaName(), s);
        }
        if (schemaMap.isEmpty()) throw new IllegalArgumentException("schemas is null or empty: " + schemas);
        this.schemaMap = Collections.unmodifiableMap(schemaMap);
    }

    @Override
    public Schema<V>.Table getTable(String schemaName, String tableName) {
        Schema<V> schema = schemaMap.get(schemaName);
        return schema == null ? null : schema.getTable(tableName);
    }

    @Override
    public Schema<V> getSchema(String schemaName) {
        return schemaMap.get(schemaName);
    }

    @Override
    public Iterator<Schema<V>> iterator() {
        return schemaMap.values().iterator();
    }
}
