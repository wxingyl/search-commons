package com.tqmall.search.common.cache.receive;

import com.tqmall.search.common.utils.HostInfo;

/**
 * Created by xing on 16/1/2.
 * http的实现
 */
public class HttpMasterHostInfo extends MasterHostInfo {

    private String registerUrlPath;

    private String unRegisterUrlPath;

    public HttpMasterHostInfo(HostInfo masterHost) {
        super(masterHost);
    }

    public String getRegisterUrlPath() {
        return registerUrlPath;
    }

    public void setRegisterUrlPath(String registerUrlPath) {
        this.registerUrlPath = registerUrlPath;
    }

    public String getUnRegisterUrlPath() {
        return unRegisterUrlPath;
    }

    public void setUnRegisterUrlPath(String unRegisterUrlPath) {
        this.unRegisterUrlPath = unRegisterUrlPath;
    }
}
