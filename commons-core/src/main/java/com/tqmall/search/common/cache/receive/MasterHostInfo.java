package com.tqmall.search.common.cache.receive;

import com.tqmall.search.common.utils.HostInfo;
import com.tqmall.search.common.utils.HttpUtils;

/**
 * Created by xing on 16/1/2.
 * 本地机器注册信息
 */
public abstract class MasterHostInfo implements HostInfo {

    /**
     * 注册状态: 初始状态, 还没有执行注册
     */
    public static final int REGISTER_STATUS_INIT = 0;
    /**
     * 注册状态: 无需注册, masterHost为本地或者过滤掉了
     */
    public static final int REGISTER_STATUS_USELESS = 1;
    /**
     * 注册状态: 注册成功
     */
    public static final int REGISTER_STATUS_SUCCEED = 2;
    /**
     * 注册状态: 注册失败
     */
    public static final int REGISTER_STATUS_FAILED = 3;
    /**
     * 注册状态: 注册中断
     */
    public static final int REGISTER_STATUS_INTERRUPT = 4;
    /**
     * 注册状态: 完成注销
     */
    public static final int REGISTER_STATUS_UNREGISTER = 5;

    private String ip;

    private int port;

    private volatile int registerStatus;

    public MasterHostInfo(HostInfo masterHost) {
        this(masterHost.getIp(), masterHost.getPort());
    }

    public MasterHostInfo(String ip, int port) {
        this.ip = ip;
        this.port = port;
        this.registerStatus = REGISTER_STATUS_INIT;
    }

    /**
     * 是否需要执行远程注册, 目前出了本地master和已经注册成功的无需注册,其他的都需要注册
     * @return true需要执行
     */
    public boolean needDoRegister() {
        return registerStatus != REGISTER_STATUS_SUCCEED && registerStatus != REGISTER_STATUS_USELESS;
    }

    public int getRegisterStatus() {
        return registerStatus;
    }

    public void setRegisterStatus(int registerStatus) {
        if (registerStatus > REGISTER_STATUS_FAILED || registerStatus < REGISTER_STATUS_INIT) {
            throw new IllegalArgumentException("registerStatus: " + registerStatus + "非法");
        }
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
