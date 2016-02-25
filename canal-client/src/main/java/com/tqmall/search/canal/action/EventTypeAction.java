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
     * @param updatedData 入参list不可以更改, 不支持{@link List#add(Object)}, {@link List#remove(int)}等修改操作
     * @see java.util.Collections#unmodifiableList(List)
     */
    void onUpdateAction(List<RowChangedData.Update> updatedData);

    /**
     * table 记录插入的Action处理
     * @param insertedData 入参list不可以更改, 不支持{@link List#add(Object)}, {@link List#remove(int)}等修改操作
     * @see java.util.Collections#unmodifiableList(List)
     */
    void onInsertAction(List<RowChangedData.Insert> insertedData);

    /**
     * table 记录删除的Action处理
     * @param deletedData 入参list不可以更改, 不支持{@link List#add(Object)}, {@link List#remove(int)}等修改操作
     * @see java.util.Collections#unmodifiableList(List)
     */
    void onDeleteAction(List<RowChangedData.Delete> deletedData);
}
