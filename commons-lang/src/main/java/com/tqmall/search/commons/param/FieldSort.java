package com.tqmall.search.commons.param;

import com.tqmall.search.commons.utils.SearchStringUtils;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by xing on 16/1/23.
 * 排序类型封装, 默认降序
 */
public class FieldSort implements Serializable {

    private static final long serialVersionUID = -104947665314235229L;

    private final String field;
    /**
     * 表识是否为asc升序排序, 默认false, 即降序
     */
    private final boolean asc;

    public FieldSort(String field) {
        this(field, false);
    }

    public FieldSort(String field, boolean asc) {
        this.field = field;
        this.asc = asc;
    }

    public String getField() {
        return field;
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
    public static List<FieldSort> build(String sortStr) {
        if (SearchStringUtils.isEmpty(sortStr)) return null;
        String[] array = SearchStringUtils.split(sortStr, Param.SEPARATOR_CHAR);
        if (array.length == 0) return null;
        List<FieldSort> ret = new ArrayList<>();
        for (String s : array) {
            FieldSort c = parse(s);
            if (c != null) ret.add(c);
        }
        return ret.isEmpty() ? null : ret;
    }

    public static FieldSort parse(String s) {
        s = SearchStringUtils.filterString(s);
        if (s == null) return null;
        String[] array = SearchStringUtils.splitTrim(s, Param.ASSIGNMENT_CHAR);
        if (array.length == 0) return null;
        return new FieldSort(array[0], (array.length > 1 && "asc".equalsIgnoreCase(array[1])));
    }
}
