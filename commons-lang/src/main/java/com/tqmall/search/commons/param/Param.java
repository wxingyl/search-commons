package com.tqmall.search.commons.param;

import java.io.Serializable;

/**
 * Created by xing on 15/12/5.
 * 参数抽象父类
 */
public abstract class Param implements Serializable {

    private static final long serialVersionUID = 1L;
    /**
     * 默认的分割符separator
     */
    public static final char SEPARATOR_CHAR = ',';
    /**
     * 默认的区间范围字符
     */
    public static final char RANGE_FILTER_CHAR = '~';

    /**
     * 赋值操作符号, 指定字段赋值, 比如排序的时候,按照id升序排序: "id:asc"
     */
    public static final char ASSIGNMENT_CHAR = ':';

    /**
     * 记录系统调用来源
     */
    private String source;
    /**
     * 请求用来表示用户唯一的参数, 这个用户具体业务中区分用户的id, 比如电商的userId, UC的shopId等
     */
    private int uid;

    public String getSource() {
        return source;
    }

    public int getUid() {
        return uid;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public void setUid(Integer uid) {
        this.uid = uid;
    }

}
