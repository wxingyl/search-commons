package com.tqmall.search.commons.mcache.notify;

import com.tqmall.search.commons.mcache.MemoryCacheErrorCode;
import com.tqmall.search.commons.mcache.RtCacheManager;
import com.tqmall.search.commons.mcache.receive.RtCacheSlaveHandle;
import com.tqmall.search.commons.lang.HostInfo;
import com.tqmall.search.commons.param.LocalRegisterParam;
import com.tqmall.search.commons.param.NotifyChangeParam;
import com.tqmall.search.commons.result.MapResult;
import com.tqmall.search.commons.result.ResultUtils;
import com.tqmall.search.commons.utils.CommonsUtils;
import com.tqmall.search.commons.utils.HttpUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * Created by xing on 15/12/23.
 * abstract RtCacheNotify
 */
public abstract class AbstractRtCacheNotify<T extends AbstractSlaveHostInfo> implements RtCacheNotify {

    private static final Logger log = LoggerFactory.getLogger(AbstractRtCacheNotify.class);

    /**
     * key: cache key, value:slaveHost info list
     */
    private ConcurrentMap<T, Set<String>> slaveHostMap = new ConcurrentHashMap<>();

    protected abstract T createSlaveInfo(LocalRegisterParam param);

    protected abstract void runNotifyTask(NotifyChangeParam param, List<T> slaves);

    protected abstract MapResult wrapSlaveRegisterResult(LocalRegisterParam param);

    protected Map<String, Object> appendHostStatusInfo(T slaveHost, Map<String, Object> infoMap) {
        return infoMap;
    }

    /**
     * 根据slaveHost获取对应的HostInfo
     * @return 如果没有,不存在返回null
     */
    protected T getSlaveHost(HostInfo slaveHost) {
        for (T host : slaveHostMap.keySet()) {
            if (HttpUtils.isEquals(host, slaveHost)) {
                return host;
            }
        }
        return null;
    }

    @Override
    public MapResult handleSlaveRegister(final LocalRegisterParam param) {
        if (param.getSlaveHost() == null ||
                param.getInterestCache() == null || param.getInterestCache().isEmpty()) {
            log.warn("Slave注册参数不全: slaveHost: " + param.getSlaveHost() + ", interestCache: " + param.getInterestCache());
            return ResultUtils.mapResult(MemoryCacheErrorCode.NOTIFY_HANDLE_ARG_INVALID);
        }
        final T slaveInfo;
        try {
            slaveInfo = createSlaveInfo(param);
        } catch (Throwable e) {
            log.error("注册时创建slaveHost信息存在异常", e);
            return ResultUtils.mapResult(MemoryCacheErrorCode.NOTIFY_RUNTIME_ERROR, "注册时创建slaveHost信息异常: " + e.getMessage());
        }
        if (slaveInfo == null) {
            return ResultUtils.mapResult(MemoryCacheErrorCode.NOTIFY_RUNTIME_ERROR, "无法构建slaveHost: " + param.getSlaveHost() + "的信息");
        }
        Set<String> interestKeys = new HashSet<>(param.getInterestCache());
        slaveHostMap.put(slaveInfo, interestKeys);
        MapResult mapResult = wrapSlaveRegisterResult(param);
        log.info("Slave注册处理成功, slaveHost: " + param.getSlaveHost() + ", interestCache: " + interestKeys
                + ", 返回结果: " + ResultUtils.resultToString(mapResult));
        return mapResult;
    }

    @Override
    public MapResult handleSlaveUnRegister(final HostInfo slaveHost) {
        if (slaveHost == null) {
            return ResultUtils.mapResult(MemoryCacheErrorCode.NOTIFY_HANDLE_ARG_INVALID);
        }
        T key = getSlaveHost(slaveHost);
        if (key == null) {
            return ResultUtils.mapResult(MemoryCacheErrorCode.NOTIFY_UNREGISTER_UNKNOWN_HOST, HttpUtils.hostInfoToString(slaveHost));
        }
        slaveHostMap.remove(key);
        log.info("Slave注销缓存处理完成, slaveHost: " + slaveHost);
        return ResultUtils.mapResult("msg", "注销成功");
    }

    @Override
    public MapResult handleMonitor(HostInfo slaveHost) {
        if (slaveHost == null) {
            return ResultUtils.mapResult(MemoryCacheErrorCode.NOTIFY_HANDLE_ARG_INVALID);
        }
        boolean found = getSlaveHost(slaveHost) != null;
        return ResultUtils.mapResult("status", found);
    }

    @Override
    public boolean notify(RtCacheSlaveHandle slaveCache, List<String> keys) {
        if (CommonsUtils.isEmpty(keys)) return false;
        String cacheKey = RtCacheManager.getCacheHandleKey(slaveCache);
        List<T> slaveHostList = new ArrayList<>();
        for (Map.Entry<T, Set<String>> e : slaveHostMap.entrySet()) {
            if (e.getValue().contains(cacheKey)) {
                slaveHostList.add(e.getKey());
            }
        }
        log.info("发送缓存" + cacheKey + "更改的key: " + keys + "到机器: " + slaveHostList);
        if (slaveHostList.isEmpty()) {
            //没有slave机器订阅, 那就算了
            return true;
        }
        final NotifyChangeParam param = new NotifyChangeParam();
        param.setCacheKey(cacheKey);
        param.setKeys(keys);
        runNotifyTask(param, slaveHostList);
        return true;
    }

    @Override
    public List<Map<String, Object>> status() {
        List<Map<String, Object>> ret = new ArrayList<>();
        for (Map.Entry<T, Set<String>> e : slaveHostMap.entrySet()) {
            Map<String, Object> map = new HashMap<>();
            map.put("host", HttpUtils.hostInfoToString(e.getKey()));
            map.put("interestKeys", e.getValue());
            map = appendHostStatusInfo(e.getKey(), map);
            ret.add(map);
        }
        return ret;
    }
}
