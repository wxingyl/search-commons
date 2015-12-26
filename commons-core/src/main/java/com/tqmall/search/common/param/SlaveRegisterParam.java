package com.tqmall.search.common.param;

import java.util.List;

/**
 * Created by xing on 15/12/23.
 * Slave机器向master机器注册参数, 通知Master机器对那些cache感兴趣
 */
public abstract class SlaveRegisterParam extends Param {

    private List<String> interestCache;
    /**
     * host必须存在,用来区分服务器
     */
    private String slaveHost;

    public List<String> getInterestCache() {
        return interestCache;
    }

    public String getSlaveHost() {
        return slaveHost;
    }

    public void setInterestCache(List<String> interestCache) {
        this.interestCache = interestCache;
    }

    public void setSlaveHost(String slaveHost) {
        this.slaveHost = slaveHost;
    }

}
