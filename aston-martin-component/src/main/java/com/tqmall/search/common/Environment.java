package com.tqmall.search.common;

import com.tqmall.search.common.utils.HostInfo;
import com.tqmall.search.common.utils.HttpUtils;
import com.tqmall.search.common.utils.SearchStringUtils;
import com.tqmall.search.common.utils.StrValueConverts;
import org.apache.commons.lang3.StringUtils;

import java.util.Map;
import java.util.Set;

/**
 * Created by xing on 15/12/28.
 * 环境类型定义
 */
public enum Environment implements HostInfo {
    //电商的商品环境
    GOODS,
    //电商的订单环境
    ORDER,
    //云修搜索环境
    LEGEND,
    //uc, crm和erp, 统称为SAINT的搜索环境
    SAINT;

    //对应环境的master ip
    private String masterIp;
    //对应环境的master port
    private int masterPort;
    //本机是否支持该环境
    private boolean localSupport = false;

    /**
     * 注意: 调用该函数确保初始化完成, 即{@link #initializeStatus} == true才有效
     */
    @Override
    public String getIp() {
        return masterIp;
    }

    /**
     * 注意: 调用该函数确保初始化完成, 即{@link #initializeStatus} == true才有效
     */
    @Override
    public int getPort() {
        return masterPort;
    }



    /**
     * 注意: 调用该函数确保初始化完成, 即{@link #initializeStatus} == true才有效
     */
    public boolean localSupport() {
        return localSupport;
    }

    /**
     * 内部初始化{@link #masterIp}和{@link #masterPort}
     *
     * @param host 配置文件中的host
     */
    private void initHost(String host) {
        String[] hostArray = SearchStringUtils.stringArrayTrim(StringUtils.split(host, ':'));
        if (hostArray.length == 1) {
            masterIp = LOCAL_HOST.ip;
            masterPort = StrValueConverts.intConvert(hostArray[0]);
        } else if (hostArray.length == 2) {
            String ip = hostArray[0].toLowerCase();
            masterIp = (ip.contains("127.0.0.1") || ip.contains("localhost")) ? LOCAL_HOST.ip : hostArray[0];
            masterPort = StrValueConverts.intConvert(hostArray[1]);
        } else {
            throw new IllegalArgumentException("allMasterHost中" + host + "的ip:port值不符合ip:port格式");
        }
        if (masterPort <= 0) {
            throw new IllegalArgumentException("allMasterHost中" + host + "的port值不正确");
        }
    }


    /**
     * 调用init方法状态, 0: 未执行或者原先执行失败, 1: 正在执行, 2: 初始化完成
     */
    private static volatile int initializeStatus = 0;

    public static void init(Set<Environment> currentEnvSet, Map<Environment, String> envHostInfoMap) {
        if (initializeStatus != 0) return;
        initializeStatus = 1;
        try {
            for (Environment env : currentEnvSet) {
                env.localSupport = true;
            }
            //初始化各个环境的masterHost
            for (Environment e : values()) {
                String str = envHostInfoMap.get(e);
                if (str == null) {
                    if (e.localSupport) {
                        throw new IllegalArgumentException("环境" + e + "配置的本地支持,但是没有找对对应的masterHost, 配置存在错误");
                    } else {
                        continue;
                    }
                }
                e.initHost(str);
                //如果符合, 主动添加本地
                if (HttpUtils.isEquals(e, LOCAL_HOST) && !e.localSupport) {
                    e.localSupport = true;
                }
            }
            initializeStatus = 2;
        } catch (Throwable e) {
            initializeStatus = 0;
            throw e;
        }
    }

    /**
     * @return 初始化执行完成, 并且OK?
     */
    public static boolean initialized() {
        return initializeStatus == 2;
    }

    /**
     * 判断给定的环境目前是否是master状态, 目前每种类型, master机器只有1台
     *
     * @param env 给定的环境
     */
    public static boolean isMaster(Environment env) {
        if (!env.localSupport) return false;
        else if (HttpUtils.isEquals(env, LOCAL_HOST)) return true;
        else return false;
    }

    public static final LocalHost LOCAL_HOST = new LocalHost();

    public final static class LocalHost implements HostInfo {

        private final String ip = HttpUtils.LOCAL_IP;
        /**
         * 通过-Dtq.local.port=XXX指定端口, 如果没有指定, 则默认8080
         */
        private final int port;

        LocalHost() {
            int localPort = StrValueConverts.intConvert(System.getProperty("tq.local.port"));
            port = localPort == 0 ? 8080 : localPort;
        }

        @Override
        public String getIp() {
            return ip;
        }

        @Override
        public int getPort() {
            return port;
        }
    }

}
