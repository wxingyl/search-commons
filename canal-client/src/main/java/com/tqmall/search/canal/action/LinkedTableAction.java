package com.tqmall.search.canal.action;

import com.tqmall.search.canal.RowChangedData;

import java.util.List;

/**
 * Created by xing on 16/3/17.
 * 可以提供给{@link TableListAction}的{@link TableAction}, 事件处理函数为{@link #doAction(List)}
 * 支持{@link CurrentHandleTable}
 * Note: 该Actionable为单线程操作, 也就是同一个LinkableTableAction对象实例不能同时工作于多个运行Canal实例
 *
 * @author xing
 */
public abstract class LinkedTableAction extends AbstractTableAction {

    protected LinkedTableAction() {
        super(new SingleThreadCurrentHandleTable<TableAction>());
    }

    /**
     * @return 是否继续执行下一个, false则中断执行
     */
    public abstract boolean doAction(List<? extends RowChangedData> changedData);

    public final void onAction(List<? extends RowChangedData> changedData) {
        doAction(changedData);
    }
}
