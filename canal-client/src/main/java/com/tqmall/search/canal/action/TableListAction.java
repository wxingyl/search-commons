package com.tqmall.search.canal.action;

import com.tqmall.search.canal.RowChangedData;
import com.tqmall.search.commons.utils.CommonsUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by xing on 16/3/17.
 * 支持链式调用的{@link LinkedTableAction}, 调用{@link LinkedTableAction#doAction(List)}的过程中发生{@link RuntimeException}, 则停止调用
 * Note: 该Actionable为单线程操作, 也就是同一个LinkableTableAction对象实例不能同时工作于多个运行Canal实例
 *
 * @author xing
 */
public class TableListAction extends AbstractTableAction {

    private final List<LinkedTableAction> actions;

    public TableListAction(List<LinkedTableAction> actions) {
        //只能单线程处理
        super(new SingleThreadCurrentHandleTable<TableAction>());
        if ((actions = CommonsUtils.filterNullValue(actions)) == null)
            throw new IllegalArgumentException("action list is empty");
        this.actions = new ArrayList<>(actions);
    }

    @Override
    public final void onAction(List<? extends RowChangedData> changedData) {
        for (LinkedTableAction action : actions) {
            action.setCurrentTable(getCurrentTable());
            if (!action.doAction(changedData)) return;
        }
    }
}
