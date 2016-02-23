package com.tqmall.search.canal;

import com.alibaba.otter.canal.client.CanalConnector;
import com.alibaba.otter.canal.client.CanalConnectors;
import com.google.common.base.Function;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.SocketAddress;

/**
 * Created by xing on 16/2/22.
 * {@link CanalInstanceHandle} 的抽象类封装
 * 想自定义实现{@link CanalInstanceHandle}直接继承该类, 少些一些代码
 */
public abstract class AbstractCanalInstanceHandle implements CanalInstanceHandle {

    private static final Logger log = LoggerFactory.getLogger(AbstractCanalInstanceHandle.class);

    protected final CanalConnector canalConnector;

    protected final String instanceName;

    /**
     * 异常处理方法, 优先根据该Function处理
     */
    private Function<HandleExceptionContext, Boolean> handleExceptionFunction;

    /**
     * 是否忽略处理异常, 默认忽略
     * 优先处理{@link #handleExceptionFunction}
     */
    private boolean ignoreHandleException = true;

    /**
     * @param address     canal服务器地址
     * @param destination canal实例名称
     */
    public AbstractCanalInstanceHandle(SocketAddress address, String destination) {
        //canal中对于Connector中的用户名和密码不做校验, 所以设置也没有意义
        canalConnector = CanalConnectors.newSingleConnector(address, destination, null, null);
        this.instanceName = destination;
    }

    @Override
    public String instanceName() {
        return instanceName;
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

    public boolean isIgnoreHandleException() {
        return ignoreHandleException;
    }


    /**
     * 是否忽略处理异常, 默认忽略
     * 优先处理{@link #handleExceptionFunction}
     */
    public void setIgnoreHandleException(boolean ignoreHandleException) {
        this.ignoreHandleException = ignoreHandleException;
    }

    public Function<HandleExceptionContext, Boolean> getHandleExceptionFunction() {
        return handleExceptionFunction;
    }

    /**
     * 异常处理方法, 优先根据该Function处理
     *
     * @param handleExceptionFunction 该function的返回结果标识是否忽略该异常, 同{@link #ignoreHandleException}
     * @see #ignoreHandleException
     */
    public void setHandleExceptionFunction(Function<HandleExceptionContext, Boolean> handleExceptionFunction) {
        this.handleExceptionFunction = handleExceptionFunction;
    }

    @Override
    public boolean exceptionHandle(HandleExceptionContext context) {
        if (handleExceptionFunction != null) {
            Boolean ignore = handleExceptionFunction.apply(context);
            return ignore == null ? false : ignore;
        } else {
            log.error("canal handle table: " + context.getSchema() + '.' + context.getTable() + ", eventType: "
                    + context.getEventType() + ", changeDataSize: " + context.getChangedData().size() + " occurring exception", context.getException());
            return ignoreHandleException;
        }
    }
}
