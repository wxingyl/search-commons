package com.tqmall.search.commons.param;

import com.tqmall.search.commons.condition.ConditionContainer;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by xing on 16/1/24.
 * http调用之外的Rpc调用使用的公共参数类, 比如Dubbo等
 * 该类可以说是一个Build类
 */
public class RpcParam implements Serializable {

    private static final long serialVersionUID = -4627652456092763293L;
    /**
     * 记录系统调用来源
     */
    private String source;
    /**
     * 请求用来表示用户唯一的参数, 这个用户具体业务中区分用户的id, 比如电商的userId, UC的shopId等
     */
    private int uid;

    private ConditionContainer conditionContainer;

    private List<FieldSort> sort;

    /**
     * 如果原先已经添加过SortCondition, 则追加
     */
    public RpcParam sort(String sortStr) {
        List<FieldSort> list = FieldSort.build(sortStr);
        if (list != null) {
            for (FieldSort c : list) {
                sort(c);
            }
        }
        return this;
    }

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

    public void setSource(String source) {
        this.source = source;
    }

    public void setUid(int uid) {
        this.uid = uid;
    }

    public ConditionContainer getConditionContainer() {
        return conditionContainer;
    }

    public List<FieldSort> getSort() {
        return sort;
    }

    public String getSource() {
        return source;
    }

    public int getUid() {
        return uid;
    }
}
