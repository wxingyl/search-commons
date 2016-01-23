package com.tqmall.search.common.param;


import java.util.List;

/**
 * Created by xing on 15/12/22.
 * 给slave机器通知变更的参数
 */
public class NotifyChangeParam extends Param {

    private static final long serialVersionUID = -1280846291575310275L;

    private String cacheKey;
    /**
     * 受影响的keys
     */
    private List<String> keys;

    public String getCacheKey() {
        return cacheKey;
    }

    public List<String> getKeys() {
        return keys;
    }

    public void setCacheKey(String cacheKey) {
        this.cacheKey = cacheKey;
    }

    public void setKeys(List<String> keys) {
        this.keys = keys;
    }
}
