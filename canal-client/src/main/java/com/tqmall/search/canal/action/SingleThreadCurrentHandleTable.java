package com.tqmall.search.canal.action;

import com.tqmall.search.canal.Schema;

/**
 * Created by xing on 16/2/28.
 * 单线程的{@link CurrentHandleTable}实现, 一般常用这个的
 * 当一个{@link Actionable}对象在多个canal实例使用时使用{@link MultiThreadCurrentHandleTable}
 */
public class SingleThreadCurrentHandleTable<T extends Actionable> implements CurrentHandleTable<T> {

    private Schema<T>.Table currentTable;

    @Override
    public final void setCurrentTable(Schema<T>.Table table) {
        this.currentTable = table;
    }

    @Override
    public final Schema<T>.Table getCurrentTable() {
        return currentTable;
    }
}
