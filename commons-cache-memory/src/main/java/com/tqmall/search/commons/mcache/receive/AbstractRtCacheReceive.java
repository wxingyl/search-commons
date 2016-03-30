package com.tqmall.search.commons.mcache.receive;

import com.google.common.base.Predicate;
import com.tqmall.search.commons.mcache.MemoryCacheErrorCode;
import com.tqmall.search.commons.mcache.RtCacheManager;
import com.tqmall.search.commons.lang.HostInfo;
import com.tqmall.search.commons.param.NotifyChangeParam;
import com.tqmall.search.commons.result.MapResult;
import com.tqmall.search.commons.result.ResultUtils;
import com.tqmall.search.commons.utils.HttpUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

/**
 * Created by xing on 15/12/23.
 * AbstractRtCacheReceive
 */
public abstract class AbstractRtCacheReceive<T extends MasterHostInfo> implements RtCacheReceive {

    private static final Logger log = LoggerFactory.getLogger(AbstractRtCacheReceive.class);

    /**
     * key: cache key, value: handle
     */
    private final Map<String, RtCacheSlaveHandle> handleMap = new HashMap<>();

    /**
     * key: masterHostInfo, value: cache key
     */
    private final Map<T, List<String>> masterHostMap = new HashMap<>();

    private Predicate<RtCacheSlaveHandle> filter;

    /**
     * 注册master次数, 等同于调用{@link #registerMaster(HostInfo)}, 除了参数错误之外的次数
     * 由于方法加了同步锁, 不用考虑多线程问题
     */
    private int registerMasterTimes;

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
    protected abstract boolean doMasterMonitor(HostInfo localHost, T masterHostInfo);

    /**
     * 填充masterHost信息, {@link #status()}接口使用, 默认啥都不干
     * @return hostInfo的map, 建议返回入参
     */
    protected Map<String, Object> appendHostStatusInfo(T masterHost, Map<String, Object> hostInfo) {
        return hostInfo;
    }

    /**
     * @return 返回的Map中key = "status"表示处理的状态, true为成功
     */
    @Override
    public final synchronized MapResult registerHandler(RtCacheSlaveHandle handler) {
        Objects.requireNonNull(handler);
        HostInfo masterHost = handler.getMasterHost();
        try {
            HttpUtils.hostInfoCheck(masterHost, false);
        } catch (IllegalArgumentException e) {
            log.warn("registerHandler, masterHost: " + HttpUtils.hostInfoToString(masterHost) + "参数有误: "
                    + e.getMessage() + ", 无法完成注册");
            return ResultUtils.mapResult(MemoryCacheErrorCode.HOST_INFO_INVALID, e.getMessage());
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
            String msg = "注册RtCacheSlaveHandle时, masterHost: " + HttpUtils.hostInfoToString(masterHost)
                    + ", 没有生成对用的MasterHostInfo对象, 取消注册";
            log.warn(msg);
            return ResultUtils.mapResult(MemoryCacheErrorCode.RECEIVER_RUNTIME_ERROR, msg);
        } else {
            String cacheKey = RtCacheManager.getCacheHandleKey(handler);
            handleMap.put(cacheKey, handler);
            masterHostMap.get(masterHostInfo).add(cacheKey);
            return ResultUtils.mapResult("status", true);
        }
    }

    /**
     * 返回的Map中:
     * 1. key为"finish"表示注册完所有机器, bool类型, 如果为false表明没有完成注册,可能masterHost机器停机等原因,稍后重试可能就成功了
     * 2. key为"times"表示当前已经注册的次数
     */
    @Override
    public synchronized MapResult registerMaster(HostInfo localHost) {
        if (masterHostMap.isEmpty()) {
            ResultUtils.mapResult("finish", true);
        }
        HostInfo usedLocalHost;
        try {
            usedLocalHost = HttpUtils.hostInfoCheck(localHost, true);
        } catch (IllegalArgumentException e) {
            log.warn("registerMaster, localHost: " + HttpUtils.hostInfoToString(localHost) + "参数有误: "
                    + e.getMessage() + ", 无法完成注册");
            return ResultUtils.mapResult(MemoryCacheErrorCode.HOST_INFO_INVALID, e.getMessage());
        }
        boolean finish = true;
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
            boolean localSucceed;
            try {
                localSucceed = doMasterRegister(usedLocalHost, master, cacheKeys);
            } catch (RuntimeException re) {
                log.error("doMasterRegister 存在异常", re);
                localSucceed = false;
            }
            if (localSucceed) {
                master.setRegisterStatus(RegisterStatus.SUCCEED);
            } else {
                master.setRegisterStatus(RegisterStatus.FAILED);
                finish = false;
            }
            log.info("cache注册: " + master + "执行完成, succeed: " + localSucceed + ", cacheKeys: " + cacheKeys);
        }
        registerMasterTimes++;
        MapResult result = ResultUtils.mapResult("finish", finish);
        result.put("times", registerMasterTimes);
        return result;
    }

    /**
     * @return 返回的Map中key = "status"表示处理的状态, true为成功
     */
    @Override
    public synchronized MapResult unRegister(HostInfo localHost) {
        HostInfo usedLocalHost;
        try {
           usedLocalHost = HttpUtils.hostInfoCheck(localHost, true);
        } catch (IllegalArgumentException e) {
            log.warn("unRegister, localHost: " + HttpUtils.hostInfoToString(localHost) + "参数有误: "
                    + e.getMessage() + ", 无法完成注销");
            return ResultUtils.mapResult(MemoryCacheErrorCode.HOST_INFO_INVALID, e.getMessage());
        }
        boolean status = true;
        for (T info : masterHostMap.keySet()) {
            if (info.getRegisterStatus() == RegisterStatus.SUCCEED) {
                boolean succeed;
                try {
                    succeed = doMasterUnRegister(usedLocalHost, info);
                } catch (RuntimeException e) {
                    succeed = false;
                    log.error("doMasterUnRegister 存在异常", e);
                }
                if (!succeed) {
                    status = false;
                }
            }
            info.setRegisterStatus(RegisterStatus.UNREGISTER);
        }
        return ResultUtils.mapResult("status", status);
    }

    @Override
    public boolean doMonitor(HostInfo localHost) {
        boolean haveException = false;
        for (T masterHost : masterHostMap.keySet()) {
            //已经注销了,我们就不用管了
            if (masterHost.getRegisterStatus() == RegisterStatus.UNREGISTER) continue;
            if (masterHost.needDoRegister()) {
                //说明注册还没有完成
                haveException = true;
            } else if (masterHost.getRegisterStatus() == RegisterStatus.SUCCEED) {
                //这儿才是需要真正监控的地方
                boolean succeed;
                try {
                    succeed = doMasterMonitor(localHost, masterHost);
                } catch (RuntimeException e) {
                    succeed = false;
                    log.error("doMasterMonitor 存在异常", e);
                }
                if (!succeed) {
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
            hostInfo = appendHostStatusInfo(master, hostInfo);
            List<Map<String, Object>> interestKeys = new ArrayList<>();
            for (String key : e.getValue()) {
                Map<String, Object> keyMap = new HashMap<>();
                keyMap.put("key", key);
                boolean filterApplied = true;
                if (filter != null) {
                    filterApplied = filter.apply(handleMap.get(key));
                }
                keyMap.put("filterApplied", filterApplied);
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
    public void setFilter(Predicate<RtCacheSlaveHandle> filter) {
        this.filter = filter;
    }

    @Override
    public String toString() {
        return "RtCacheReceive{" + masterHostMap + '}';
    }
}
