package com.tqmall.search.common.cache;

import com.google.common.base.Supplier;
import com.google.common.collect.Maps;
import com.tqmall.search.common.param.NotifyChangeParam;
import com.tqmall.search.common.param.SlaveRegisterParam;
import com.tqmall.search.common.result.MapResult;
import com.tqmall.search.common.result.ResultUtils;
import com.tqmall.search.common.utils.RwLock;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created by xing on 15/12/23.
 * abstract RtCacheNotify
 */
public abstract class AbstractRtCacheNotify<T extends AbstractSlaveRegisterInfo> implements RtCacheNotify {

    private static final Logger log = LoggerFactory.getLogger(AbstractRtCacheNotify.class);

    /**
     * key: cache key, value: {@link Pair#getLeft()} is slaveHost, {@link Pair#getRight()} is urlPath
     */
    private RwLock<Map<String, List<T>>> slaveHostLock = RwLock.build(new Supplier<Map<String, List<T>>>() {
        @Override
        public Map<String, List<T>> get() {
            return Maps.newHashMap();
        }
    });

    protected abstract T createSlaveInfo(SlaveRegisterParam param);

    protected abstract void runNotifyTask(NotifyChangeParam param, List<T> slaves);

    @Override
    public MapResult handleSlaveRegister(final SlaveRegisterParam param) {
        if (StringUtils.isEmpty(param.getSlaveHost()) ||
                param.getInterestCache() == null || param.getInterestCache().isEmpty()) {
            return ResultUtils.mapResult(CacheErrorCode.SLAVE_REGISTER_INVALID,
                    "参数slaveHost为空或者interestCache为空");
        }
        return slaveHostLock.writeOp(new RwLock.OpRet<Map<String, List<T>>, MapResult>() {
            @Override
            public MapResult op(Map<String, List<T>> input) {
                for (String cache : param.getInterestCache()) {
                    List<T> slaveHosts = input.get(cache);
                    if (slaveHosts == null) {
                        input.put(cache, slaveHosts = new ArrayList<>());
                    }
                    T info;
                    try {
                        info = createSlaveInfo(param);
                    } catch (Throwable e) {
                        return ResultUtils.mapResult(CacheErrorCode.SLAVE_REGISTER_INVALID, e.getMessage());
                    }
                    if (info != null && !slaveHosts.contains(info)) {
                        slaveHosts.add(info);
                        log.info("Slave注册缓存处理成功, slaveHost: " + param.getSlaveHost() + ", interestCache: " + param.getInterestCache());
                    }
                }
                return ResultUtils.mapResult("msg", "注册成功");
            }
        });
    }

    @Override
    public void notify(RtCacheSlaveHandle slaveCache, List<String> keys) {
        if (keys == null || keys.isEmpty()) return;
        final NotifyChangeParam param = new NotifyChangeParam();
        param.setCacheKey(RtCacheManager.getCacheHandleKey(slaveCache));
        param.setKeys(keys);
        final String cacheKey = RtCacheManager.getCacheHandleKey(slaveCache);
        slaveHostLock.readOp(new RwLock.Op<Map<String, List<T>>>() {
            @Override
            public void op(Map<String, List<T>> input) {
                List<T> slaveHosts = input.get(cacheKey);
                if (slaveHosts == null || slaveHosts.isEmpty()) return;
                log.info("发送缓存" + cacheKey + "更改的key: " + param.getKeys() + "到机器: " + slaveHosts);
                runNotifyTask(param, slaveHosts);
            }
        });
    }

}
