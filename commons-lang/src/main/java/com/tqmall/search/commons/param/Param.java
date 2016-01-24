package com.tqmall.search.commons.param;

import java.io.Serializable;

/**
 * Created by xing on 15/12/5.
 * 参数抽象父类
 */
public abstract class Param implements Serializable {

    private static final long serialVersionUID = 1L;
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
