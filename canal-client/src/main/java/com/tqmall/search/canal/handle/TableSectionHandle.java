package com.tqmall.search.canal.handle;

import com.alibaba.otter.canal.protocol.CanalEntry;
import com.tqmall.search.canal.HandleExceptionContext;
import com.tqmall.search.canal.RowChangedData;
import com.tqmall.search.canal.action.SchemaTables;
import com.tqmall.search.canal.action.TableAction;

import java.net.SocketAddress;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by xing on 16/2/24.
 * table处理级别的{@link com.tqmall.search.canal.CanalInstanceHandle}
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
    protected void doRowChangeHandle(CanalEntry.Header header, List<? extends RowChangedData> changedData) {
        //尽量集中处理
        if (!header.getTableName().equals(lastTable) || !header.getSchemaName().equals(lastSchema)) {
            runRowChangeAction();
            lastSchema = header.getSchemaName();
            lastTable = header.getTableName();
        }
        rowChangedDataList.addAll(changedData);
    }

    @Override
    protected boolean exceptionHandle(RuntimeException exception, boolean inFinishHandle) {
        try {
            return super.exceptionHandle(exception, inFinishHandle);
        } finally {
            rowChangedDataList.clear();
        }
    }

    @Override
    protected void doFinishHandle() {
        runRowChangeAction();
    }
}
