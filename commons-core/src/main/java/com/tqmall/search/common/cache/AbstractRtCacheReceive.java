package com.tqmall.search.common.cache;

import com.google.common.base.Predicate;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.tqmall.search.common.param.NotifyChangeParam;
import com.tqmall.search.common.utils.HostInfo;
import com.tqmall.search.common.utils.HttpUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * Created by xing on 15/12/23.
 * AbstractRtCacheReceive
 */
public abstract class AbstractRtCacheReceive<T extends SlaveHandleInfo> implements RtCacheReceive {

    private static final Logger log = LoggerFactory.getLogger(AbstractRtCacheReceive.class);

    /**
     * 本地机器注册的cache对象,这些对象对应着处理slave变化通知
     */
    private final ConcurrentMap<String, T> handleInfoMap = new ConcurrentHashMap<>();

    private volatile Predicate<T> filter;

    /**
     * 实例化{@link T}对象实例
     * 该函数为默认实现
     *
     * @param masterHost 根据格式为ip:port组装的结果
     * @return 返回null, 则标识不进行注册
     */
    protected abstract T initSlaveHandleInfo(RtCacheSlaveHandle handler, HostInfo masterHost);

    /**
     * 执行具体的master register
     *
     * @return 注册是否成功
     */
    protected abstract boolean doMasterRegister(HostInfo localHost, HostInfo masterHost, List<T> handleInfo);

    /**
     * 过滤SlaveHandle
     * @return true: 有效
     */
    protected boolean filterSlaveHandle(T handleInfo) {
        return filter == null || filter.apply(handleInfo);
    }

    @Override
    public final boolean registerHandler(RtCacheSlaveHandle handler) {
        Objects.requireNonNull(handler);
        HostInfo masterHost = handler.getMasterHost();
        Objects.requireNonNull(masterHost);
        if (StringUtils.isEmpty(masterHost.getIp())) {
            throw new IllegalArgumentException("masterIp is empty");
        }
        //这而判断个<= 80, 误杀那就算你倒霉,谁让你没事干的用系统端口, 本身就是找死
        if (masterHost.getPort() <= 80) {
            throw new IllegalArgumentException("masterPort " + masterHost.getPort() + " <= 80 is invalid");
        }
        T info = initSlaveHandleInfo(handler, masterHost);

        if (info == null || !filterSlaveHandle(info)) {
            return false;
        } else {
            handleInfoMap.put(info.getCacheKey(), info);
            return true;
        }
    }

    @Override
    public boolean registerMaster(final HostInfo localHost) {
        Objects.requireNonNull(localHost);
        if (localHost.getPort() <= 80) {
            throw new IllegalArgumentException("localPort " + localHost.getPort() + " <= 80 is invalid");
        }
        final HostInfo usedLocalHost;
        if (StringUtils.isEmpty(localHost.getIp())) {
            usedLocalHost = new HostInfo() {

                //传进来的ip不好使就用默认的
                @Override
                public String getIp() {
                    return HttpUtils.LOCAL_IP;
                }

                @Override
                public int getPort() {
                    return localHost.getPort();
                }
            };
        } else {
            usedLocalHost = localHost;
        }
        Map<HostInfo, List<T>> group = Maps.newHashMap();
        //先根据masterHost分组, 同时讲masterHost跟localHost相同的过滤掉
        for (T info : handleInfoMap.values()) {
            HostInfo masterHost = info.getMasterHost();
            if (info.isRegisterSucceed()) continue;
            if (HttpUtils.isEquals(masterHost, usedLocalHost)) {
                info.registerSucceed();
                log.info("注册cacheKey: " + info.getCacheKey() + ", masterHost: "
                        + HttpUtils.hostInfoToString(masterHost) + "同localHost一样, 无需注册");
                continue;
            }
            List<T> list = group.get(masterHost);
            if (list == null) {
                group.put(masterHost, list = Lists.newArrayList());
            }
            list.add(info);
        }
        boolean succeed = true;
        for (Map.Entry<HostInfo, List<T>> e : group.entrySet()) {
            if (doMasterRegister(usedLocalHost, e.getKey(), e.getValue())) {
                for (T info : e.getValue()) {
                    info.registerSucceed();
                }
                log.info("向masterHost: " + HttpUtils.hostInfoToString(e.getKey()) + " 注册cache: " + e.getValue() + "成功");
            } else {
                succeed = false;
                log.error("向masterHost: " + HttpUtils.hostInfoToString(e.getKey()) + " 注册cache: " + e.getValue() + "失败");
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

    protected ConcurrentMap<String, T> getHandleInfoMap() {
        return handleInfoMap;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder(512);
        sb.append("RtCacheReceive{");
        if (handleInfoMap.isEmpty()) {
            sb.append("empty handle");
        } else {
            Map<HostInfo, Pair<Boolean, ? extends List<String>>> group = Maps.newHashMap();
            for (T info : handleInfoMap.values()) {
                HostInfo masterHost = info.getMasterHost();
                Pair<Boolean, ? extends List<String>> pair = group.get(masterHost);
                if (pair == null) {
                    group.put(masterHost, pair = Pair.of(info.isRegisterSucceed(), new ArrayList<String>()));
                }
                pair.getRight().add(info.getCacheKey());
            }
            for (Map.Entry<HostInfo, Pair<Boolean, ? extends List<String>>> e : group.entrySet()) {
                sb.append(HttpUtils.hostInfoToString(e.getKey())).append("->")
                        .append(" registerSucceed: ").append(e.getValue().getLeft())
                        .append(", cacheKeys: ").append(e.getValue().getRight())
                        .append(", ");
            }
            sb.delete(sb.length() - 2, sb.length());
        }
        sb.append('}');
        return sb.toString();
    }

    public void setFilter(Predicate<T> filter) {
        this.filter = filter;
        List<String> needRemoveKeys = Lists.newArrayList();
        for (T info : handleInfoMap.values()) {
            if (!filterSlaveHandle(info)) {
                needRemoveKeys.add(info.getCacheKey());
            }
        }
        for (String key: needRemoveKeys) {
            handleInfoMap.remove(key);
        }
    }
}
