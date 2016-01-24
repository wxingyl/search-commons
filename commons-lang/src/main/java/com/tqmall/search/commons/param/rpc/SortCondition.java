package com.tqmall.search.commons.param.rpc;

import com.tqmall.search.commons.lang.CommonsConst;
import com.tqmall.search.commons.utils.SearchStringUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by xing on 16/1/23.
 * 排序类型封装, 默认降序
 * 排序的条件单独处理, 不应该添加到{@link ConditionContainer}中
 *
 * @see ConditionContainer
 */
public class SortCondition extends Condition {

    private static final long serialVersionUID = 8996525280583145641L;

    /**
     * 表识是否为asc升序排序, 默认false, 即降序
     */
    private final boolean asc;

    public SortCondition(String field) {
        this(field, false);
    }

    public SortCondition(String field, boolean asc) {
        super(field);
        this.asc = asc;
    }

    public boolean isAsc() {
        return asc;
    }

    @Override
    public String toString() {
        return "SortCondition{" + super.toString() + ", asc = " + asc;
    }

    /**
     * 识别Sort排序字符串, 返回的list保留字符串中的顺序
     */
    public static List<SortCondition> build(String sortStr) {
        if (SearchStringUtils.isEmpty(sortStr)) return null;
        String[] array = SearchStringUtils.split(sortStr, CommonsConst.SEPARATOR_CHAR);
        if (array.length == 0) return null;
        List<SortCondition> ret = new ArrayList<>();
        for (String s : array) {
            SortCondition c = parse(s);
            if (c != null) ret.add(c);
        }
        return ret.isEmpty() ? null : ret;
    }

    public static SortCondition parse(String s) {
        s = SearchStringUtils.filterString(s);
        if (s == null) return null;
        String[] array = SearchStringUtils.split(s, CommonsConst.ASSIGNMENT_CHAR);
        if (array.length == 0) return null;
        array = SearchStringUtils.stringArrayTrim(array);
        return new SortCondition(array[0], (array.length > 1 && "asc".equalsIgnoreCase(array[1])));
    }
}
