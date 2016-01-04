package com.tqmall.search.common.cache.notify;

import com.google.common.base.Supplier;
import com.google.common.collect.Maps;
import com.tqmall.search.common.cache.RtCacheManager;
import com.tqmall.search.common.cache.receive.RtCacheSlaveHandle;
import com.tqmall.search.common.param.LocalRegisterParam;
import com.tqmall.search.common.param.NotifyChangeParam;
import com.tqmall.search.common.result.MapResult;
import com.tqmall.search.common.result.ResultUtils;
import com.tqmall.search.common.utils.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

/**
 * Created by xing on 15/12/23.
 * abstract RtCacheNotify
 */
public abstract class AbstractRtCacheNotify<T extends AbstractSlaveHostInfo> implements RtCacheNotify {

    private static final Logger log = LoggerFactory.getLogger(AbstractRtCacheNotify.class);

    /**
     * key: cache key, value:slaveHost info list
     */
    private RwLock<Map<String, List<T>>> slaveHostLock = RwLock.build(new Supplier<Map<String, List<T>>>() {
        @Override
        public Map<String, List<T>> get() {
            return Maps.newHashMap();
        }
    });

    protected abstract T createSlaveInfo(LocalRegisterParam param);

    protected abstract void runNotifyTask(NotifyChangeParam param, List<T> slaves);

    protected abstract MapResult wrapSlaveRegisterResult(LocalRegisterParam param);

    @Override
    public MapResult handleSlaveRegister(final LocalRegisterParam param) {
        if (param.getSlaveHost() == null ||
                param.getInterestCache() == null || param.getInterestCache().isEmpty()) {
            log.warn("Slave注册参数不全: slaveHost: " + param.getSlaveHost() + ", interestCache: " + param.getInterestCache());
            return ResultUtils.mapResult(UtilsErrorCode.CACHE_SLAVE_REGISTER_ARG_INVALID);
        }
        final T slaveInfo;
        try {
            slaveInfo = createSlaveInfo(param);
        } catch (Throwable e) {
            return ResultUtils.mapResult(UtilsErrorCode.CACHE_SLAVE_REGISTER_INVALID, e.getMessage());
        }
        if (slaveInfo == null) {
            return ResultUtils.mapResult(UtilsErrorCode.CACHE_SLAVE_REGISTER_INVALID, "无法构建slaveHost: " + param.getSlaveHost() + "的信息");
        }
        MapResult mapResult = slaveHostLock.writeOp(new RwLock.OpRet<Map<String, List<T>>, MapResult>() {
            @Override
            public MapResult op(Map<String, List<T>> input) {
                for (String cache : param.getInterestCache()) {
                    List<T> slaveInfoList = input.get(cache);
                    if (slaveInfoList == null) {
                        input.put(cache, slaveInfoList = new ArrayList<>());
                    }
                    Iterator<T> it = slaveInfoList.iterator();
                    while (it.hasNext()) {
                        if (HttpUtils.isEquals(it.next().getSlaveHost(), slaveInfo.getSlaveHost())) {
                            //先将原先的实例删除, 再将新的实例添加进去
                            it.remove();
                            break;
                        }
                    }
                    slaveInfoList.add(slaveInfo);
                }
                return wrapSlaveRegisterResult(param);
            }
        });
        log.info("Slave注册处理成功, slaveHost: " + param.getSlaveHost() + ", interestCache: " + param.getInterestCache()
                + ", 返回结果: " + ResultUtils.resultToString(mapResult));
        return mapResult;
    }

    @Override
    public MapResult handleSlaveUnRegister(final HostInfo slaveHost) {
        if (slaveHost == null) {
            return ResultUtils.mapResult(UtilsErrorCode.CACHE_SLAVE_UNREGISTER_ARG_INVALID);
        }
        return slaveHostLock.writeOp(new RwLock.OpRet<Map<String, List<T>>, MapResult>() {
            @Override
            public MapResult op(Map<String, List<T>> input) {
                List<String> needRemoveKey = null;
                for (Map.Entry<String, List<T>> e : input.entrySet()) {
                    Iterator<T> it = e.getValue().iterator();
                    while (it.hasNext()) {
                        if (HttpUtils.isEquals(it.next().getSlaveHost(), slaveHost)) {
                            it.remove();
                        }
                    }
                    if (e.getValue().isEmpty()) {
                        if (needRemoveKey == null) needRemoveKey = new ArrayList<>();
                        needRemoveKey.add(e.getKey());
                    }
                }
                if (needRemoveKey != null) {
                    for (String k : needRemoveKey) {
                        input.remove(k);
                    }
                }
                log.info("Slave注销缓存处理完成, slaveHost: " + slaveHost);
                return ResultUtils.mapResult("msg", "注销成功");
            }
        });
    }

    @Override
    public boolean notify(RtCacheSlaveHandle slaveCache, List<String> keys) {
        if (keys == null || keys.isEmpty()) return false;
        final NotifyChangeParam param = new NotifyChangeParam();
        param.setCacheKey(RtCacheManager.getCacheHandleKey(slaveCache));
        param.setKeys(keys);
        slaveHostLock.readOp(new RwLock.Op<Map<String, List<T>>>() {
            @Override
            public void op(Map<String, List<T>> input) {
                List<T> slaveHosts = input.get(param.getCacheKey());
                if (slaveHosts == null || slaveHosts.isEmpty()) return;
                log.info("发送缓存" + param.getCacheKey() + "更改的key: " + param.getKeys() + "到机器: " + slaveHosts);
                runNotifyTask(param, slaveHosts);
            }
        });
        return true;
    }

    @Override
    public List<Map<String, Object>> status() {
        Map<T, List<String>> slaveHostMap = slaveHostLock.readOp(new RwLock.OpRet<Map<String, List<T>>, Map<T, List<String>>>() {
            @Override
            public Map<T, List<String>> op(Map<String, List<T>> input) {
                Map<T, List<String>> slaveHostMap = new HashMap<>();
                for (Map.Entry<String, List<T>> e : input.entrySet()) {
                    for (T t : e.getValue()) {
                        List<String> list = slaveHostMap.get(t);
                        if (list == null) {
                            slaveHostMap.put(t, list = new ArrayList<>());
                        }
                        if (!list.contains(e.getKey())) {
                            list.add(e.getKey());
                        }
                    }
                }
                return slaveHostMap;
            }
        });
        List<Map<String, Object>> ret = new ArrayList<>();
        for (T slaveHost : slaveHostMap.keySet()) {
            Map<String, Object> map = JsonUtils.objToMap(slaveHost);
            if (map == null) continue;
            map.put("interestKeys", slaveHostMap.get(slaveHost));
            ret.add(map);
        }
        return ret;
    }
}
