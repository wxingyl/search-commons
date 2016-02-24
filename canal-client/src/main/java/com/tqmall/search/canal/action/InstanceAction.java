package com.tqmall.search.canal.action;

import com.tqmall.search.canal.handle.InstanceRowChangedData;
import com.tqmall.search.canal.handle.InstanceSectionHandle;

import java.util.List;

/**
 * Created by xing on 16/2/23.
 * canalInstance 级别对应的处理方法, 对应{@link InstanceSectionHandle}
 */
public interface InstanceAction {

    /**
     * 实例名
     */
    String instanceName();

    void onAction(List<? extends InstanceRowChangedData> rowChangedData);
}
