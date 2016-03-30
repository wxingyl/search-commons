package com.tqmall.search.commons.mcache.receive;

import com.tqmall.search.commons.lang.HostInfo;

/**
 * Created by xing on 16/1/1.
 * Http方式封装的salve handle info
 */
public class HttpLocalHandleInfo extends LocalHandleInfo {

    private String registerUrlPath;

    private String unRegisterUrlPath;

    public HttpLocalHandleInfo(RtCacheSlaveHandle handler, HostInfo masterHost) {
        super(handler, masterHost);
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
