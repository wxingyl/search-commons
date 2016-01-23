package com.tqmall.search.commons.lang;

/**
 * Created by xing on 15/12/31.
 * 获取master应用的host信息, 分开定义原因是省去校验ip:port格式
 */
public interface HostInfo {

    String getIp();

    int getPort();

}
