package com.tqmall.search.commons.param.condition;

import java.io.Serializable;
import java.util.Collection;

/**
 * Created by xing on 16/1/24.
 * 条件集合, 即各个条件的容器
 * 注意: 获取条件的接口返回的List都是unmodifiableList, 不可修改的
 *
 * @see InCondition
 * @see GtCondition
 * @see RangeCondition
 * @see EqualCondition
 */
public abstract class ConditionContainer implements Serializable {

    private static final long serialVersionUID = 8536199694404404344L;
    /**
     * 且关系条件集合
     */
    protected Collection<Condition> must;
    /**
     * 或关系条件集合
     */
    protected Collection<Condition> should;
    /**
     * 非关系条件集合
     */
    protected Collection<Condition> mustNot;

    /**
     * {@link #should} 的最小的匹配条件数目, 当然{@link #should}有值才会有效
     */
    protected int minimumShouldMatch = 1;
    /**
     * @return unmodifiableList
     */
    public abstract Collection<Condition> getMust();

    /**
     * @return unmodifiableList
     */
    public abstract Collection<Condition> getShould();

    /**
     * @return unmodifiableList
     */
    public abstract Collection<Condition> getMustNot();

    public int getMinimumShouldMatch() {
        return minimumShouldMatch;
    }
}
