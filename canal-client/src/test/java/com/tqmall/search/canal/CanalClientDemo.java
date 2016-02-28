package com.tqmall.search.canal;

import com.alibaba.otter.canal.common.utils.AddressUtils;
import com.tqmall.search.canal.action.*;
import com.tqmall.search.canal.handle.*;
import com.tqmall.search.commons.lang.Function;
import com.tqmall.search.commons.param.condition.EqualCondition;
import com.tqmall.search.commons.param.condition.RangeCondition;
import org.junit.Test;

import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Created by xing on 16/2/24.
 * canal client 使用demo
 *
 * @see #addInstanceSectionCanalInstance()
 * @see #addTableSectionCanalInstance()
 * @see #addEventTypeSectionCanalInstance()
 * @see #startInstance()
 */
public class CanalClientDemo {
    /**
     * 最好单例~~~
     */
    private final static CanalExecutor CANAL_EXECUTOR = new CanalExecutor();

    /**
     * canal server 端口, 默认是11111
     */
    private final static int CANAL_PORT = 11111;

    /**
     * canal server ip地址, 这儿就直接取本地了
     */
    private final static SocketAddress LOCAL_ADDRESS = new InetSocketAddress(AddressUtils.getHostAddress(), CANAL_PORT);

    @Test
    public void runCanalInstanceTest() {
        ActionFactory<TableAction> actionFactory = new SingleSchemaActionFactory<>(Schemas.<TableAction>buildSchema("autoparts")
                .addTable(Schemas.buildTable("db_goods_stock")
                        .action(new AbstractTableAction(new SingleThreadCurrentHandleTable<TableAction>()) {
                            @Override
                            public void onAction(List<? extends RowChangedData> changedData) {
                                System.out.println("currentTable: " + getCurrentTable());
                                System.out.println(changedData);
                            }
                        })
                        .columns("id", "goods_id", "goods_number")
                        .columnCondition(TableColumnCondition.DEFAULT_DELETE_COLUMN_CONDITION))
                .create());
        CANAL_EXECUTOR.addInstanceHandle(new TableSectionHandle(LOCAL_ADDRESS, "shop", actionFactory));
        ActionFactory<EventTypeAction> eventTypeFactory = new SingleSchemaActionFactory<>(Schemas.<EventTypeAction>buildSchema("autoparts")
                .addTable(Schemas.buildTable("db_goods")
                        .action(new EventTypeAction() {
                            @Override
                            public void onUpdateAction(List<RowChangedData.Update> updatedData) {
                                System.out.println("db_goods.onUpdateAction: " + updatedData);
                            }

                            @Override
                            public void onInsertAction(List<RowChangedData.Insert> insertedData) {
                                System.out.println("db_goods.onInsertAction: " + insertedData);
                            }

                            @Override
                            public void onDeleteAction(List<RowChangedData.Delete> deletedData) {
                                System.out.println("db_goods.onDeleteAction: " + deletedData);
                            }
                        })
                        .columns("goods_id", "goods_name", "cat_id", "new_goods_sn")
                        .columnCondition(TableColumnCondition.build()
                                .condition(EqualCondition.build("is_delete", false), Boolean.TYPE)
                                .condition(EqualCondition.build("seller_id", 1), Integer.TYPE)
                                .create()))
                .create());
        CANAL_EXECUTOR.addInstanceHandle(new EventTypeSectionHandle(LOCAL_ADDRESS, "shop_goods", eventTypeFactory));
        CANAL_EXECUTOR.startAllInstance(0L);
        try {
            TimeUnit.MINUTES.sleep(100);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

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

        CANAL_EXECUTOR.addInstanceHandle(instanceSectionHandle);
    }

    /**
     * 添加{@link TableSectionHandle}
     */
    private void addTableSectionCanalInstance() {

        List<Schema<TableAction>> schemas = new ArrayList<>();

        List<Schemas.TableBuilder> tableList = new ArrayList<>();
        //添加legend表中的legend_shop和legend_shop_service_info表处理
        tableList.add(Schemas.buildTable("legend_shop")
                .action(new TableAction() {
                    @Override
                    public void onAction(List<? extends RowChangedData> changedData) {
                        //legend-shop表改动对应的处理
                        //do some work or call some function~~~
                    }
                })
                .columnCondition(TableColumnCondition.DEFAULT_DELETE_COLUMN_CONDITION)
        );

        tableList.add(Schemas.buildTable("legend_shop_service_info")
                .action(new TableAction() {
                    @Override
                    public void onAction(List<? extends RowChangedData> changedData) {
                        //legend_shop_service_info表改动对应的处理
                        //do some work or call some function~~~
                    }
                })
                .columns("id", "is_deleted", "name", "service_sn") //目前还不支持列过滤~~~不过很快了~~~
                //id 取值返回在[10, 100], 并且is_deleted = 'N'
                .columnCondition(TableColumnCondition.build()
                        .condition(RangeCondition.build("id", 10, 100), Integer.class)
                        .condition(TableColumnCondition.NOT_DELETED_CONDITION, Boolean.class)
                        .create())
        );
        String schemaName = "legend";
        schemas.add(Schemas.<TableAction>buildSchema(schemaName)
                .addTable(tableList)
                .create());

        //添加dandelion表中的activity, member表处理
        schemaName = "dandelion";
        schemas.add(Schemas.<TableAction>buildSchema(schemaName)
                .addTable(Schemas.buildTable("activity")
                        .columnCondition(TableColumnCondition.DEFAULT_DELETE_COLUMN_CONDITION)
                        .action(new TableAction() {
                            @Override
                            public void onAction(List<? extends RowChangedData> changedData) {
                                //activity表改动对应的处理
                                //do some work or call some function~~~
                            }
                        }), Schemas.buildTable("member")
                        .columnCondition(TableColumnCondition.DEFAULT_DELETE_COLUMN_CONDITION)
                        .action(new TableAction() {
                            @Override
                            public void onAction(List<? extends RowChangedData> changedData) {
                                //member表改动对应的处理
                                //do some work or call some function~~~
                            }
                        }))
                .create());

        //canal实例名称
        String instanceName = "legend-table-section";
        TableSectionHandle tableSectionHandle = new TableSectionHandle(LOCAL_ADDRESS, instanceName, new MultiSchemaActionFactory<>(schemas));

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

        CANAL_EXECUTOR.addInstanceHandle(tableSectionHandle);

    }

    /**
     * 添加{@link EventTypeSectionHandle}
     */
    private void addEventTypeSectionCanalInstance() {
        ActionFactory<EventTypeAction> actionFactory = Schemas.<EventTypeAction>buildFactory()
                //添加legend表中的legend_shop和legend_shop_service_info表处理
                .addSchema(Schemas.<EventTypeAction>buildSchema("legend")
                        .addTable(Schemas.buildTable("legend_shop")
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
                                .columns("id", "shop_id")
                                .columnCondition(TableColumnCondition.DEFAULT_DELETE_COLUMN_CONDITION))
                        .addTable(Schemas.buildTable("legend_shop_service_info")
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
                                .columnCondition(TableColumnCondition.DEFAULT_DELETE_COLUMN_CONDITION)))
                //添加dandelion表中的activity, member表处理
                .addSchema(Schemas.<EventTypeAction>buildSchema("dandelion")
                        .addTable(Schemas.buildTable("activity")
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
                                }))
                        .addTable(Schemas.buildTable("member")
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
                                })))
                .create();

        //canal实例名称
        String instanceName = "legend-event-type-section";
        EventTypeSectionHandle eventTypeSectionHandle = new EventTypeSectionHandle(LOCAL_ADDRESS, instanceName, actionFactory);

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

        CANAL_EXECUTOR.addInstanceHandle(eventTypeSectionHandle);
    }

    private static final long START_TIMESTAMP = System.currentTimeMillis();

    /**
     * 开启实例
     */
    private void startInstance() {
        //启动指定的canalInstance
        CANAL_EXECUTOR.startInstance("legend-table-section");

        //启动所有的canalInstance
        for (String instanceName : CANAL_EXECUTOR.allCanalInstance()) {
            CANAL_EXECUTOR.startInstance(instanceName);
        }

        //停止Instance
        for (String instanceName : CANAL_EXECUTOR.allCanalInstance()) {
            CANAL_EXECUTOR.stopInstance(instanceName);
        }

        //指定开始时间启动
        //启动所有的canalInstance
        for (String instanceName : CANAL_EXECUTOR.allCanalInstance()) {
            CANAL_EXECUTOR.startInstance(instanceName, START_TIMESTAMP);
        }
    }

}
