package com.tqmall.search.canal.handle;

import com.tqmall.search.canal.RowChangedData;
import com.tqmall.search.canal.action.SchemaTables;
import com.tqmall.search.canal.action.TableAction;

import java.net.SocketAddress;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by xing on 16/2/24.
 * table处理级别的{@link CanalInstanceHandle}
 * 连续的事件更新, 发现不同表则处理掉
 */
public class TableSectionHandle extends ActionInstanceHandle<TableAction> {
    /**
     * 最近处理的schema
     * 只能canal获取数据的线程访问, 线程不安全的
     */
    private String lastSchema;
    /**
     * 最近处理的table
     * 只能canal获取数据的线程访问, 线程不安全的
     */
    private String lastTable;

    /**
     * 待处理数据集合列表
     * 只能canal获取数据的线程访问, 线程不安全的
     */
    private final List<RowChangedData> rowChangedDataList = new LinkedList<>();

    public TableSectionHandle(SocketAddress address, String destination, SchemaTables<TableAction> schemaTables) {
        super(address, destination, schemaTables);
    }

    private void runRowChangeAction() {
        if (rowChangedDataList.isEmpty()) return;
        schemaTables.getTable(lastSchema, lastTable).getAction().onAction(rowChangedDataList);
        rowChangedDataList.clear();
    }

    @Override
    protected HandleExceptionContext buildHandleExceptionContext(RuntimeException exception) {
        return HandleExceptionContext.build(exception)
                .schema(lastSchema)
                .table(lastTable)
                .changedData(rowChangedDataList)
                .create();
    }


    @Override
    protected void doRowChangeHandle(List<? extends RowChangedData> changedData) {
        //尽量集中处理
        if (!currentHandleTable.equals(lastTable) || !currentHandleSchema.equals(lastSchema)) {
            runRowChangeAction();
            lastSchema = currentHandleSchema;
            lastTable = currentHandleTable;
        }
        rowChangedDataList.addAll(changedData);
    }

    /**
     * 如果出现异常, 可以肯定方法{@link #runRowChangeAction()}至少调用过一次, 那么对应的{@link #lastSchema}, {@link #lastTable}需要更新
     *
     * @param exception      具体异常
     * @param inFinishHandle 标识是否在{@link #doFinishHandle()}中产生的异常
     * @return 是否忽略异常
     */
    @Override
    protected boolean exceptionHandle(RuntimeException exception, boolean inFinishHandle) {
        if (super.exceptionHandle(exception, inFinishHandle)) {
            lastSchema = currentHandleSchema;
            lastTable = currentHandleTable;
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
