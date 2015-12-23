package com.tqmall.search.common.cache;

import com.tqmall.search.common.param.Param;

import java.util.List;

/**
 * Created by xing on 15/12/23.
 * Slave机器向master机器注册参数, 高速Master机器对那些cache感兴趣
 *
 */
public class SlaveRegisterParam extends Param {

    private String slaveHost;

    private List<String> interestCache;

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
