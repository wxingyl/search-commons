package com.tqmall.search.commons.param;

import com.tqmall.search.commons.condition.ConditionContainer;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by xing on 16/1/24.
 * http调用之外的Rpc调用使用的公共参数类, 比如Dubbo等
 * 该类可以说是一个Build类
 */
public class RpcParam extends Param {

    private static final long serialVersionUID = -4627652456092763293L;

    private ConditionContainer conditionContainer;

    private List<FieldSort> sort;

    /**
     * 如果原先已经添加过SortCondition, 则追加
     */
    public RpcParam sort(FieldSort fieldSort) {
        if (sort == null) {
            sort = new ArrayList<>();
        }
        sort.add(fieldSort);
        return this;
    }

    public void setConditionContainer(ConditionContainer conditionContainer) {
        this.conditionContainer = conditionContainer;
    }

    public ConditionContainer getConditionContainer() {
        return conditionContainer;
    }

    public List<FieldSort> getSort() {
        return sort;
    }

}
