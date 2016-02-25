package com.tqmall.search.canal.handle;

import com.alibaba.otter.canal.client.CanalConnector;
import com.alibaba.otter.canal.client.CanalConnectors;
import com.alibaba.otter.canal.protocol.CanalEntry;
import com.alibaba.otter.canal.protocol.Message;
import com.tqmall.search.canal.CanalExecutor;
import com.tqmall.search.canal.RowChangedData;
import com.tqmall.search.commons.utils.CommonsUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.SocketAddress;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Created by xing on 16/2/22.
 * {@link CanalInstanceHandle} 的抽象类封装
 * 想自定义实现{@link CanalInstanceHandle}直接继承该类, 少些一些代码
 */
public abstract class AbstractCanalInstanceHandle implements CanalInstanceHandle {

    private static final Logger log = LoggerFactory.getLogger(AbstractCanalInstanceHandle.class);

    protected final CanalConnector canalConnector;

    protected final String instanceName;

    private int messageBatchSize = 1000;

    /**
     * {@link CanalConnector#getWithoutAck(int, Long, TimeUnit)}中的时间参数是Long的, 避免频繁的自动装箱,砸门还是直接定义成{@link Long}
     */
    private Long messageTimeout = 1000L;

    /**
     * 当前这在处理的schema.table
     * 每个canalHandle都是单个线程负责调用, 所以这儿也就不用考虑多线程, 如果以后添加了多线程处理, 引入{@link ThreadLocal}
     *
     * @see #startHandle(CanalEntry.Header)
     */
    protected String currentHandleSchema, currentHandleTable;

    protected CanalEntry.EventType currentEventType;

    /**
     * @param address     canal服务器地址
     * @param destination canal实例名称
     */
    public AbstractCanalInstanceHandle(SocketAddress address, String destination) {
        //canal中对于Connector中的用户名和密码不做校验, 所以设置也没有意义
        canalConnector = CanalConnectors.newSingleConnector(address, destination, null, null);
        this.instanceName = destination;
    }

    protected abstract void doConnect();

    protected abstract void doRowChangeHandle(List<? extends RowChangedData> changedData);

    protected abstract void doFinishHandle();

    /**
     * 执行{@link #doRowChangeHandle(List)}或者{@link #doFinishHandle()} 发生异常时, 触发该方法调用
     * 如果继续处理后续的更新数据, 忽略当前异常, 则返回true
     * 如果该异常较严重, 后续更新无法处理, 则返回false, canal执行器{@link CanalExecutor}会停止canal同步, 待问题处理之后再说~~~
     *
     * @param exception      对应的异常
     * @param inFinishHandle 标识是否在{@link #doFinishHandle()}中发生的异常
     * @return 是否忽略该异常
     */
    protected abstract boolean exceptionHandle(RuntimeException exception, boolean inFinishHandle);

    @Override
    public final String instanceName() {
        return instanceName;
    }

    @Override
    public final void connect() {
        log.info("canal instance: " + instanceName + " start connect");
        doConnect();
        log.info("canal instance: " + instanceName + " connect succeed");
    }

    @Override
    public void disConnect() {
        log.info("canal instance: " + instanceName + " start disConnect");
        try {
            canalConnector.unsubscribe();
        } finally {
            canalConnector.disconnect();
            log.info("canal instance: " + instanceName + " disConnect succeed");
        }
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

    /**
     * 更改记录的诗句转换成 {@link RowChangedData} list
     *
     * @param rowChange 更改的数据
     * @return 构建的{@link RowChangedData} list
     */
    protected List<? extends RowChangedData> changedDataParse(CanalEntry.RowChange rowChange) {
        return RowChangedData.build(rowChange);
    }

    @Override
    public boolean startHandle(CanalEntry.Header header) {
        currentHandleSchema = header.getSchemaName();
        currentHandleTable = header.getTableName();
        currentEventType = header.getEventType();
        return true;
    }

    @Override
    public final void rowChangeHandle(CanalEntry.RowChange rowChange) {
        try {
            List<? extends RowChangedData> changedData = changedDataParse(rowChange);
            if (!CommonsUtils.isEmpty(changedData)) doRowChangeHandle(changedData);
        } catch (RuntimeException e) {
            if (!exceptionHandle(e, false)) {
                throw e;
            }
        }
    }

    @Override
    public final void finishMessageHandle() {
        try {
            doFinishHandle();
        } catch (RuntimeException e) {
            if (!exceptionHandle(e, true)) {
                throw e;
            }
        }
    }

    @Override
    public void ack(long batchId) {
        canalConnector.ack(batchId);
    }

    /**
     * 如果batchId == 0, 则下次fetch的时候，可以从最后一个没有 {@link #ack(long)} 的地方开始拿, 即调用
     * {@link CanalConnector#rollback()}
     *
     * @param batchId message id
     */
    @Override
    public void rollback(long batchId) {
        if (batchId == 0L) {
            canalConnector.rollback();
        } else {
            canalConnector.rollback(batchId);
        }
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

}
