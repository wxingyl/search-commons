package com.tqmall.search.canal.action;

import com.tqmall.search.canal.RowChangedData;
import com.tqmall.search.commons.utils.CommonsUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by xing on 16/3/17.
 * 支持链式调用的{@link LinkedEventTypeAction}, 调用doXXXAction()方法的过程中发生{@link RuntimeException}, 则停止调用
 * Note: 该Actionable为单线程操作, 也就是同一个LinkableTableAction对象实例不能同时工作于多个运行Canal实例
 *
 * @author xing
 */
public class EventTypeListAction extends AbstractEventTypeAction {

    private final List<LinkedEventTypeAction> actions;

    public EventTypeListAction(List<LinkedEventTypeAction> actions) {
        //只能单线程处理
        super(new SingleThreadCurrentHandleTable<EventTypeAction>());
        if ((actions = CommonsUtils.filterNullValue(actions)) == null)
            throw new IllegalArgumentException("action list is empty");
        this.actions = new ArrayList<>(actions);
    }

    @Override
    public final void onUpdateAction(List<RowChangedData.Update> updatedData) {
        callAction(UPDATE_CALL, updatedData);
    }

    @Override
    public final void onInsertAction(List<RowChangedData.Insert> insertedData) {
        callAction(INSERT_CALL, insertedData);
    }

    @Override
    public final void onDeleteAction(List<RowChangedData.Delete> deletedData) {
        callAction(DELETE_CALL, deletedData);
    }

    protected final <T extends RowChangedData> void callAction(CallAction<T> callAction, List<T> data) {
        for (LinkedEventTypeAction action : actions) {
            action.setCurrentTable(getCurrentTable());
            if (!callAction.call(action, data)) return;
        }
    }

    protected interface CallAction<T extends RowChangedData> {

        boolean call(LinkedEventTypeAction action, List<T> data);
    }

    private final static CallAction<RowChangedData.Insert> INSERT_CALL = new CallAction<RowChangedData.Insert>() {
        @Override
        public boolean call(LinkedEventTypeAction action, List<RowChangedData.Insert> data) {
            return action.doInsertAction(data);
        }
    };

    private final static CallAction<RowChangedData.Update> UPDATE_CALL = new CallAction<RowChangedData.Update>() {
        @Override
        public boolean call(LinkedEventTypeAction action, List<RowChangedData.Update> data) {
            return action.doUpdateAction(data);
        }
    };

    private final static CallAction<RowChangedData.Delete> DELETE_CALL = new CallAction<RowChangedData.Delete>() {
        @Override
        public boolean call(LinkedEventTypeAction action, List<RowChangedData.Delete> data) {
            return action.doDeleteAction(data);
        }
    };
}
