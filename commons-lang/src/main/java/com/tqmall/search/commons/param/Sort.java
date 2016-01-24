package com.tqmall.search.commons.param;

import com.tqmall.search.commons.lang.CommonsConst;
import com.tqmall.search.commons.utils.SearchStringUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by xing on 16/1/23.
 * 排序类型封装, 默认降序
 */
public class Sort extends Condition {

    private static final long serialVersionUID = 8996525280583145641L;

    /**
     * 表识是否为asc升序排序, 默认false, 即降序
     */
    private boolean asc;

    public Sort(String field) {
        this(field, false);
    }

    public Sort(String field, boolean asc) {
        super(field);
        this.asc = asc;
    }

    public boolean isAsc() {
        return asc;
    }

    /**
     * 识别Sort排序字符串, 返回的list保留字符串中的顺序
     */
    public static List<Sort> build(String sortStr) {
        if (SearchStringUtils.isEmpty(sortStr)) return null;
        String[] array = SearchStringUtils.split(sortStr, CommonsConst.SEPARATOR_CHAR);
        if (array.length == 0) return null;
        List<Sort> ret = new ArrayList<>();
        for (String s : array) {
            Sort obj = parse(s);
            if (obj != null) ret.add(obj);
        }
        return ret;
    }

    public static Sort parse(String s) {
        s = SearchStringUtils.filterString(s);
        if (s == null) return null;
        String[] array = SearchStringUtils.split(s, CommonsConst.ASSIGNMENT_CHAR);
        if (array.length == 0) return null;
        array = SearchStringUtils.stringArrayTrim(array);
        return new Sort(array[0], (array.length > 1 && "asc".equalsIgnoreCase(array[1])));
    }
}
