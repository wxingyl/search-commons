package com.tqmall.search.canal.handle;

import com.alibaba.otter.canal.protocol.CanalEntry;
import com.tqmall.search.canal.RowChangedData;
import com.tqmall.search.canal.Schema;
import com.tqmall.search.canal.TableColumnCondition;
import com.tqmall.search.canal.action.*;
import com.tqmall.search.commons.lang.Function;

import java.net.SocketAddress;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;

/**
 * Created by xing on 16/2/24.
 * table处理级别的{@link CanalInstanceHandle}
 * 连续的事件更新, 发现不同schema, table则处理掉
 *
 * @see #runRowChangeAction()
 * @see TableAction
 */
public class TableSectionHandle extends ActionableInstanceHandle<TableAction> {
    /**
     * 最近处理的table
     * 只能canal获取数据的线程访问, 线程不安全的
     */
    private Schema<TableAction>.Table lastTable;

    /**
     * 待处理数据集合列表
     * 只能canal获取数据的线程访问, 线程不安全的
     */
    private final List<RowChangedData> rowChangedDataList = new LinkedList<>();

    public TableSectionHandle(SocketAddress address, String destination, ActionFactory<TableAction> schemaTables) {
        super(address, destination, schemaTables);
    }

    private void runRowChangeAction() {
        if (rowChangedDataList.isEmpty()) return;
        currentTable.getAction().onAction(rowChangedDataList);
        rowChangedDataList.clear();
    }

    @Override
    protected HandleExceptionContext buildHandleExceptionContext(RuntimeException exception) {
        return HandleExceptionContext.build(exception)
                .schema(lastTable.getSchemaName())
                .table(lastTable.getTableName())
                .changedData(rowChangedDataList)
                .create();
    }

    @Override
    protected void doRowChangeHandle(List<RowChangedData> changedData) {
        //尽量集中处理
        if (!currentTable.equals(lastTable)) {
            runRowChangeAction();
            lastTable = currentTable;
        }
        TableColumnCondition columnCondition;
        if (currentEventType == CanalEntry.EventType.UPDATE
                && (columnCondition = currentTable.getColumnCondition()) != null) {
            ListIterator<RowChangedData> it = changedData.listIterator();
            Function<String, String> beforeFunction = UpdateDataFunction.before();
            Function<String, String> afterFunction = UpdateDataFunction.after();
            try {
                while (it.hasNext()) {
                    RowChangedData.Update update = (RowChangedData.Update) it.next();
                    UpdateDataFunction.setUpdateData(update);

                    boolean beforeInvalid = !columnCondition.validation(beforeFunction);
                    boolean afterInvalid = !columnCondition.validation(afterFunction);
                    if (beforeInvalid && afterInvalid) {
                        //没有数据, 删除
                        it.remove();
                    } else if (beforeInvalid) {
                        it.set(update.transferToInsert());
                    } else if (afterInvalid) {
                        it.set(update.transferToDelete());
                    }
                }
            } finally {
                //要记得清楚掉, 避免内存泄露
                UpdateDataFunction.setUpdateData(null);
            }
        }
        rowChangedDataList.addAll(changedData);
    }

    /**
     * 如果出现异常, 可以肯定方法{@link #runRowChangeAction()}至少调用过一次, 那么对应的{@link #lastTable}需要更新
     *
     * @param exception      具体异常
     * @param inFinishHandle 标识是否在{@link #doFinishHandle()}中产生的异常
     * @return 是否忽略异常
     */
    @Override
    protected boolean exceptionHandle(RuntimeException exception, boolean inFinishHandle) {
        if (super.exceptionHandle(exception, inFinishHandle)) {
            lastTable = currentTable;
            return true;
        } else {
            return false;
        }
    }

    @Override
    protected void doFinishHandle() {
        try {
            runRowChangeAction();
        } finally {
            if (!rowChangedDataList.isEmpty()) {
                rowChangedDataList.clear();
            }
        }
    }
}
