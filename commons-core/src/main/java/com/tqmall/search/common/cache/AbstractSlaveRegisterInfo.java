package com.tqmall.search.common.cache;

import com.tqmall.search.common.utils.HostInfo;
import com.tqmall.search.common.utils.HttpUtils;

/**
 * Created by xing on 15/12/25.
 * slave cache 注册抽象实例
 */
public abstract class AbstractSlaveRegisterInfo {

    private HostInfo slaveHost;

    public AbstractSlaveRegisterInfo(HostInfo slaveHost) {
        this.slaveHost = slaveHost;
    }

    public HostInfo getSlaveHost() {
        return slaveHost;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof AbstractSlaveRegisterInfo)) return false;

        AbstractSlaveRegisterInfo that = (AbstractSlaveRegisterInfo) o;

        return HttpUtils.isEquals(slaveHost, that.slaveHost);

    }

    @Override
    public int hashCode() {
        return slaveHost.hashCode();
    }
}
