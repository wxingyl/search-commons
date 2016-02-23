package com.tqmall.search.canal;

import com.alibaba.otter.canal.client.CanalConnector;
import com.alibaba.otter.canal.protocol.Message;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.SocketAddress;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Created by xing on 16/2/22.
 * 默认的{@link CanalInstanceHandle} 实现
 */
public class SimpleCanalInstanceHandle extends AbstractCanalInstanceHandle {

    private static final Logger log = LoggerFactory.getLogger(SimpleCanalInstanceHandle.class);

    private int messageBatchSize = 1000;

    /**
     * {@link CanalConnector#getWithoutAck(int, Long, TimeUnit)}中的时间参数是Long的, 避免频繁的自动装箱,砸门还是直接定义成{@link Long}
     */
    private Long messageTimeout = 1000L;

    /**
     * @param address     canal服务器地址
     * @param destination canal实例名称
     */
    public SimpleCanalInstanceHandle(SocketAddress address, String destination) {
        super(address, destination);
    }

    @Override
    public void connect() {
        log.info("canal instance: " + instanceName + " start connect");
        log.info("canal instance: " + instanceName + " connect succeed");
    }

    @Override
    public void disConnect() {
        log.info("canal instance: " + instanceName + " start disConnect");
        log.info("canal instance: " + instanceName + " disConnect succeed");
    }

    /**
     * 获取Message {@link CanalConnector#getWithoutAck(int, Long, TimeUnit)}的batchSize
     * 默认1000, 如果 <= 0, canal内部取默认1000
     */
    public void setMessageBatchSize(int messageBatchSize) {
        this.messageBatchSize = messageBatchSize;
    }

    /**
     * 获取Message {@link CanalConnector#getWithoutAck(int, Long, TimeUnit)}的超时时间
     * time unit is ms, 默认1s
     */
    public void setMessageTimeout(Long messageTimeout) {
        this.messageTimeout = messageTimeout;
    }

    /**
     * 不指定 position 获取事件.
     * 该方法返回的条件：
     * a. 拿够{@link #messageBatchSize}条记录或者超过timeout时间
     * b. 如果{@link #messageTimeout} = 0，则阻塞至拿到batchSize记录才返回
     * <p/>
     * canal 会记住此 client 最新的position。 <br/>
     * 如果是第一次 fetch，则会从 canal 中保存的最老一条数据开始输出。
     */
    @Override
    public Message getWithoutAck() {
        return canalConnector.getWithoutAck(messageBatchSize, messageTimeout, TimeUnit.MILLISECONDS);
    }

    @Override
    public void rowChangeHandle(String schema, String table, List<? extends RowChangedData> changedData) {

    }

}
