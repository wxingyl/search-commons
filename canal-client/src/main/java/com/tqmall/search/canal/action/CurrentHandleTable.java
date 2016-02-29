package com.tqmall.search.canal.action;

import com.tqmall.search.canal.Schema;

/**
 * Created by xing on 16/2/27.
 * 可以设定当前正在处理的{@link Schema.Table}接口定义, 配合{@link TableAction}, {@link EventTypeAction}一起使用
 */
public interface CurrentHandleTable<T extends Actionable> {

    /**
     * 设置当前处理的{@link Schema.Table}对象
     */
    void setCurrentTable(Schema<T>.Table table);

    /**
     * 获取当前正在处理的{@link Schema.Table}对象
     */
    Schema<T>.Table getCurrentTable();
}
