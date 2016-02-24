package com.tqmall.search.canal.handle;

import com.alibaba.otter.canal.protocol.CanalEntry;
import com.tqmall.search.canal.RowChangedData;
import com.tqmall.search.canal.action.EventTypeAction;
import com.tqmall.search.canal.action.SchemaTables;

import java.net.SocketAddress;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by xing on 16/2/23.
 * 基于表级别, 每个事件如果
 * 连续的事件更新, 发现不同表则处理掉
 */
public class EventTypeSectionHandle extends ActionInstanceHandle<EventTypeAction> {
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
     * 最近处理的tableEvent
     * 只能canal获取数据的线程访问, 线程不安全的
     */
    private CanalEntry.EventType lastEventType;

    /**
     * 待处理数据集合列表
     * 只能canal获取数据的线程访问, 线程不安全的
     */
    private final List<RowChangedData> rowChangedDataList = new LinkedList<>();

    /**
     * @param address     canal服务器地址
     * @param destination canal实例名称
     */
    public EventTypeSectionHandle(SocketAddress address, String destination, SchemaTables<EventTypeAction> schemaTables) {
        super(address, destination, schemaTables);
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    private <T extends RowChangedData> List<T> rowChangedDataTransfer(List<T> list) {
        for (RowChangedData r : rowChangedDataList) {
            list.add((T) r);
        }
        return list;
    }

    private void runRowChangeAction() {
        if (rowChangedDataList.isEmpty()) return;
        EventTypeAction action = schemaTables.getTable(lastSchema, lastTable).getAction();
        switch (lastEventType) {
            case DELETE:
                action.onDeleteAction(rowChangedDataTransfer(new ArrayList<RowChangedData.Delete>()));
                break;
            case UPDATE:
                action.onUpdateAction(rowChangedDataTransfer(new ArrayList<RowChangedData.Update>()));
            case INSERT:
                action.onInsertAction(rowChangedDataTransfer(new ArrayList<RowChangedData.Insert>()));
            default:
                //can not reach here
                throw new UnsupportedOperationException("unsupported eventType: " + lastEventType);
        }
        rowChangedDataList.clear();
    }

    @Override
    protected void doRowChangeHandle(CanalEntry.Header header, List<? extends RowChangedData> changedData) {
        //尽量集中处理
        if (!header.getTableName().equals(lastTable) || !header.getSchemaName().equals(lastSchema)
                || !header.getEventType().equals(lastEventType)) {
            runRowChangeAction();
            lastSchema = header.getSchemaName();
            lastTable = header.getTableName();
            lastEventType = header.getEventType();
        }
        rowChangedDataList.addAll(changedData);
    }

    @Override
    protected void doFinishHandle() {
        runRowChangeAction();
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
    protected HandleExceptionContext buildHandleExceptionContext(RuntimeException exception) {
        return HandleExceptionContext.build(exception)
                .schema(lastSchema)
                .table(lastTable)
                .eventType(lastEventType)
                .changedData(rowChangedDataList)
                .create();
    }

}
