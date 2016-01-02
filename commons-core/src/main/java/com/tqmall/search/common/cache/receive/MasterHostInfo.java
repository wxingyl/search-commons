package com.tqmall.search.common.cache.receive;

import com.tqmall.search.common.utils.HostInfo;
import com.tqmall.search.common.utils.HttpUtils;

/**
 * Created by xing on 16/1/2.
 * 本地机器注册信息
 */
public abstract class MasterHostInfo implements HostInfo {

    private String ip;

    private int port;
    /**
     * 注册是否成功
     */
    private volatile boolean registerSucceed;

    public MasterHostInfo(HostInfo masterHost) {
        this(masterHost.getIp(), masterHost.getPort());
    }

    public MasterHostInfo(String ip, int port) {
        this.ip = ip;
        this.port = port;
    }

    public boolean isRegisterSucceed() {
        return registerSucceed;
    }

    public void registerSucceed() {
        if (registerSucceed) return;
        registerSucceed = true;
    }

    @Override
    public String getIp() {
        return ip;
    }

    @Override
    public int getPort() {
        return port;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof MasterHostInfo)) return false;

        MasterHostInfo that = (MasterHostInfo) o;

        if (port != that.port) return false;
        return ip.equals(that.ip);

    }

    @Override
    public int hashCode() {
        int result = ip.hashCode();
        result = 31 * result + port;
        return result;
    }

    @Override
    public String toString() {
        return "MasterHostInfo{host=" + HttpUtils.hostInfoToString(this) + ", registerSucceed=" + registerSucceed + '}';
    }
}
