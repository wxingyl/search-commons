package com.tqmall.search.canal.action;

import com.tqmall.search.canal.Schema;

/**
 * Created by xing on 16/2/28.
 * 多线程的{@link CurrentHandleTable}实现, 一般常用{@link SingleThreadCurrentHandleTable}, 当一个{@link Actionable}对象在多个canal实例使用时才用这玩意
 */
public class MultiThreadCurrentHandleTable<T extends Actionable> implements CurrentHandleTable<T> {

    private final ThreadLocal<Schema<T>.Table> currentTable = new ThreadLocal<>();

    @Override
    public final void setCurrentTable(Schema<T>.Table table) {
        currentTable.set(table);
    }

    @Override
    public final Schema<T>.Table getCurrentTable() {
        return currentTable.get();
    }

}
