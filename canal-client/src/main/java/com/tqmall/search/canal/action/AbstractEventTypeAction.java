package com.tqmall.search.canal.action;

import com.tqmall.search.canal.Schema;

/**
 * Created by xing on 16/2/28.
 * {@link EventTypeAction} 的抽象封装, 实现了{@link CurrentHandleTable<EventTypeAction>}接口
 * 如果把一个canalInstance中的所有action写到一个类里面, 这个类就有用了
 */
public abstract class AbstractEventTypeAction implements EventTypeAction, CurrentHandleTable<EventTypeAction> {

    private final CurrentHandleTable<EventTypeAction> currentHandleTable;

    protected AbstractEventTypeAction(CurrentHandleTable<EventTypeAction> currentHandleTable) {
        this.currentHandleTable = currentHandleTable;
    }

    @Override
    public void setCurrentTable(Schema<EventTypeAction>.Table table) {
        currentHandleTable.setCurrentTable(table);
    }

    @Override
    public final Schema<EventTypeAction>.Table getCurrentTable() {
        return currentHandleTable.getCurrentTable();
    }
}
