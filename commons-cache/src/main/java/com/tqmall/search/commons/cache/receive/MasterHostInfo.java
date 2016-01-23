package com.tqmall.search.commons.cache.receive;

import com.tqmall.search.commons.lang.HostInfo;
import com.tqmall.search.commons.utils.HttpUtils;

/**
 * Created by xing on 16/1/2.
 * 本地机器注册信息
 */
public abstract class MasterHostInfo implements HostInfo {

    private String ip;

    private int port;

    private volatile RegisterStatus registerStatus;

    public MasterHostInfo(HostInfo masterHost) {
        this(masterHost.getIp(), masterHost.getPort());
    }

    public MasterHostInfo(String ip, int port) {
        this.ip = ip;
        this.port = port;
        this.registerStatus = RegisterStatus.INIT;
    }

    /**
     * 是否需要执行远程注册, 目前出了本地master和已经注册成功的无需注册,其他的都需要注册
     * @return true需要执行
     */
    public boolean needDoRegister() {
        return registerStatus != RegisterStatus.SUCCEED && registerStatus != RegisterStatus.USELESS;
    }

    public RegisterStatus getRegisterStatus() {
        return registerStatus;
    }

    public void setRegisterStatus(RegisterStatus registerStatus) {
        this.registerStatus = registerStatus;
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
        return "MasterHostInfo{host=" + HttpUtils.hostInfoToString(this) + ", registerStatus=" + registerStatus + '}';
    }
}
