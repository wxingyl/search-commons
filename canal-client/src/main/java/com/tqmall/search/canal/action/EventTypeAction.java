package com.tqmall.search.canal.action;

import com.tqmall.search.canal.RowChangedData;
import com.tqmall.search.canal.handle.EventTypeSectionHandle;

import java.util.List;

/**
 * Created by xing on 16/2/23.
 * 处理数据更新, 单个表某一事件类型的action, 粒度为table中某一事件类型, 最小粒度
 * 对应{@link EventTypeSectionHandle}
 */
public interface EventTypeAction {

    /**
     * table 记录更新的Action处理
     */
    void onUpdateAction(List<RowChangedData.Update> updatedData);

    /**
     * table 记录插入的Action处理
     */
    void onInsertAction(List<RowChangedData.Insert> insertedData);

    /**
     * table 记录删除的Action处理
     */
    void onDeleteAction(List<RowChangedData.Delete> deletedData);
}
