package com.tqmall.search.commons.param.rpc;

import com.tqmall.search.commons.param.Param;

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
    private final String source;
    /**
     * 请求用来表示用户唯一的参数, 这个用户具体业务中区分用户的id, 比如电商的userId, UC的shopId等
     */
    private final int uid;

    private ConditionContainer must;

    private ConditionContainer should;

    /**
     * {@link #should} 的最小的匹配条件数目, 当然{@link #should}有值才会有效
     */
    private int minimumShouldMatch = 1;

    private ConditionContainer mustNot;

    private List<FieldSort> sort;

    public RpcParam(String source) {
        this(source, 0);
    }

    public RpcParam(Param param) {
        this(param.getSource(), param.getUid());
    }

    public RpcParam(String source, int uid) {
        this.source = source;
        this.uid = uid;
    }

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

    public RpcParam must(Condition condition) {
        if (must == null) {
            must = new ConditionContainer();
        }
        must.addCondition(condition);
        return this;
    }

    public RpcParam should(Condition condition) {
        if (should == null) {
            should = new ConditionContainer();
        }
        should.addCondition(condition);
        return this;
    }

    public RpcParam mustNot(Condition condition) {
        if (mustNot == null) {
            mustNot = new ConditionContainer();
        }
        mustNot.addCondition(condition);
        return this;
    }

    public void setMinimumShouldMatch(int minimumShouldMatch) {
        this.minimumShouldMatch = minimumShouldMatch;
    }

    public int getMinimumShouldMatch() {
        return minimumShouldMatch;
    }

    public ConditionContainer getMust() {
        return must;
    }

    public ConditionContainer getMustNot() {
        return mustNot;
    }

    public ConditionContainer getShould() {
        return should;
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
