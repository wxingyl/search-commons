package com.tqmall.search.canal.handle;

import com.alibaba.otter.canal.protocol.CanalEntry;
import com.tqmall.search.canal.RowChangedData;

import java.util.Collections;
import java.util.List;

/**
 * Created by xing on 16/2/23.
 * {@link CanalEntry}中记录schema, table等信息的实例
 */
public class InstanceRowChangedData {

    private final String schema;

    private final String table;

    private final CanalEntry.EventType eventType;

    public final List<RowChangedData> changedData;

    public InstanceRowChangedData(String schema, String table, CanalEntry.EventType eventType,
                                  List<? extends RowChangedData> changedData) {
        this.schema = schema;
        this.table = table;
        this.eventType = eventType;
        this.changedData = Collections.unmodifiableList(changedData);
    }

    /**
     * list不可修改
     * @see Collections#unmodifiableList(List)
     */
    public List<RowChangedData> getChangedData() {
        return changedData;
    }

    public CanalEntry.EventType getEventType() {
        return eventType;
    }

    public String getSchema() {
        return schema;
    }

    public String getTable() {
        return table;
    }
}
