package com.tqmall.search.commons.mcache.notify;

import com.tqmall.search.commons.lang.HostInfo;
import com.tqmall.search.commons.utils.HttpUtils;

/**
 * Created by xing on 15/12/25.
 * slave 机器注册完master后,master机器将slave机器的信息封装
 * 子类继承, 一定注意{@link #equals(Object)} 和 {@link #equals(Object)}两个发放的重写, 建议不要重写, 该方法的实例作为Map的key使用
 */
public abstract class AbstractSlaveHostInfo implements HostInfo {

    private HostInfo slaveHost;

    public AbstractSlaveHostInfo(HostInfo slaveHost) {
        this.slaveHost = slaveHost;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof AbstractSlaveHostInfo)) return false;

        AbstractSlaveHostInfo that = (AbstractSlaveHostInfo) o;

        return HttpUtils.isEquals(slaveHost, that.slaveHost);
    }

    @Override
    public int getPort() {
        return slaveHost.getPort();
    }

    @Override
    public String getIp() {
        return slaveHost.getIp();
    }

    @Override
    public int hashCode() {
        return slaveHost.hashCode();
    }

    @Override
    public String toString() {
        return HttpUtils.hostInfoToString(slaveHost);
    }
}
