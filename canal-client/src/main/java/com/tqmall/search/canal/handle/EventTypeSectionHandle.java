package com.tqmall.search.canal.handle;

import com.alibaba.otter.canal.protocol.CanalEntry;
import com.tqmall.search.canal.RowChangedData;
import com.tqmall.search.canal.action.EventTypeAction;
import com.tqmall.search.canal.Schema;
import com.tqmall.search.canal.action.ActionFactory;
import com.tqmall.search.canal.TableColumnCondition;
import com.tqmall.search.commons.lang.Function;

import java.net.SocketAddress;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;

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
     * 最近处理的table
     * 只能canal获取数据的线程访问, 线程不安全的
     */
    private Schema<EventTypeAction>.Table lastTable;
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
    public EventTypeSectionHandle(SocketAddress address, String destination, ActionFactory<EventTypeAction> schemaTables) {
        super(address, destination, schemaTables);
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    private void runEventTypeOfAction(int eventType, List<? extends RowChangedData> dataList) {
        if (eventType == CanalEntry.EventType.UPDATE_VALUE) {
            currentTable.getAction().onUpdateAction(Collections.unmodifiableList((List<RowChangedData.Update>) dataList));
        } else if (eventType == CanalEntry.EventType.INSERT_VALUE) {
            currentTable.getAction().onInsertAction(Collections.unmodifiableList((List<RowChangedData.Insert>) dataList));
        } else {
            currentTable.getAction().onDeleteAction(Collections.unmodifiableList((List<RowChangedData.Delete>) dataList));
        }
        //这儿清楚掉
        dataList.clear();
    }

    /**
     * UPDATE 事件, 执行条件过滤, 对于多条更新记录, 由于条件过滤, UPDATE事件可能想DELETE, INSERT转换, 这样将本来只需要一次调用
     * {@link EventTypeAction#onUpdateAction(List)}, 由于{@link RowChangedData.Update}转换, 分隔成多个List, 调用多次各自事
     * 件处理方法
     */
    private void runRowChangeAction() {
        if (rowChangedDataList.isEmpty()) return;
        TableColumnCondition columnCondition;
        if (lastEventType == CanalEntry.EventType.UPDATE && (columnCondition = currentTable.getColumnCondition()) != null) {
            ListIterator<RowChangedData> it = rowChangedDataList.listIterator();
            Function<String, String> beforeFunction = UpdateDataFunction.before();
            Function<String, String> afterFunction = UpdateDataFunction.after();
            boolean insertForbid = (currentTable.getForbidEventType() & RowChangedData.INSERT_TYPE_FLAG) != 0;
            boolean deleteForbid = (currentTable.getForbidEventType() & RowChangedData.DELETE_TYPE_FLAG) != 0;
            try {
                int lastType = -1, i = 0;
                while (it.hasNext()) {
                    RowChangedData.Update update = (RowChangedData.Update) it.next();
                    UpdateDataFunction.setUpdateData(update);
                    boolean beforeInvalid = !columnCondition.validation(beforeFunction);
                    boolean afterInvalid = !columnCondition.validation(afterFunction);
                    int curType;
                    if ((beforeInvalid && afterInvalid) || (beforeInvalid && insertForbid)
                            || (afterInvalid && deleteForbid)) {
                        //没有数据, 删除
                        it.remove();
                        continue;
                    } else if (beforeInvalid) {
                        it.set(update.transferToInsert());
                        curType = CanalEntry.EventType.INSERT_VALUE;
                    } else if (afterInvalid) {
                        it.set(update.transferToDelete());
                        curType = CanalEntry.EventType.DELETE_VALUE;
                    } else {
                        curType = CanalEntry.EventType.UPDATE_VALUE;
                    }
                    i++;
                    if (lastType == -1) {
                        lastType = curType;
                    } else if (lastType != curType) {
                        runEventTypeOfAction(lastType, rowChangedDataList.subList(0, i));
                        //从头开始
                        it = rowChangedDataList.listIterator();
                        i = 0;
                        lastType = curType;
                    }
                }
                if (i > 0) {
                    runEventTypeOfAction(lastType, rowChangedDataList);
                }
            } finally {
                //要记得清楚掉, 避免内存泄露
                UpdateDataFunction.setUpdateData(null);
            }
        } else {
            runEventTypeOfAction(currentEventType.getNumber(), rowChangedDataList);
        }
    }

    @Override
    protected void doRowChangeHandle(List<RowChangedData> changedData) {
        //尽量集中处理
        if (!currentTable.equals(lastTable) || currentEventType != lastEventType) {
            runRowChangeAction();
            lastTable = currentTable;
            lastEventType = currentEventType;
        }
        rowChangedDataList.addAll(changedData);
    }

    /**
     * 如果出现异常, 可以肯定方法{@link #runRowChangeAction()}至少调用过一次, 那么对应的, {@link #lastTable},
     * {@link #lastEventType} 需要更新
     *
     * @param exception      具体异常
     * @param inFinishHandle 标识是否在{@link #doFinishHandle()}中产生的异常
     * @return 是否忽略异常
     */
    @Override
    protected boolean exceptionHandle(RuntimeException exception, boolean inFinishHandle) {
        try {
            if (super.exceptionHandle(exception, inFinishHandle)) {
                lastTable = currentTable;
                lastEventType = currentEventType;
                return true;
            } else {
                return false;
            }
        } finally {
            if (!rowChangedDataList.isEmpty()) {
                rowChangedDataList.clear();
            }
        }
    }

    @Override
    protected void doFinishHandle() {
        runRowChangeAction();
    }

    @Override
    protected HandleExceptionContext buildHandleExceptionContext(RuntimeException exception) {
        return HandleExceptionContext.build(exception)
                .schema(lastTable.getSchemaName())
                .table(lastTable.getTableName())
                .eventType(lastEventType)
                .changedData(rowChangedDataList)
                .create();
    }

}
