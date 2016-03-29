package com.tqmall.search.commons.param.condition;

import com.tqmall.search.commons.lang.Function;
import com.tqmall.search.commons.utils.CommonsUtils;

import java.io.Serializable;
import java.util.*;

/**
 * Created by xing on 16/1/24.
 * 条件集合, 即各个条件的容器, 主要分3大类: {@link #must}, {@link #should}, {@link #mustNot}, 校验方法{@link #validation(Function)}
 * 3类条件都考虑, 这3个大的条件list之间是且的关系
 * <p/>
 * 注意: 获取条件的接口返回的List都是unmodifiableList, 不可修改的
 *
 * @see InCondition
 * @see RangeCondition
 * @see EqualCondition
 */
public abstract class ConditionContainer implements Condition, Serializable {

    private static final long serialVersionUID = 8536199694404404344L;
    /**
     * 且关系条件集合
     */
    protected List<Condition> must;
    /**
     * 或关系条件集合, 通过{@link #minimumShouldMatch}控制至少匹配条件数, 默认至少匹配1个条件
     * 如果该集合为空, 即{@link CommonsUtils#isEmpty(Collection)}为true, 则不考虑should相关条件
     */
    protected List<Condition> should;

    /**
     * {@link #should} 的最小的匹配条件数目, 当然{@link #should}有值才会有效
     */
    protected int minimumShouldMatch = 1;
    /**
     * 非关系条件集合
     */
    protected List<Condition> mustNot;

    @Override
    public Set<String> fields() {
        Set<String> fields = new HashSet<>();
        if (!CommonsUtils.isEmpty(must)) {
            for (Condition c : must) {
                fields.addAll(c.fields());
            }
        }
        if (!CommonsUtils.isEmpty(should)) {
            for (Condition c : should) {
                fields.addAll(c.fields());
            }
        }
        if (!CommonsUtils.isEmpty(mustNot)) {
            for (Condition c : mustNot) {
                fields.addAll(c.fields());
            }
        }
        return fields;
    }

    /**
     * 给定值验证, 3类条件都考虑, 这3个大的条件list之间是且的关系
     */
    @Override
    public boolean validation(Function<String, String> values) {
        Objects.requireNonNull(values);
        if (must != null) {
            for (Condition c : must) {
                if (!c.validation(values)) return false;
            }
        }
        //mustNot放在第二, 毕竟是且关系, 不匹配直接返回, 避免后面should的或查询比较
        if (mustNot != null) {
            for (Condition c : mustNot) {
                if (c.validation(values)) return false;
            }
        }
        if (!CommonsUtils.isEmpty(should)) {
            int matchCount = 0;
            for (Condition c : should) {
                if (c.validation(values) && ++matchCount >= minimumShouldMatch) {
                    break;
                }
            }
            if (matchCount < minimumShouldMatch) return false;
        }
        return true;
    }

}
