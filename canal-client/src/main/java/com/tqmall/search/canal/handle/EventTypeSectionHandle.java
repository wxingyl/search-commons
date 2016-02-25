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
 * 连续的事件更新, 发现不同schema, table, eventType 则处理掉
 *
 * @see #runRowChangeAction()
 * @see EventTypeAction
 */
public class EventTypeSectionHandle extends ActionableInstanceHandle<EventTypeAction> {
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
                break;
            case INSERT:
                action.onInsertAction(rowChangedDataTransfer(new ArrayList<RowChangedData.Insert>()));
                break;
            default:
                //can not reach here
                throw new UnsupportedOperationException("unsupported eventType: " + lastEventType);
        }
        rowChangedDataList.clear();
    }

    @Override
    protected void doRowChangeHandle(List<RowChangedData> changedData) {
        //尽量集中处理
        if (!currentHandleTable.equals(lastTable) || !currentEventType.equals(lastEventType)
                || !currentHandleSchema.equals(lastSchema)) {
            runRowChangeAction();
            lastSchema = currentHandleSchema;
            lastTable = currentHandleTable;
            lastEventType = currentEventType;
        }
        rowChangedDataList.addAll(changedData);
    }

    /**
     * 如果出现异常, 可以肯定方法{@link #runRowChangeAction()}至少调用过一次, 那么对应的{@link #lastSchema}, {@link #lastTable},
     * {@link #lastEventType} 需要更新
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
            lastEventType = currentEventType;
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
