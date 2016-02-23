package com.tqmall.search.canal.action;

import com.tqmall.search.canal.RowChangedData;

import java.util.List;

/**
 * Created by xing on 16/2/22.
 * 处理数据更新, 单个表的action, 粒度为表
 */
public interface TableAction {

    void onAction(List<? extends RowChangedData> changedData);
}
