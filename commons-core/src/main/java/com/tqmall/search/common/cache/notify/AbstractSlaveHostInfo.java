package com.tqmall.search.common.cache.notify;

import com.tqmall.search.common.utils.HostInfo;
import com.tqmall.search.common.utils.HttpUtils;

/**
 * Created by xing on 15/12/25.
 * slave 机器注册完master后,master机器将slave机器的信息封装
 */
public abstract class AbstractSlaveHostInfo {

    private HostInfo slaveHost;

    public AbstractSlaveHostInfo(HostInfo slaveHost) {
        this.slaveHost = slaveHost;
    }

    public HostInfo getSlaveHost() {
        return slaveHost;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof AbstractSlaveHostInfo)) return false;

        AbstractSlaveHostInfo that = (AbstractSlaveHostInfo) o;

        return HttpUtils.isEquals(slaveHost, that.slaveHost);

    }

    @Override
    public int hashCode() {
        return slaveHost.hashCode();
    }
}
