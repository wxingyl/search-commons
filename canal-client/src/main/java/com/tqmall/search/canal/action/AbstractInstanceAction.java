package com.tqmall.search.canal.action;

import com.tqmall.search.canal.handle.InstanceRowChangedData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * Created by xing on 16/2/23.
 * InstanceAction 抽象类, 方便使用
 */
public abstract class AbstractInstanceAction implements InstanceAction {

    private static final Logger log = LoggerFactory.getLogger(AbstractInstanceAction.class);

    private final String instanceName;

    /**
     * 是否忽略处理异常, 默认忽略
     */
    private boolean ignoreHandleException = true;

    protected AbstractInstanceAction(String instanceName) {
        this.instanceName = instanceName;
    }

    public void setIgnoreHandleException(boolean ignoreHandleException) {
        this.ignoreHandleException = ignoreHandleException;
    }

    @Override
    public String instanceName() {
        return instanceName;
    }

    @Override
    public boolean exceptionHandle(RuntimeException exception, List<InstanceRowChangedData> exceptionData) {
        log.info("canalInstance: " + instanceName + " handle data change(" + exceptionData.size()
                + ") have exception, ignoreHandleException: " + ignoreHandleException, exception);
        return ignoreHandleException;
    }
}
