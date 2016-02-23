package com.tqmall.search.canal.action;

import com.tqmall.search.canal.RowChangedData;

import java.util.List;

/**
 * Created by xing on 16/2/22.
 * 处理数据更新, 实例级别的响应, 粒度为实例, 此为最大粒度
 */
public interface InstanceAction {

    void onAction(String schema, String table, List<? extends RowChangedData> changedData);
}
