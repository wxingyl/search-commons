package com.tqmall.search.canal.action;

import com.tqmall.search.canal.RowChangedData;

import java.util.List;

/**
 * Created by xing on 16/3/17.
 * 可以提供给{@link EventTypeListAction}的{@link EventTypeAction}, 事件处理函数为doXXXAction(List)
 * 支持{@link CurrentHandleTable}
 * Note: 该Actionable为单线程操作, 也就是同一个LinkableTableAction对象实例不能同时工作于多个运行Canal实例
 *
 * @author xing
 */
public abstract class LinkedEventTypeAction extends AbstractEventTypeAction {

    protected LinkedEventTypeAction() {
        super(new SingleThreadCurrentHandleTable<EventTypeAction>());
    }

    /**
     * @return 是否继续执行下一个, false则中断执行
     */
    public abstract boolean doDeleteAction(List<RowChangedData.Delete> deletedData);

    /**
     * @return 是否继续执行下一个, false则中断执行
     */
    public abstract boolean doUpdateAction(List<RowChangedData.Update> updatedData);

    /**
     * @return 是否继续执行下一个, false则中断执行
     */
    public abstract boolean doInsertAction(List<RowChangedData.Insert> insertedData);

    @Override
    public final void onDeleteAction(List<RowChangedData.Delete> deletedData) {
        doDeleteAction(deletedData);
    }

    @Override
    public final void onUpdateAction(List<RowChangedData.Update> updatedData) {
        doUpdateAction(updatedData);
    }

    @Override
    public final void onInsertAction(List<RowChangedData.Insert> insertedData) {
        doInsertAction(insertedData);
    }
}
