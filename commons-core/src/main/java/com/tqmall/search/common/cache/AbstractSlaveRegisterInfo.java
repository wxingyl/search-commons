package com.tqmall.search.common.cache;

/**
 * Created by xing on 15/12/25.
 * slave cache 注册抽象实例
 */
public abstract class AbstractSlaveRegisterInfo {

    private String slaveHost;

    public AbstractSlaveRegisterInfo(String slaveHost) {
        this.slaveHost = slaveHost;
    }

    public String getSlaveHost() {
        return slaveHost;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof AbstractSlaveRegisterInfo)) return false;

        AbstractSlaveRegisterInfo that = (AbstractSlaveRegisterInfo) o;

        return slaveHost.equals(that.slaveHost);

    }

    @Override
    public int hashCode() {
        return slaveHost.hashCode();
    }
}
