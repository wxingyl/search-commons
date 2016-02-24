package com.tqmall.search.canal;

import com.alibaba.otter.canal.common.utils.AddressUtils;
import com.google.common.base.Function;
import com.tqmall.search.canal.action.AbstractInstanceAction;
import com.tqmall.search.canal.action.EventTypeAction;
import com.tqmall.search.canal.action.SchemaTables;
import com.tqmall.search.canal.action.TableAction;
import com.tqmall.search.canal.handle.*;
import com.tqmall.search.commons.lang.LazyInit;
import com.tqmall.search.commons.lang.Supplier;

import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by xing on 16/2/24.
 * canal client 使用demo
 *
 * @see #INSTANCE
 * @see #addInstanceSectionCanalInstance()
 * @see #addTableSectionCanalInstance()
 * @see #addEventTypeSectionCanalInstance()
 * @see #startInstance()
 */
public class CanalClientDemo {

    /**
     * 最好单例~~~
     */
    private final static LazyInit<CanalExecutor> INSTANCE = new LazyInit<>(new Supplier<CanalExecutor>() {
        @Override
        public CanalExecutor get() {
            return new CanalExecutor();
        }
    });

    /**
     * canal server 端口, 默认是11111
     */
    private final static int CANAL_PORT = 11111;

    /**
     * canal server ip地址, 这儿就直接取本地了
     */
    private final static SocketAddress LOCAL_ADDRESS = new InetSocketAddress(AddressUtils.getHostAddress(), CANAL_PORT);

    /**
     * 添加{@link InstanceSectionHandle}
     */
    private void addInstanceSectionCanalInstance() {
        //canal实例名称
        String instanceName = "legend-instance-section";

        InstanceSectionHandle instanceSectionHandle = new InstanceSectionHandle(LOCAL_ADDRESS, new AbstractInstanceAction(instanceName) {
            @Override
            public void onAction(List<? extends InstanceRowChangedData> rowChangedData) {
                //do some work or call some function~~~
            }
        });

        //下面是异常处理以及canal连接的一些参数设置, 都有默认值, 需要设置之~~~
        instanceSectionHandle.setExceptionHandleFunction(new Function<InstanceSectionHandle.ExceptionContext, Boolean>() {
            @Override
            public Boolean apply(InstanceSectionHandle.ExceptionContext input) {
                //do some exception handle
                return false;
            }
        });
        instanceSectionHandle.setMessageBatchSize(1000);
        instanceSectionHandle.setMessageTimeout(500L);

        INSTANCE.getInstance().addInstanceHandle(instanceSectionHandle);
    }

    /**
     * 添加{@link TableSectionHandle}
     */
    private void addTableSectionCanalInstance() {

        SchemaTables.Builder<TableAction> schemaTablesBuilder = SchemaTables.builder();
        List<SchemaTables.Table<TableAction>> tableList = new ArrayList<>();

        //添加legend表中的legend_shop和legend_shop_service_info表处理
        tableList.add(SchemaTables.Table.<TableAction>build("legend_shop")
                .action(new TableAction() {
                    @Override
                    public void onAction(List<? extends RowChangedData> changedData) {
                        //legend-shop表改动对应的处理
                        //do some work or call some function~~~
                    }
                })
                .create()
        );
        tableList.add(SchemaTables.Table.<TableAction>build("legend_shop_service_info")
                .action(new TableAction() {
                    @Override
                    public void onAction(List<? extends RowChangedData> changedData) {
                        //legend_shop_service_info表改动对应的处理
                        //do some work or call some function~~~
                    }
                })
                .columns("id", "is_deleted", "name", "service_sn") //目前还不支持列过滤~~~不过很快了~~~
                .create()
        );
        String schemaName = "legend";
        schemaTablesBuilder.add(schemaName, tableList);

        //添加dandelion表中的activity, member表处理
        schemaName = "dandelion";
        schemaTablesBuilder.add(schemaName,
                SchemaTables.Table.<TableAction>build("activity")
                        .action(new TableAction() {
                            @Override
                            public void onAction(List<? extends RowChangedData> changedData) {
                                //activity表改动对应的处理
                                //do some work or call some function~~~
                            }
                        })
                        .create(),
                SchemaTables.Table.<TableAction>build("member")
                        .action(new TableAction() {
                            @Override
                            public void onAction(List<? extends RowChangedData> changedData) {
                                //member表改动对应的处理
                                //do some work or call some function~~~
                            }
                        })
                        .create());

        //canal实例名称
        String instanceName = "legend-table-section";
        TableSectionHandle tableSectionHandle = new TableSectionHandle(LOCAL_ADDRESS, instanceName, schemaTablesBuilder.create());

        //下面是异常处理以及canal连接的一些参数设置, 都有默认值, 需要设置之~~~
        tableSectionHandle.setHandleExceptionFunction(new Function<HandleExceptionContext, Boolean>() {
            @Override
            public Boolean apply(HandleExceptionContext input) {
                //do some exception handle
                return false;
            }
        });
        tableSectionHandle.setUserLocalTableFilter(false);
        tableSectionHandle.setIgnoreHandleException(false);
        tableSectionHandle.setMessageBatchSize(1000);
        tableSectionHandle.setMessageTimeout(500L);

        INSTANCE.getInstance().addInstanceHandle(tableSectionHandle);

    }

    /**
     * 添加{@link EventTypeSectionHandle}
     */
    private void addEventTypeSectionCanalInstance() {
        SchemaTables.Builder<EventTypeAction> schemaTablesBuilder = SchemaTables.builder();
        List<SchemaTables.Table<EventTypeAction>> tableList = new ArrayList<>();

        //添加legend表中的legend_shop和legend_shop_service_info表处理
        tableList.add(SchemaTables.Table.<EventTypeAction>build("legend_shop")
                .action(new EventTypeAction() {
                    //legend-shop表改动对应的处理
                    @Override
                    public void onUpdateAction(List<RowChangedData.Update> updatedData) {
                        //do some work or call some function~~~
                    }

                    @Override
                    public void onInsertAction(List<RowChangedData.Insert> insertedData) {
                        //do some work or call some function~~~
                    }

                    @Override
                    public void onDeleteAction(List<RowChangedData.Delete> deletedData) {
                        //do some work or call some function~~~
                    }
                })
                .create()
        );
        tableList.add(SchemaTables.Table.<EventTypeAction>build("legend_shop_service_info")
                .action(new EventTypeAction() {
                    //legend_shop_service_info表改动对应的处理

                    @Override
                    public void onUpdateAction(List<RowChangedData.Update> updatedData) {
                        //do some work or call some function~~~
                    }

                    @Override
                    public void onInsertAction(List<RowChangedData.Insert> insertedData) {
                        //do some work or call some function~~~
                    }

                    @Override
                    public void onDeleteAction(List<RowChangedData.Delete> deletedData) {
                        //do some work or call some function~~~
                    }
                })
                .columns("id", "is_deleted", "name", "service_sn") //目前还不支持列过滤~~~不过很快了~~~
                .create()
        );
        String schemaName = "legend";
        schemaTablesBuilder.add(schemaName, tableList);

        //添加dandelion表中的activity, member表处理
        schemaName = "dandelion";
        schemaTablesBuilder.add(schemaName,
                SchemaTables.Table.<EventTypeAction>build("activity")
                        .action(new EventTypeAction() {
                            //activity表改动对应的处理

                            @Override
                            public void onUpdateAction(List<RowChangedData.Update> updatedData) {
                                //do some work or call some function~~~
                            }

                            @Override
                            public void onInsertAction(List<RowChangedData.Insert> insertedData) {
                                //do some work or call some function~~~
                            }

                            @Override
                            public void onDeleteAction(List<RowChangedData.Delete> deletedData) {
                                //do some work or call some function~~~
                            }
                        })
                        .create(),
                SchemaTables.Table.<EventTypeAction>build("member")
                        .action(new EventTypeAction() {
                            //member表改动对应的处理

                            @Override
                            public void onUpdateAction(List<RowChangedData.Update> updatedData) {
                                //do some work or call some function~~~
                            }

                            @Override
                            public void onInsertAction(List<RowChangedData.Insert> insertedData) {
                                //do some work or call some function~~~
                            }

                            @Override
                            public void onDeleteAction(List<RowChangedData.Delete> deletedData) {
                                //do some work or call some function~~~
                            }
                            //member表改动对应的处理
                        })
                        .create());

        //canal实例名称
        String instanceName = "legend-event-type-section";
        EventTypeSectionHandle eventTypeSectionHandle = new EventTypeSectionHandle(LOCAL_ADDRESS, instanceName, schemaTablesBuilder.create());

        //下面是异常处理以及canal连接的一些参数设置, 都有默认值, 需要设置之~~~
        eventTypeSectionHandle.setHandleExceptionFunction(new Function<HandleExceptionContext, Boolean>() {
            @Override
            public Boolean apply(HandleExceptionContext input) {
                //do some exception handle
                return false;
            }
        });
        eventTypeSectionHandle.setUserLocalTableFilter(false);
        eventTypeSectionHandle.setIgnoreHandleException(false);
        eventTypeSectionHandle.setMessageBatchSize(1000);
        eventTypeSectionHandle.setMessageTimeout(500L);

        INSTANCE.getInstance().addInstanceHandle(eventTypeSectionHandle);
    }

    private static final long START_TIMESTAMP = System.currentTimeMillis();

    /**
     * 开启实例
     */
    private void startInstance() {
        //启动指定的canalInstance
        INSTANCE.getInstance().startInstance("legend-table-section");

        //启动所有的canalInstance
        for (String instanceName : INSTANCE.getInstance().allCanalInstance()) {
            INSTANCE.getInstance().startInstance(instanceName);
        }

        //停止Instance
        for (String instanceName : INSTANCE.getInstance().allCanalInstance()) {
            INSTANCE.getInstance().stopInstance(instanceName);
        }

        //指定开始时间启动
        //启动所有的canalInstance
        for (String instanceName : INSTANCE.getInstance().allCanalInstance()) {
            INSTANCE.getInstance().startInstance(instanceName, START_TIMESTAMP);
        }
    }

}
