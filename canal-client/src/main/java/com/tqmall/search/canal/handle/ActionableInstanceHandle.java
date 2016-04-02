package com.tqmall.search.canal.handle;

import com.alibaba.otter.canal.client.CanalConnector;
import com.alibaba.otter.canal.protocol.CanalEntry;
import com.tqmall.search.canal.RowChangedData;
import com.tqmall.search.canal.Schema;
import com.tqmall.search.canal.action.ActionFactory;
import com.tqmall.search.canal.action.Actionable;
import com.tqmall.search.canal.action.CurrentHandleTable;
import com.tqmall.search.commons.lang.Function;
import com.tqmall.search.commons.condition.ConditionContainer;
import com.tqmall.search.commons.utils.CommonsUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.SocketAddress;
import java.util.Iterator;
import java.util.List;

/**
 * Created by xing on 16/2/22.
 * 收集了每个schema.table{@link CanalInstanceHandle}
 *
 * @see #actionFactory
 */
public abstract class ActionableInstanceHandle<T extends Actionable> extends AbstractCanalInstanceHandle {

    private static final Logger log = LoggerFactory.getLogger(ActionableInstanceHandle.class);

    /**
     * 异常处理方法, 优先根据该Function处理
     */
    private Function<HandleExceptionContext, Boolean> handleExceptionFunction;
    /**
     * 是否忽略处理异常, 默认忽略
     * 优先处理{@link #handleExceptionFunction}
     */
    private boolean ignoreHandleException = true;

    protected final ActionFactory<T> actionFactory;

    /**
     * 当前这在处理的schema.table
     * 每个canalHandle都是单个线程负责调用, 所以这儿也就不用考虑多线程, 如果以后添加了多线程处理, 引入{@link ThreadLocal}
     *
     * @see #startHandle(CanalEntry.Header)
     */
    protected Schema<T>.Table currentTable;

    protected CanalEntry.EventType currentEventType;

    private boolean userLocalTableFilter = true;

    /**
     * @param address       canal服务器地址
     * @param destination   canal实例名称
     * @param actionFactory table对应action实例
     */
    public ActionableInstanceHandle(SocketAddress address, String destination, ActionFactory<T> actionFactory) {
        super(address, destination);
        this.actionFactory = actionFactory;
    }

    protected abstract HandleExceptionContext buildHandleExceptionContext(RuntimeException exception);

    @Override
    protected void doConnect() {
        canalConnector.connect();
        if (userLocalTableFilter) {
            StringBuilder sb = new StringBuilder();
            for (Schema<T> s : actionFactory) {
                String schemaName = s.getSchemaName();
                for (Schema<T>.Table t : s) {
                    sb.append(schemaName).append('.').append(t.getTableName()).append(',');
                }
            }
            sb.deleteCharAt(sb.length() - 1);
            canalConnector.subscribe(sb.toString());
        } else {
            canalConnector.subscribe();
        }
    }

    /**
     * {@link Schema.Table#columns}有值, 则对于UPDATE操作过滤更改的字段是否包含在{@link Schema.Table#columns}
     * DELETE, INSERT事件执行条件过滤, 对于UPDATE的过滤不在这做, 比较复杂, 由子类自己实现过滤
     *
     * @param rowChange 更改的数据
     * @return 解析结果
     */
    @SuppressWarnings({"rawtypes", "unchecked"})
    @Override
    protected final List<RowChangedData> changedDataParse(CanalEntry.RowChange rowChange) {
        List<RowChangedData> dataList = RowChangedData.build(rowChange, currentTable.getColumns());
        if (CommonsUtils.isEmpty(dataList)) return null;
        ConditionContainer columnCondition;
        if (currentEventType != CanalEntry.EventType.UPDATE
                && (columnCondition = currentTable.getColumnCondition()) != null) {
            //对于INSERT类型的记录更新, 如果条件判断没有通过, 可以认为该更新事件没有发生~~~~
            //对于DELETE类型的记录更新, 如果条件判断没有通过, 可以认为该数据删除之前就不关心, 那这次删除我们更不关心了~~~
            Iterator<RowChangedData> it = dataList.iterator();
            while (it.hasNext()) {
                if (!columnCondition.verify(it.next())) {
                    it.remove();
                }
            }
        }
        return dataList;
    }

    @Override
    protected boolean exceptionHandle(RuntimeException exception, boolean inFinishHandle) {
        try (HandleExceptionContext context = buildHandleExceptionContext(exception)) {
            if (handleExceptionFunction != null) {
                Boolean ignore = handleExceptionFunction.apply(context);
                return ignore == null ? false : ignore;
            } else {
                log.error("canal instance: " + instanceName + " handle table data change occurring exception: " + context.getSchema()
                        + '.' + context.getTable() + ", eventType: " + context.getEventType() + ", changedData size: "
                        + context.getChangedData().size() + ", ignoreHandleException: " + ignoreHandleException + ", inFinishHandle: "
                        + inFinishHandle, exception);
                return ignoreHandleException;
            }
        }
    }

    /**
     * 是否忽略处理异常, 默认忽略
     * 优先处理{@link #handleExceptionFunction}
     */
    public void setIgnoreHandleException(boolean ignoreHandleException) {
        this.ignoreHandleException = ignoreHandleException;
    }

    /**
     * 异常处理方法, 优先根据该Function处理
     *
     * @param handleExceptionFunction 该function的返回结果标识是否忽略该异常, 同{@link #ignoreHandleException}
     * @see #ignoreHandleException
     */
    public void setExceptionHandleFunction(Function<HandleExceptionContext, Boolean> handleExceptionFunction) {
        this.handleExceptionFunction = handleExceptionFunction;
    }

    /**
     * {@link #canalConnector}连接时, 需要执行订阅{@link CanalConnector#subscribe()} / {@link CanalConnector#subscribe(String)}
     * 该变量标识是否使用本地, 即在{@link #actionFactory}中注册的schema, table
     * 如果为true, 订阅时生成filter, 提交直接替换canal server服务端配置的filter信息
     * 如果为false, 以canal server服务端配置的filter信息为准
     * 默认为true, 使用本地的filter配置
     */
    public void setUserLocalTableFilter(boolean userLocalTableFilter) {
        this.userLocalTableFilter = userLocalTableFilter;
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    @Override
    public boolean startHandle(CanalEntry.Header header) {
        currentEventType = header.getEventType();
        currentTable = actionFactory.getTable(header.getSchemaName(), header.getTableName());
        if (currentTable == null) return false;
        //排除事件类型过滤, 对于UPDATE类型, 如果存在条件判断, 在这儿没有办法执行排除
        if ((RowChangedData.getEventTypeFlag(currentEventType) & currentTable.getForbidEventType()) != 0
                && (currentEventType != CanalEntry.EventType.UPDATE || currentTable.getColumnCondition() == null)) {
            return false;
        }
        T action = currentTable.getAction();
        if (action instanceof CurrentHandleTable) {
            ((CurrentHandleTable<T>) action).setCurrentTable(currentTable);
        }
        return true;
    }
}
