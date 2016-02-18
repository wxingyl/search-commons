package com.tqmall.search.commons.lang;

/**
 * Created by xing on 16/1/22.
 * Client 包常量定义
 */
public interface CommonsConst {

    /**
     * 默认的分割符separator
     */
    char SEPARATOR_CHAR = ',';

    String SEPARATOR = "" + SEPARATOR_CHAR;
    /**
     * 默认的区间范围字符
     */
    char RANGE_FILTER_CHAR = '~';

    String RANGE_FILTER = "" + RANGE_FILTER_CHAR;

    /**
     * 赋值操作符号, 指定字段赋值, 比如排序的时候,按照id升序排序: "id:asc"
     */
    char ASSIGNMENT_CHAR = ':';

    String ASSIGNMENT = "" + ASSIGNMENT_CHAR;

}
