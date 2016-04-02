package com.tqmall.search.commons.condition;

import com.tqmall.search.commons.lang.Function;
import com.tqmall.search.commons.utils.CommonsUtils;

import java.io.Serializable;
import java.util.*;

/**
 * Created by xing on 16/1/24.
 * 条件集合, 即各个条件的容器, 主要分3大类: {@link #must}, {@link #should}, 校验方法{@link #verify(Function)}
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
        return fields;
    }

    /**
     * 给定值验证, 3类条件都考虑, 这3个大的条件list之间是且的关系
     */
    @Override
    public boolean verify(Function<String, String> values) {
        Objects.requireNonNull(values);
        if (must != null) {
            for (Condition c : must) {
                if (!c.verify(values)) return false;
            }
        }
        if (!CommonsUtils.isEmpty(should)) {
            int matchCount = 0;
            for (Condition c : should) {
                if (c.verify(values) && ++matchCount >= minimumShouldMatch) {
                    break;
                }
            }
            if (matchCount < minimumShouldMatch) return false;
        }
        return true;
    }

    /**
     * only user for test
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ConditionContainer)) return false;

        ConditionContainer container = (ConditionContainer) o;

        if (minimumShouldMatch != container.minimumShouldMatch) return false;
        if (must != null ? !must.equals(container.must) : container.must != null) return false;
        return should != null ? should.equals(container.should) : container.should == null;
    }

    /**
     * only user for test
     */
    @Override
    public int hashCode() {
        int result = must != null ? must.hashCode() : 0;
        result = 31 * result + (should != null ? should.hashCode() : 0);
        result = 31 * result + minimumShouldMatch;
        return result;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        if (!CommonsUtils.isEmpty(must)) {
            sb.append(", must: ").append(must);
        }
        if (!CommonsUtils.isEmpty(should)) {
            sb.append(", should: ").append(should);
            if (minimumShouldMatch > 1) {
                sb.append(", minimumShouldMatch: ").append(minimumShouldMatch);
            }
        }
        if (sb.length() > 0) sb.delete(0, 2);
        return sb.toString();
    }
}
