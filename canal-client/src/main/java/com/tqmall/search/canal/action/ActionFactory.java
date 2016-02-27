package com.tqmall.search.canal.action;

import com.tqmall.search.canal.Schema;

import java.util.Iterator;

/**
 * Created by xing on 16/2/27.
 * schema.table的action提供者接口定义
 * 注意: 该接口实现要求返回的{@link #iterator()}不可修改的, 即{@link Iterator#remove()}操纵不支持
 * <p/>
 * 泛型 V 为{@link Schema.Table#action}的类型
 *
 * @see TableAction
 * @see EventTypeAction
 */
public interface ActionFactory<V extends Actionable> extends Iterable<Schema<V>> {

    /**
     * 通过schemaName, tableName获取对应的{@link Schema.Table}对象
     * 如果对应的table不存在, 返回null
     */
    Schema<V>.Table getTable(String schemaName, String tableName);

    /**
     * 通过schemaName获得对应的{@link Schema}对象
     * 如果对应的table不存在, 返回null
     */
    Schema<V> getSchema(String schemaName);
}
