package com.tqmall.search.commons.param;

import com.tqmall.search.commons.lang.HostInfoObj;

import java.util.List;

/**
 * Created by xing on 15/12/23.
 * Local机器向master机器注册参数, 通知Master机器对那些cache感兴趣
 */
public abstract class LocalRegisterParam extends Param {

    private static final long serialVersionUID = 6906155869166498152L;

    private List<String> interestCache;
    /**
     * host必须存在,用来区分服务器
     */
    private HostInfoObj slaveHost;

    public List<String> getInterestCache() {
        return interestCache;
    }

    public void setInterestCache(List<String> interestCache) {
        this.interestCache = interestCache;
    }

    public HostInfoObj getSlaveHost() {
        return slaveHost;
    }

    public void setSlaveHost(HostInfoObj slaveHost) {
        this.slaveHost = slaveHost;
    }

}
