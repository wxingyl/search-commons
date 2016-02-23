package com.tqmall.search.canal;

import com.alibaba.otter.canal.client.CanalConnector;
import com.alibaba.otter.canal.protocol.Message;

import java.util.List;

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
     * 处理数据更新~~~, 每次更新处理,都是同类型更新事件, changedData中不会存在{@link RowChangedData.Insert}, {@link RowChangedData.Delete}和
     * {@link RowChangedData.Update}混合的对象实例, 所以判断时间类型可以通过如下代码
     * <p/>
     * <pre>{@code
     *      CanalEntry.EventType eventType = RowChangedData.getEventType(changedData.get(0));
     * }</pre>
     *
     * @param changedData 待处理的更新数据, 调用的时候保证changeData有效, 即 != null && !isEmpty()
     * @param schema      对应的schema name
     * @param table       对应的table name
     * @see RowChangedData.Update
     * @see RowChangedData.Delete
     * @see RowChangedData.Insert
     * @see RowChangedData#getEventType(RowChangedData)
     */
    void rowChangeHandle(String schema, String table, List<? extends RowChangedData> changedData);

    /**
     * 处理函数{@link #rowChangeHandle(String, String, List)}发生异常时, 出发该方法调用
     * 如果继续处理后续的更新数据, 忽略当前异常, 则返回true
     * 如果该异常较严重, 后续更新无法处理, 则返回false, canal执行器{@link CanalExecutor}会停止canal同步, 待问题处理之后再说~~~
     *
     * @param context 出异常时的上下文
     * @return 是否忽略该异常
     * @see CanalExecutor.CanalInstance#runHandleRowChange()
     * @see CanalExecutor#startInstance(String)
     */
    boolean exceptionHandle(HandleExceptionContext context);
}
