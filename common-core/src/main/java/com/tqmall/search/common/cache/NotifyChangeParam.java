package com.tqmall.search.common.cache;


import com.tqmall.search.common.param.Param;

import java.io.Serializable;
import java.util.List;

/**
 * Created by xing on 15/12/22.
 * 给slave机器通知变更的参数
 */
public class NotifyChangeParam extends Param implements Serializable {

    private static final long serialVersionUID = -1280846291575310275L;
    /**
     * 发送请求的host地址
     */
    private String fromHost;

    private String cacheKey;

    /**
     * 受影响的keys
     */
    private List<String> keys;

    public String getFromHost() {
        return fromHost;
    }

    public String getCacheKey() {
        return cacheKey;
    }

    public List<String> getKeys() {
        return keys;
    }

    public void setFromHost(String fromHost) {
        this.fromHost = fromHost;
    }

    public void setCacheKey(String cacheKey) {
        this.cacheKey = cacheKey;
    }

    public void setKeys(List<String> keys) {
        this.keys = keys;
    }
}
