package com.tqmall.search.canal;

import com.alibaba.otter.canal.client.CanalConnector;
import com.alibaba.otter.canal.client.CanalConnectors;
import com.alibaba.otter.canal.protocol.CanalEntry;
import com.tqmall.search.canal.action.*;
import com.tqmall.search.canal.handle.CanalInstanceHandle;
import com.tqmall.search.commons.condition.ConditionContainer;
import com.tqmall.search.commons.condition.Conditions;
import com.tqmall.search.commons.condition.EqualCondition;
import com.tqmall.search.commons.utils.CommonsUtils;
import com.tqmall.search.commons.utils.SearchStringUtils;

import java.net.SocketAddress;
import java.util.*;

/**
 * Created by xing on 16/2/24.
 * {@link Schema} 构造工具类
 */
public final class Schemas {

    private Schemas() {
    }

    /**
     * 字段名: "is_deleted"
     * 有效值: false
     *
     * @see com.tqmall.search.commons.utils.StrValueConverts.BoolStrValueConvert
     */
    public static final EqualCondition<Boolean> NOT_DELETED_CONDITION = Conditions.equal("is_deleted", false);

    /**
     * 默认的逻辑删除表字段过滤器
     */
    public static final ConditionContainer DEFAULT_DELETE_COLUMN_CONDITION = Conditions.unmodifiableContainer()
            .mustCondition(NOT_DELETED_CONDITION)
            .create();

    /**
     * 方便创建{@link MultiSchemaActionFactory}对象搞的
     */
    public static <T extends Actionable> ActionFactoryBuilder<T> buildFactory() {
        return new ActionFactoryBuilder<>();
    }

    public static <T extends Actionable> Builder<T> buildSchema(String schemaName, Class<T> actionType) {
        return new Builder<>(schemaName, actionType);
    }

    public static TableBuilder buildTable(String tableName) {
        return new TableBuilder(tableName);
    }

    public static CanalInstanceHandle.ConnectorFactory singleConnector(SocketAddress address) {
        return new SingleConnectorFactory(address);
    }

    public static CanalInstanceHandle.ConnectorFactory clusterConnector(List<SocketAddress> addresses) {
        return new ClusterConnectorFactory(addresses);
    }

    public static CanalInstanceHandle.ConnectorFactory zkClusterConnector(String zkServers) {
        return new ZkClusterConnectorFactory(zkServers);
    }

    static class SingleConnectorFactory implements CanalInstanceHandle.ConnectorFactory {

        private final SocketAddress address;

        SingleConnectorFactory(SocketAddress address) {
            Objects.requireNonNull(address);
            this.address = address;
        }

        @Override
        public CanalConnector create(String instanceName) {
            return CanalConnectors.newSingleConnector(address, instanceName, null, null);
        }
    }

    static class ClusterConnectorFactory implements CanalInstanceHandle.ConnectorFactory {

        private final List<SocketAddress> addresses;

        ClusterConnectorFactory(List<SocketAddress> addresses) {
            if (CommonsUtils.isEmpty(addresses)) {
                throw new IllegalArgumentException("addresses can not empty");
            }
            this.addresses = addresses;
        }

        @Override
        public CanalConnector create(String instanceName) {
            return CanalConnectors.newClusterConnector(addresses, instanceName, null, null);
        }
    }

    static class ZkClusterConnectorFactory implements CanalInstanceHandle.ConnectorFactory {

        private final String zkServers;

        ZkClusterConnectorFactory(String zkServers) {
            if (SearchStringUtils.isEmpty(zkServers)) {
                throw new IllegalArgumentException("zkServers string value is empty");
            }
            this.zkServers = zkServers;
        }

        @Override
        public CanalConnector create(String instanceName) {
            return CanalConnectors.newClusterConnector(zkServers, instanceName, null, null);
        }
    }

    /**
     * 方便创建{@link MultiSchemaActionFactory}对象搞的
     */
    public static class ActionFactoryBuilder<T extends Actionable> {

        private Map<String, Builder<T>> schemaBuilderMap = new HashMap<>();

        public final ActionFactoryBuilder<T> addSchema(Builder<T> schema) {
            schemaBuilderMap.put(schema.schemaName, schema);
            return this;
        }

        @SafeVarargs
        public final ActionFactoryBuilder<T> addSchema(Builder<T>... schemas) {
            for (Builder<T> b : schemas) {
                schemaBuilderMap.put(b.schemaName, b);
            }
            return this;
        }

        public ActionFactoryBuilder<T> addSchema(Collection<Builder<T>> schemas) {
            for (Builder<T> b : schemas) {
                schemaBuilderMap.put(b.schemaName, b);
            }
            return this;
        }

        public ActionFactory<T> create() {
            List<Schema<T>> schemaList = new ArrayList<>(schemaBuilderMap.size());
            for (Builder<T> b : schemaBuilderMap.values()) {
                schemaList.add(b.create());
            }
            if (schemaList.size() == 1) {
                return new SingleSchemaActionFactory<>(schemaList.get(0));
            } else {
                return new MultiSchemaActionFactory<>(schemaList);
            }
        }
    }

    /**
     * {@link Schema}构造器
     *
     * @param <T>
     */
    public static class Builder<T extends Actionable> {

        private final String schemaName;

        private final Map<String, TableBuilder> tableBuilderMap = new HashMap<>();

        private final Class<T> actionType;

        Builder(String schemaName, Class<T> actionType) {
            this.schemaName = schemaName;
            this.actionType = actionType;
        }

        /**
         * @see #buildTable(String)
         */
        public final Builder<T> addTable(TableBuilder tb) {
            Objects.requireNonNull(tb.action);
            if (actionType != null && !actionType.isInstance(tb.action)) {
                throw new IllegalArgumentException(schemaName + '.' + tb.tableName + " action: " + tb.action + " is not "
                        + actionType + " object");
            }
            tableBuilderMap.put(tb.tableName, tb);
            return this;
        }

        /**
         * @see #buildTable(String)
         */
        public final Builder<T> addTable(TableBuilder... tableBuilders) {
            for (TableBuilder tb : tableBuilders) {
                addTable(tb);
            }
            return this;
        }

        /**
         * @see #buildTable(String)
         */
        public Builder<T> addTable(Iterable<TableBuilder> tableBuilders) {
            for (TableBuilder tb : tableBuilders) {
                addTable(tb);
            }
            return this;
        }

        public Schema<T> create() {
            Schema<T> schema = new Schema<>(schemaName);
            for (TableBuilder tb : tableBuilderMap.values()) {
                schema.addTable(tb);
            }
            return schema;
        }
    }

    /**
     * 方便{@link Schema.Table}构造创建, 其初始化完成之后, 调用方法{@link Schema#addTable(TableBuilder)}, 作为入参添加到对应的{@link Schema},
     */
    public static class TableBuilder {
        String tableName;
        Actionable action;
        Set<String> columns = new HashSet<>();
        ConditionContainer columnCondition;
        byte forbidEventType;

        TableBuilder(String tableName) {
            this.tableName = tableName;
        }

        /**
         * @param action 必须是{@link TableAction} 或者 {@link EventTypeAction}对象
         */
        public TableBuilder action(Actionable action) {
            if (action instanceof TableAction || action instanceof EventTypeAction) {
                this.action = action;
                return this;
            } else {
                throw new IllegalArgumentException("action type must be instance " + TableAction.class + " or " + EventTypeAction.class);
            }
        }

        public TableBuilder columns(String... columns) {
            if (columns.length > 0) {
                Collections.addAll(this.columns, columns);
            }
            return this;
        }

        public TableBuilder columns(Collection<String> columns) {
            if (!CommonsUtils.isEmpty(columns)) {
                this.columns.addAll(columns);
            }
            return this;
        }

        public TableBuilder columnCondition(ConditionContainer columnCondition) {
            this.columnCondition = columnCondition;
            return this;
        }

        /**
         * 添加需要排除的事件类型
         */
        public TableBuilder forbidEventType(CanalEntry.EventType eventType) {
            forbidEventType |= RowChangedData.getEventTypeFlag(eventType);
            return this;
        }
    }

}
