package com.tqmall.search.canal.action;

import com.tqmall.search.canal.Schema;

/**
 * Created by xing on 16/2/28.
 * {@link TableAction} 的抽象封装, 实现了{@link CurrentHandleTable<TableAction>}接口
 * 如果把一个canalInstance中的所有action写到一个类里面, 这个类就有用了
 */
public abstract class AbstractTableAction implements TableAction, CurrentHandleTable<TableAction> {

    private final CurrentHandleTable<TableAction> currentHandleTable;

    protected AbstractTableAction(CurrentHandleTable<TableAction> currentHandleTable) {
        this.currentHandleTable = currentHandleTable;
    }

    @Override
    public void setCurrentTable(Schema<TableAction>.Table table) {
        currentHandleTable.setCurrentTable(table);
    }

    @Override
    public final Schema<TableAction>.Table getCurrentTable() {
        return currentHandleTable.getCurrentTable();
    }
}
