package com.tqmall.search.common.cache.receive;

import com.google.common.base.Predicate;
import com.tqmall.search.common.cache.RtCacheManager;
import com.tqmall.search.common.param.NotifyChangeParam;
import com.tqmall.search.common.utils.Filterable;
import com.tqmall.search.common.utils.HostInfo;
import com.tqmall.search.common.utils.HttpUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

/**
 * Created by xing on 15/12/23.
 * AbstractRtCacheReceive
 */
public abstract class AbstractRtCacheReceive<T extends MasterHostInfo> implements RtCacheReceive, Filterable<RtCacheSlaveHandle> {

    private static final Logger log = LoggerFactory.getLogger(AbstractRtCacheReceive.class);

    /**
     * key: cache key, value: handle
     */
    private final Map<String, RtCacheSlaveHandle> handleMap = new HashMap<>();

    /**
     * key: masterHostInfo, value: cache key
     */
    private final Map<T, List<String>> masterHostMap = new HashMap<>();

    private volatile Predicate<RtCacheSlaveHandle> filter;

    /**
     * 实例化{@link T}对象实例
     * 该函数为默认实现
     *
     * @return 返回null, 则标识不进行注册
     */
    protected abstract T initMasterHostInfo(HostInfo masterHost);

    /**
     * 执行具体的master register
     *
     * @return 注册是否成功
     */
    protected abstract boolean doMasterRegister(HostInfo localHost, T masterHostInfo, List<String> cacheKeys);

    /**
     * 调用master执行注销操作
     * @return 注销是否成功
     */
    protected abstract boolean doMasterUnRegister(HostInfo localHost, T masterHostInfo);

    /**
     * 执行具体的监听操作
     * @return 是否OK
     */
    protected abstract boolean doMasterMonitor(T masterHostInfo);

    private HostInfo localHostCheck(final HostInfo localHost) {
        Objects.requireNonNull(localHost);
        if (localHost.getPort() <= 80) {
            throw new IllegalArgumentException("localPort " + localHost.getPort() + " <= 80 is invalid");
        }
        if (StringUtils.isEmpty(localHost.getIp())) {
            return new HostInfo() {

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
            return localHost;
        }
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
        T masterHostInfo = null;
        for (T e : masterHostMap.keySet()) {
            if (HttpUtils.isEquals(e, masterHost)) {
                masterHostInfo = e;
                break;
            }
        }
        if (masterHostInfo == null) {
            masterHostInfo = initMasterHostInfo(masterHost);
            if (masterHostInfo != null) {
                masterHostMap.put(masterHostInfo, new ArrayList<String>());
            }
        }
        if (masterHostInfo == null) {
            log.warn("注册RtCacheSlaveHandle时, masterHost: " + HttpUtils.hostInfoToString(masterHost)
                    + ", 没有生成对用的MasterHostInfo对象, 取消注册");
            return false;
        } else {
            String cacheKey = RtCacheManager.getCacheHandleKey(handler);
            handleMap.put(cacheKey, handler);
            masterHostMap.get(masterHostInfo).add(cacheKey);
            return true;
        }
    }

    @Override
    public boolean registerMaster(HostInfo localHost) {
        if (masterHostMap.isEmpty()) return false;
        HostInfo usedLocalHost = localHostCheck(localHost);
        //先根据masterHost分组, 同时讲masterHost跟localHost相同的过滤掉
        boolean succeed = true;
        for (Map.Entry<T, List<String>> e : masterHostMap.entrySet()) {
            T master = e.getKey();
            if (!master.needRegister()) continue;
            if (HttpUtils.isEquals(master, usedLocalHost)) {
                master.setRegisterStatus(MasterHostInfo.REGISTER_STATUS_USELESS);
                log.info("注册cache处理时, masterHost: " + HttpUtils.hostInfoToString(master) + "同localHost相同, 无需注册");
                continue;
            }
            List<String> cacheKeys = new ArrayList<>(e.getValue().size());
            if (filter != null) {
                for (String ck : e.getValue()) {
                    if (filter.apply(handleMap.get(ck))) {
                        cacheKeys.add(ck);
                    }
                }
            } else {
                cacheKeys.addAll(e.getValue());
            }
            if (cacheKeys.isEmpty()) {
                master.setRegisterStatus(MasterHostInfo.REGISTER_STATUS_USELESS);
                log.info("注册cache处理时, masterHost: " + HttpUtils.hostInfoToString(master) + "的handle全部被过滤");
                continue;
            }
            if (doMasterRegister(usedLocalHost, master, cacheKeys)) {
                master.setRegisterStatus(MasterHostInfo.REGISTER_STATUS_SUCCEED);
            } else {
                master.setRegisterStatus(MasterHostInfo.REGISTER_STATUS_FAILED);
                succeed = false;
            }
            log.info("注册cache完成: " + master);
        }
        return succeed;
    }

    @Override
    public boolean unRegister(HostInfo localHost, HostInfo masterHost) {
        HostInfo usedLocalHost = localHostCheck(localHost);
        Objects.requireNonNull(masterHost);
        if (HttpUtils.isEquals(localHost, masterHost)) return false;
        T key = null;
        List<String> cacheKeys = null;
        for (T info : masterHostMap.keySet()) {
            if (HttpUtils.isEquals(masterHost, info)) {
                key = info;
                cacheKeys = masterHostMap.get(info);
                break;
            }
        }
        if (key == null) return false;
        for (String ck : cacheKeys) {
            handleMap.remove(ck);
        }
        masterHostMap.remove(key);
        return doMasterUnRegister(usedLocalHost, key);
    }

    @Override
    public boolean doMonitor() {
        boolean ret = true;
        for (T masterHost : masterHostMap.keySet()) {
            if (masterHost.needRegister()) {
                ret = false;
            } else if (masterHost.getRegisterStatus() == MasterHostInfo.REGISTER_STATUS_SUCCEED) {
                if (!doMasterMonitor(masterHost)) {
                    masterHost.setRegisterStatus(MasterHostInfo.REGISTER_STATUS_INTERRUPT);
                    ret = false;
                }
            }
        }
        return ret;
    }

    @Override
    public boolean receive(NotifyChangeParam param) {
        log.info("接收到变化通知, cacheKey: " + param.getCacheKey() + ", keys: " + param.getKeys());
        if (param.getKeys() == null || param.getKeys().isEmpty()) return false;
        RtCacheSlaveHandle handle = handleMap.get(param.getCacheKey());
        //为null表示发错了
        if (handle == null) return false;
        if (handle.initialized()) {
            handle.onSlaveHandle(param.getKeys());
        }
        return true;
    }

    @Override
    public String toString() {
        return "RtCacheReceive{" + masterHostMap + '}';
    }

    /**
     * 添加filter
     */
    @Override
    public void setFilter(Predicate<RtCacheSlaveHandle> filter) {
        this.filter = filter;
    }
}
