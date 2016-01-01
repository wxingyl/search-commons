package com.tqmall.search.common.param;

import com.tqmall.search.common.utils.HostInfo;
import com.tqmall.search.common.utils.HttpUtils;

import java.io.Serializable;

/**
 * Created by xing on 16/1/1.
 * 实现{@link HostInfo}的默认对象
 */
public class HostInfoObj implements HostInfo, Serializable {

    private static final long serialVersionUID = 8506400854259669539L;

    private String ip;

    private int port;

    public void setIp(String ip) {
        this.ip = ip;
    }

    public void setPort(int port) {
        this.port = port;
    }

    @Override
    public String getIp() {
        return null;
    }

    @Override
    public int getPort() {
        return 0;
    }

    @Override
    public String toString() {
        return HttpUtils.hostInfoToString(this);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof HostInfoObj)) return false;

        HostInfoObj that = (HostInfoObj) o;

        if (port != that.port) return false;
        return ip.equals(that.ip);
    }

    @Override
    public int hashCode() {
        int result = ip.hashCode();
        result = 31 * result + port;
        return result;
    }
}
