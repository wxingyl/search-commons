package com.tqmall.search.common.cache;

import com.tqmall.search.common.param.SlaveRegisterParam;
import com.tqmall.search.common.result.MapResult;
import com.tqmall.search.common.utils.HostInfo;

import java.util.List;

/**
 * Created by xing on 15/12/22.
 * 本地cache对象通知变化的数据
 */
public interface RtCacheNotify {

    /**
     * 记录slave机器的注册请求,这个很有可能同时有多个进来,需要考虑多线程
     * @param param slave机器注册感兴趣的cache
     * @return 标识注册是否成功
     */
    MapResult handleSlaveRegister(SlaveRegisterParam param);

    /**
     * slaveHost取消注册
     */
    MapResult handleSlaveUnRegister(HostInfo slaveHost);

    /**
     * 1. master 机器通知给slave, 哪些key更改了
     * 2. 调用salveHost的通知接口的返回结构,建议都以{@link com.tqmall.search.common.result.MapResult}返回,
     * 这样好处理,兼容性好点, 提供的默认{@link HttpRtCacheNotify} 实现就是这样做的, 当然你可以自定义实现
     * 3. 逐个通知slave机器,如果某个slave机器通知异常,不管~~~
     * @param keys 统一用String标识
     * 发送通知,无需考虑多线程
     */
    boolean notify(RtCacheSlaveHandle slaveCache, List<String> keys);

}
