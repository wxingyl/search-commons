package com.tqmall.search.canal.action;

/**
 * Created by xing on 16/2/23.
 * InstanceAction 抽象类, 方便使用
 */
public abstract class AbstractInstanceAction implements InstanceAction {

    private final String instanceName;

    protected AbstractInstanceAction(String instanceName) {
        this.instanceName = instanceName;
    }

    @Override
    public String instanceName() {
        return instanceName;
    }

}
