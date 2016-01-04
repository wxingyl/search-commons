package com.tqmall.search.common.cache.receive;

import com.google.common.base.Predicate;
import com.tqmall.search.common.cache.RtCacheManager;
import com.tqmall.search.common.param.NotifyChangeParam;
import com.tqmall.search.common.utils.Filterable;
import com.tqmall.search.common.utils.HostInfo;
import com.tqmall.search.common.utils.HttpUtils;
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
     *
     * @return 注销是否成功
     */
    protected abstract boolean doMasterUnRegister(HostInfo localHost, T masterHostInfo);

    /**
     * 执行具体的监听操作
     *
     * @return 是否OK
     */
    protected abstract boolean doMasterMonitor(T masterHostInfo);

    /**
     * 填充masterHost信息, {@link #status()}接口使用, 默认啥都不干
     * @return hostInfo的map, 建议返回入参
     */
    protected Map<String, Object> appendHostInfo(T masterHost, Map<String, Object> hostInfo) {
        return hostInfo;
    }

    @Override
    public final boolean registerHandler(RtCacheSlaveHandle handler) {
        Objects.requireNonNull(handler);
        HostInfo masterHost = handler.getMasterHost();
        HttpUtils.hostInfoCheck(masterHost, false);
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
        HostInfo usedLocalHost = HttpUtils.hostInfoCheck(localHost, true);
        //先根据masterHost分组, 同时讲masterHost跟localHost相同的过滤掉
        boolean succeed = true;
        for (Map.Entry<T, List<String>> e : masterHostMap.entrySet()) {
            T master = e.getKey();
            if (!master.needDoRegister()) continue;
            if (HttpUtils.isEquals(master, usedLocalHost)) {
                master.setRegisterStatus(RegisterStatus.USELESS);
                log.info("向master机器: " + HttpUtils.hostInfoToString(master) + "注册cache与localHost相同, 无需执行注册操作");
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
                master.setRegisterStatus(RegisterStatus.USELESS);
                log.info("向master机器: " + HttpUtils.hostInfoToString(master) + "注册cache, RtCacheSlaveHandle全部被过滤, 无需执行注册操作");
                continue;
            }
            boolean localSucceed = doMasterRegister(usedLocalHost, master, cacheKeys);
            if (localSucceed) {
                master.setRegisterStatus(RegisterStatus.SUCCEED);
            } else {
                master.setRegisterStatus(RegisterStatus.FAILED);
                succeed = false;
            }
            log.info("cache注册: " + master + "执行完成, succeed: " + localSucceed + ", cacheKeys: " + cacheKeys);
        }
        return succeed;
    }

    @Override
    public boolean unRegister(HostInfo localHost) {
        HostInfo usedLocalHost = HttpUtils.hostInfoCheck(localHost, true);
        boolean ret = true;
        for (T info : masterHostMap.keySet()) {
            if (info.getRegisterStatus() == RegisterStatus.SUCCEED) {
                if (!doMasterUnRegister(usedLocalHost, info)) {
                    ret = false;
                }
            }
            info.setRegisterStatus(RegisterStatus.UNREGISTER);
        }
        return ret;
    }

    @Override
    public boolean doMonitor() {
        boolean haveException = false;
        for (T masterHost : masterHostMap.keySet()) {
            //已经注销了,我们就不用管了
            if (masterHost.getRegisterStatus() == RegisterStatus.UNREGISTER) continue;
            if (masterHost.needDoRegister()) {
                //说明注册还没有完成
                haveException = true;
            } else if (masterHost.getRegisterStatus() == RegisterStatus.SUCCEED) {
                //这儿才是需要真正监控的地方
                if (!doMasterMonitor(masterHost)) {
                    masterHost.setRegisterStatus(RegisterStatus.INTERRUPT);
                    haveException = true;
                }
            }
        }
        return !haveException;
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
    public List<Map<String, Object>> status() {
        List<Map<String, Object>> ret = new ArrayList<>();
        for (Map.Entry<T, List<String>> e : masterHostMap.entrySet()) {
            Map<String, Object> hostInfo = new HashMap<>();
            T master = e.getKey();
            hostInfo.put("host", HttpUtils.hostInfoToString(master));
            hostInfo.put("registerStatus", master.getRegisterStatus());
            hostInfo = appendHostInfo(master, hostInfo);
            List<Map<String, Object>> interestKeys = new ArrayList<>();
            for (String key : e.getValue()) {
                Map<String, Object> keyMap = new HashMap<>();
                keyMap.put("key", key);
                boolean filterApplied = true;
                if (filter != null) {
                    filterApplied = filter.apply(handleMap.get(key));
                }
                keyMap.put("key", filterApplied);
                interestKeys.add(keyMap);
            }
            hostInfo.put("interestKeys", interestKeys);
            ret.add(hostInfo);
        }
        return ret;
    }

    /**
     * 添加filter
     */
    @Override
    public void setFilter(Predicate<RtCacheSlaveHandle> filter) {
        this.filter = filter;
    }

    @Override
    public String toString() {
        return "RtCacheReceive{" + masterHostMap + '}';
    }
}
