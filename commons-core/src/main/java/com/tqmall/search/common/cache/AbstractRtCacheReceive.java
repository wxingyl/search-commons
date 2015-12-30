package com.tqmall.search.common.cache;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.tqmall.search.common.param.NotifyChangeParam;
import com.tqmall.search.common.utils.HttpUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Created by xing on 15/12/23.
 * AbstractRtCacheReceive
 */
public abstract class AbstractRtCacheReceive<T extends SlaveHandleInfo> implements RtCacheReceive {

    private static final Logger log = LoggerFactory.getLogger(AbstractRtCacheReceive.class);

    /**
     * 本地机器注册的cache对象,这些对象对应着处理slave变化通知
     */
    private final Map<String, T> handleInfoMap = new HashMap<>();

    /**
     * 实例化{@link T}对象实例
     * 该函数为默认实现
     * @param masterHost 根据格式为ip:port组装的结果
     * @return 返回null, 则标识不进行注册
     */
    protected abstract T initSlaveHandleInfo(RtCacheSlaveHandle handler, String masterHost);

    /**
     * 执行具体的master register
     * @return 注册是否成功
     */
    protected abstract boolean doMasterRegister(int localPort, String masterHost, List<T> handleInfo);

    @Override
    public final boolean registerHandler(RtCacheSlaveHandle handler, RtCacheSlaveHandle.HostInfo masterHost) {
        Objects.requireNonNull(handler);
        Objects.requireNonNull(masterHost);
        if (StringUtils.isEmpty(masterHost.getIp())) {
            throw new IllegalArgumentException("masterIp is empty");
        }
        //这而判断个<= 80, 误杀那就算你倒霉,谁让你没事干的用系统端口, 本身就是找死
        if (masterHost.getPort() <= 80) {
            throw new IllegalArgumentException("masterPort " + masterHost.getPort() + " is invalid");
        }
        T info = initSlaveHandleInfo(handler, masterHost.getIp() + ':' + masterHost.getPort());
        if (info == null) {
            return false;
        } else {
            handleInfoMap.put(info.getCacheKey(), info);
            return true;
        }
    }

    @Override
    public boolean registerMaster(int localPort) {
        String localHost = HttpUtils.LOCAL_IP + ':' + localPort;
        Map<String, List<T>> group = Maps.newHashMap();
        //先根据masterHost分组, 同时讲masterHost跟localHost相同的过滤掉
        for (T info : handleInfoMap.values()) {
            String masterHost = info.getMasterHost();
            if (info.isRegisterSucceed()) continue;
            if (masterHost.equals(localHost)) {
                info.registerSucceed();
                log.info("注册cacheKey: " + info.getCacheKey() + ", masterHost: "
                        + masterHost + "同localHost一样, 无需注册");
                continue;
            }
            List<T> list = group.get(masterHost);
            if (list == null) {
                group.put(masterHost, list = Lists.newArrayList());
            }
            list.add(info);
        }
        boolean succeed = true;
        for (Map.Entry<String, List<T>> e : group.entrySet()) {
            if (doMasterRegister(localPort, e.getKey(), e.getValue())) {
                for (T info : e.getValue()) {
                    info.registerSucceed();
                }
                log.info("向masterHost: " + e.getKey() + " 注册cache: " + e.getValue() + "成功");
            } else {
                succeed = false;
                log.error("向masterHost: " + e.getKey() + " 注册cache: " + e.getValue() + "失败");
            }
        }
        return succeed;
    }

    @Override
    public boolean receive(NotifyChangeParam param) {
        log.info("接收到变化通知, cacheKey: " + param.getCacheKey() + ", keys: " + param.getKeys());
        if (param.getKeys() == null || param.getKeys().isEmpty()) return false;
        T handleInfo = handleInfoMap.get(param.getCacheKey());
        //为null表示发错了
        if (handleInfo == null) return false;
        if (handleInfo.getHandler().initialized()) {
            handleInfo.getHandler().onSlaveHandle(param.getKeys());
        }
        return true;
    }

    protected Map<String, T> getHandleInfoMap() {
        return handleInfoMap;
    }

    @Override
    public String toString() {
        return "RtCacheReceive{" + "allHandleInfo=" + handleInfoMap.values() + '}';
    }
}
