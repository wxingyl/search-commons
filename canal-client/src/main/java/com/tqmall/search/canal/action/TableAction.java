package com.tqmall.search.canal.action;

import com.tqmall.search.canal.RowChangedData;

import java.util.List;

/**
 * Created by xing on 16/2/23.
 * Table 级别对应的处理Action, 对应{@link com.tqmall.search.canal.handle.TableSectionHandle}
 *
 */
public interface TableAction extends Actionable {

    void onAction(List<? extends RowChangedData> changedData);
}
