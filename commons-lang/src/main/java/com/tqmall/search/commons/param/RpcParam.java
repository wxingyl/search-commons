package com.tqmall.search.commons.param;

import com.tqmall.search.commons.condition.ConditionContainer;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created by xing on 16/1/24.
 * http调用之外的Rpc调用使用的公共参数类, 比如Dubbo等
 * 该类可以说是一个Build类
 */
public class RpcParam extends Param {

    private static final long serialVersionUID = -4627652456092763293L;

    private ConditionContainer conditionContainer;

    private List<FieldSort> sorts;

    /**
     * 如果原先已经添加过SortCondition, 则追加
     */
    public RpcParam sort(FieldSort... sorts) {
        if (this.sorts == null) {
            this.sorts = new ArrayList<>();
        }
        Collections.addAll(this.sorts, sorts);
        return this;
    }

    public final void setConditionContainer(ConditionContainer conditionContainer) {
        this.conditionContainer = conditionContainer;
    }

    public final ConditionContainer getConditionContainer() {
        return conditionContainer;
    }

    public final List<FieldSort> getSorts() {
        return sorts;
    }

}
