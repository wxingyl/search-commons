package com.tqmall.search.canal.handle;

import com.alibaba.otter.canal.client.CanalConnector;
import com.alibaba.otter.canal.protocol.CanalEntry;
import com.google.common.base.Function;
import com.tqmall.search.canal.RowChangedData;
import com.tqmall.search.canal.action.SchemaTables;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.SocketAddress;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

/**
 * Created by xing on 16/2/22.
 * 收集了每个schema.table{@link CanalInstanceHandle}
 */
public abstract class ActionInstanceHandle<V> extends AbstractCanalInstanceHandle {

    private static final Logger log = LoggerFactory.getLogger(ActionInstanceHandle.class);

    /**
     * 异常处理方法, 优先根据该Function处理
     */
    private Function<HandleExceptionContext, Boolean> handleExceptionFunction;
    /**
     * 是否忽略处理异常, 默认忽略
     * 优先处理{@link #handleExceptionFunction}
     */
    private boolean ignoreHandleException = true;

    protected final SchemaTables<V> schemaTables;

    /**
     * {@link #canalConnector}连接时, 需要执行订阅{@link CanalConnector#subscribe()} / {@link CanalConnector#subscribe(String)}
     * 该变量标识是否使用本地, 即在{@link #schemaTables}中注册的schema, table
     * 如果为true, 订阅时生成filter, 提交直接替换canal server服务端配置的filter信息
     * 如果为false, 以canal server服务端配置的filter信息为准
     * 默认为true, 使用本地的filter配置
     */
    private boolean userLocalTableFilter = true;

    /**
     * @param address      canal服务器地址
     * @param destination  canal实例名称
     * @param schemaTables table对应action实例
     */
    public ActionInstanceHandle(SocketAddress address, String destination, SchemaTables<V> schemaTables) {
        super(address, destination);
        this.schemaTables = schemaTables;
    }

    protected abstract HandleExceptionContext buildHandleExceptionContext(RuntimeException exception);

    @Override
    protected void doConnect() {
        canalConnector.connect();
        if (userLocalTableFilter) {
            StringBuilder sb = new StringBuilder();
            for (SchemaTables.Schema<V> s : schemaTables) {
                String schemaName = s.getSchemaName();
                for (SchemaTables.Table t : s) {
                    sb.append(schemaName).append('.').append(t.getTableName()).append(',');
                }
            }
            sb.deleteCharAt(sb.length() - 1);
            canalConnector.subscribe(sb.toString());
        } else {
            canalConnector.subscribe();
        }
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    @Override
    protected List<? extends RowChangedData> changedDataParse(CanalEntry.RowChange rowChange) {
        List<? extends RowChangedData> dataList = super.changedDataParse(rowChange);
        Set<String> columns;
        if (currentEventType == CanalEntry.EventType.UPDATE
                && (columns = schemaTables.getTable(currentHandleSchema, currentHandleTable).getColumns()) != null) {
            Iterator<RowChangedData.Update> it = ((List<RowChangedData.Update>) dataList).iterator();
            while (it.hasNext()) {
                RowChangedData.Update update = it.next();
                boolean canRemove = true;
                for (String c : columns) {
                    if (update.isChanged(c)) {
                        canRemove = false;
                        break;
                    }
                }
                if (canRemove) it.remove();
            }
        }
        return dataList;
    }

    @Override
    protected boolean exceptionHandle(RuntimeException exception, boolean inFinishHandle) {
        HandleExceptionContext context = buildHandleExceptionContext(exception);
        if (handleExceptionFunction != null) {
            Boolean ignore = handleExceptionFunction.apply(context);
            return ignore == null ? false : ignore;
        } else {
            log.error("canal " + instanceName + " handle table data change occurring exception: " + context.getSchema()
                    + '.' + context.getTable() + ", eventType: " + context.getEventType() + ", changedData size: "
                    + context.getChangedData().size() + ", ignoreHandleException: " + ignoreHandleException + ", inFinishHandle: "
                    + inFinishHandle, exception);
            return ignoreHandleException;
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
    public void setHandleExceptionFunction(Function<HandleExceptionContext, Boolean> handleExceptionFunction) {
        this.handleExceptionFunction = handleExceptionFunction;
    }

    public void setUserLocalTableFilter(boolean userLocalTableFilter) {
        this.userLocalTableFilter = userLocalTableFilter;
    }

    @Override
    public boolean startHandle(CanalEntry.Header header) {
        return super.startHandle(header) && schemaTables.getTable(currentHandleSchema, currentHandleTable) != null;
    }
}
