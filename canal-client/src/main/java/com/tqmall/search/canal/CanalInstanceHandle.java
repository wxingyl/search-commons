package com.tqmall.search.canal;

import com.alibaba.otter.canal.client.CanalConnector;
import com.alibaba.otter.canal.protocol.CanalEntry;
import com.alibaba.otter.canal.protocol.Message;

/**
 * Created by xing on 16/2/22.
 * 对{@link CanalConnector} 实例操作的封装
 */
public interface CanalInstanceHandle {

    /**
     * canal server配置的实例名称
     *
     * @return canal实例名称
     */
    String instanceName();

    /**
     * 执行{@link CanalConnector#connect()}, 同时执行{@link CanalConnector#subscribe()}操作
     */
    void connect();

    /**
     * 执行{@link CanalConnector#disconnect()}, 同时执行{@link CanalConnector#unsubscribe()}操作
     */
    void disConnect();

    /**
     * 批量获取数据, 不用自动ack
     *
     * @return binlog更新数据
     */
    Message getWithoutAck();

    /**
     * 进行 batch id 的确认。确认之后，小于等于此 batchId 的 Message 都会被确认
     *
     * @param batchId 数据id
     */
    void ack(long batchId);

    /**
     * 回滚到未进行{@link #ack(long)} 的地方，指定回滚具体的batchId
     * <p/>
     * 回滚操作只会在处理发生异常, 后续处理也不能正常处理, 必须停止当前canalInstance才会用到, 其他地方使用意义不大~~~
     *
     * @param batchId message id
     */
    void rollback(long batchId);

    /**
     * 根据{@link CanalEntry.Header}能够提供的信息先过滤一下, 关于事件类型, 时间戳有效性前面已经过滤过了~~~
     * @return 标识是否有效, 无效那就不解析其changeRow数据
     * @see CanalExecutor.CanalInstance#run()
     */
    boolean headerFilter(CanalEntry.Header header);

    /**
     * 记录更新处理, 一次处理过程, 不断调用该方法, 数据更新处理完成, 通过{@link #finishHandle()} 结束处理
     * @see #finishHandle()
     */
    void rowChangeHandle(CanalEntry.Header header, CanalEntry.RowChange rowChange);

    /**
     * 完成本次处理
     * @see #rowChangeHandle(CanalEntry.Header, CanalEntry.RowChange)
     */
    void finishHandle();
}
