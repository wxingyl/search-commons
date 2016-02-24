package com.tqmall.search.canal.handle;

import com.alibaba.otter.canal.protocol.CanalEntry;
import com.tqmall.search.canal.RowChangedData;
import com.tqmall.search.commons.utils.CommonsUtils;

import java.util.Collections;
import java.util.List;

/**
 * Created by xing on 16/2/23.
 * canal处理事件时出现异常, 该类封装当时异常的上下文
 *
 * @see CanalInstanceHandle#rowChangeHandle(CanalEntry.Header, CanalEntry.RowChange)
 * @see AbstractCanalInstanceHandle#doRowChangeHandle(CanalEntry.Header, List)
 */
public class HandleExceptionContext {

    private final RuntimeException exception;

    private final String schema;

    private final String table;

    private final CanalEntry.EventType eventType;

    /**
     * 不可修改的list
     *
     * @see java.util.Collections#unmodifiableList(List)
     */
    private List<RowChangedData> changedData;

    public HandleExceptionContext(RuntimeException exception, String schema, String table,
                                  CanalEntry.EventType eventType, List<RowChangedData> changedData) {
        this.exception = exception;
        this.schema = schema;
        this.table = table;
        this.eventType = eventType;
        this.changedData = CommonsUtils.isEmpty(changedData) ? Collections.<RowChangedData>emptyList()
                : Collections.unmodifiableList(changedData);
    }

    /**
     * 返回{@link Collections#unmodifiableList(List)}, 执行add操作直接抛异常
     *
     * @return 不可修改的list 并且不可能为null
     */
    public List<RowChangedData> getChangedData() {
        return changedData;
    }

    public CanalEntry.EventType getEventType() {
        return eventType;
    }

    public RuntimeException getException() {
        return exception;
    }

    public String getSchema() {
        return schema;
    }

    public String getTable() {
        return table;
    }

    public static Builder build(RuntimeException e) {
        return new Builder(e);
    }

    public static class Builder {

        private RuntimeException exception;

        private String schema, table;

        private CanalEntry.EventType eventType;

        private List<RowChangedData> changedData;

        public Builder(RuntimeException exception) {
            this.exception = exception;
        }

        public Builder schema(String schema) {
            this.schema = schema;
            return this;
        }

        public Builder table(String table) {
            this.table = table;
            return this;
        }

        public Builder eventType(CanalEntry.EventType eventType) {
            this.eventType = eventType;
            return this;
        }

        public Builder changedData(List<RowChangedData> changedData) {
            this.changedData = changedData;
            return this;
        }

        public HandleExceptionContext create() {
            return new HandleExceptionContext(exception, schema, table, eventType, changedData);
        }

    }
}
