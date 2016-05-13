package com.tqmall.search.canal.handle;

import com.alibaba.otter.canal.client.CanalConnector;
import com.alibaba.otter.canal.protocol.CanalEntry;
import com.tqmall.search.canal.RowChangedData;
import com.tqmall.search.canal.Schema;
import com.tqmall.search.canal.action.ActionFactory;
import com.tqmall.search.canal.action.EventTypeAction;
import com.tqmall.search.commons.lang.Function;
import com.tqmall.search.commons.condition.ConditionContainer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
 * @see #runLastRowChangeAction()
 * @see EventTypeAction
 */
public class EventTypeSectionHandle extends ActionableInstanceHandle<EventTypeAction> {

    private static final Logger log = LoggerFactory.getLogger(EventTypeSectionHandle.class);
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
     * @param connectorFactory {@link CanalConnector}构造器
     * @param destination      canal实例名称
     */
    public EventTypeSectionHandle(String destination, ConnectorFactory connectorFactory, ActionFactory<EventTypeAction> schemaTables) {
        super(destination, connectorFactory, schemaTables);
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    private void runLastEventTypeOfAction(int eventType, List<? extends RowChangedData> dataList) {
        if (log.isDebugEnabled()) {
            log.debug("canal instance: " + instanceName + " need handle data size: " + dataList.size() + ", eventType: " + eventType
                    + " table: " + lastTable);
        }
        if (eventType == CanalEntry.EventType.UPDATE_VALUE) {
            lastTable.getAction().onUpdateAction(Collections.unmodifiableList((List<RowChangedData.Update>) dataList));
        } else if (eventType == CanalEntry.EventType.INSERT_VALUE) {
            lastTable.getAction().onInsertAction(Collections.unmodifiableList((List<RowChangedData.Insert>) dataList));
        } else {
            lastTable.getAction().onDeleteAction(Collections.unmodifiableList((List<RowChangedData.Delete>) dataList));
        }
        //这儿主动调用clear, 数据无效掉, 避免调用Action时保留引用导致无法回收
        for (RowChangedData data : dataList) {
            data.close();
        }
        //这儿清楚掉
        dataList.clear();
    }

    /**
     * UPDATE 事件, 执行条件过滤, 对于多条更新记录, 由于条件过滤, UPDATE事件可能想DELETE, INSERT转换, 这样将本来只需要一次调用
     * {@link EventTypeAction#onUpdateAction(List)}, 由于{@link RowChangedData.Update}转换, 分隔成多个List, 调用多次各自事
     * 件处理方法
     */
    private void runLastRowChangeAction() {
        if (rowChangedDataList.isEmpty()) return;
        ConditionContainer columnCondition;
        if (lastEventType == CanalEntry.EventType.UPDATE && (columnCondition = lastTable.getColumnCondition()) != null) {
            ListIterator<RowChangedData> it = rowChangedDataList.listIterator();
            final Function<String, String> beforeFunction = UpdateDataFunction.before();
            final Function<String, String> afterFunction = UpdateDataFunction.after();
            final boolean insertForbid = (lastTable.getForbidEventType() & RowChangedData.INSERT_TYPE_FLAG) != 0;
            final boolean deleteForbid = (lastTable.getForbidEventType() & RowChangedData.DELETE_TYPE_FLAG) != 0;
            final boolean updateForbid = (lastTable.getForbidEventType() & RowChangedData.UPDATE_TYPE_FLAG) != 0;
            try {
                int lastType = -1, i = 0;
                while (it.hasNext()) {
                    RowChangedData.Update update = (RowChangedData.Update) it.next();
                    UpdateDataFunction.setUpdateData(update);
                    final boolean beforeInvalid = !columnCondition.verify(beforeFunction);
                    final boolean afterInvalid = !columnCondition.verify(afterFunction);
                    final int curType;
                    if ((beforeInvalid && afterInvalid)
                            || (updateForbid && !beforeInvalid && !afterInvalid)
                            || (beforeInvalid && insertForbid)
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
                        runLastEventTypeOfAction(lastType, rowChangedDataList.subList(0, i - 1));
                        //从头开始
                        it = rowChangedDataList.listIterator();
                        i = 1;
                        lastType = curType;
                    }
                }
                if (!rowChangedDataList.isEmpty()) {
                    runLastEventTypeOfAction(lastType, rowChangedDataList);
                }
            } finally {
                //要记得清楚掉, 避免内存泄露
                UpdateDataFunction.setUpdateData(null);
            }
        } else {
            runLastEventTypeOfAction(lastEventType.getNumber(), rowChangedDataList);
        }
    }

    @Override
    protected void doRowChangeHandle(List<RowChangedData> changedData) {
        //尽量集中处理
        if (!currentTable.equals(lastTable) || currentEventType != lastEventType) {
            runLastRowChangeAction();
            lastTable = currentTable;
            lastEventType = currentEventType;
        }
        rowChangedDataList.addAll(changedData);
    }

    /**
     * 如果出现异常, 可以肯定方法{@link #runLastRowChangeAction()}至少调用过一次, 那么对应的, {@link #lastTable},
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
        runLastRowChangeAction();
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
