package com.tqmall.search.db;

import com.tqmall.search.commons.condition.ConditionContainer;
import com.tqmall.search.commons.condition.Conditions;
import com.tqmall.search.db.param.*;
import org.apache.commons.dbutils.BasicRowProcessor;
import org.apache.commons.dbutils.RowProcessor;
import org.apache.commons.dbutils.handlers.*;

import java.lang.ref.WeakReference;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.sql.ResultSet;

/**
 * Created by xing on 16/4/5.
 * sql query 构造工具类
 *
 * @author xing
 */
public final class Queries {

    /**
     * 默认的只包含`is_deleted` = 'N' 的条件容器
     */
    public static final ConditionContainer DEFAULT_DELETED_CONTAINER = Conditions.unmodifiableContainer()
            .mustCondition(Conditions.equal("is_deleted", "N"))
            .create();

    /**
     * 默认的{@link RowProcessor}对象
     * {@link ResultSet}转为Bean时使用{@link MysqlBeanProcessor}
     *
     * @see MysqlBeanProcessor
     */
    private static final RowProcessor DEFAULT_ROW_PROCESSOR = new BasicRowProcessor(new MysqlBeanProcessor());

    private static final Queries INSTANCE = new Queries();

    private final ArrayHandler arrayHandler = new ArrayHandler();

    private final ArrayListHandler arrayListHandler = new ArrayListHandler();

    private final MapHandler mapHandler = new MapHandler();

    private final MapListHandler mapListHandler = new MapListHandler();

    private volatile WeakReference<Constructor<BeanMapHandler>> beanMapHandleConstructor;

    private Queries() {
    }

    private Constructor<BeanMapHandler> getBeanMapHandleConstructor() {
        WeakReference<Constructor<BeanMapHandler>> beanMapHandleConstructor = this.beanMapHandleConstructor;
        if (beanMapHandleConstructor != null && beanMapHandleConstructor.get() != null) {
            return beanMapHandleConstructor.get();
        } else {
            try {
                Constructor<BeanMapHandler> constructor = BeanMapHandler.class.getDeclaredConstructor(Class.class, RowProcessor.class, Integer.TYPE, String.class);
                constructor.setAccessible(true);
                this.beanMapHandleConstructor = new WeakReference<>(constructor);
                return constructor;
            } catch (NoSuchMethodException e) {
                throw new IllegalStateException("get " + BeanMapHandler.class + " constructor have exception", e);
            }
        }
    }

    /**
     * 没办法, 通过反射创建对象吧
     * 创建失败抛出{@link IllegalStateException}
     */
    @SuppressWarnings({"rawstypes", "unchecked"})
    private <K, V> BeanMapHandler<K, V> newBeanMapHandlerInstance(Class<V> cls, int columnIndex, String columnName) {
        try {
            return getBeanMapHandleConstructor().newInstance(cls, DEFAULT_ROW_PROCESSOR, columnIndex, columnName);
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException e) {
            throw new IllegalStateException("create BeanMapHandler object by invoke instance have exception", e);
        }
    }

    public static AllQueryParam allParam(String table) {
        return new AllQueryParam(null, table, QueryParam.DEFAULT_SIZE);
    }

    public static AllQueryParam allParam(String schema, String table) {
        return new AllQueryParam(schema, table, QueryParam.DEFAULT_SIZE);
    }

    public static AllQueryParam allParam(String table, int size) {
        return new AllQueryParam(null, table, size);
    }

    public static AllQueryParam allParam(String schema, String table, int size) {
        return new AllQueryParam(schema, table, size);
    }

    public static ColumnsQueryParam columnsParam(String table) {
        return new ColumnsQueryParam(null, table, QueryParam.DEFAULT_SIZE);
    }

    public static ColumnsQueryParam columnsParam(String schema, String table) {
        return new ColumnsQueryParam(schema, table, QueryParam.DEFAULT_SIZE);
    }

    public static ColumnsQueryParam columnsParam(String table, int size) {
        return new ColumnsQueryParam(null, table, size);
    }

    public static ColumnsQueryParam columnsParam(String schema, String table, int size) {
        return new ColumnsQueryParam(schema, table, size);
    }

    public static <T> BeanQueryParam<T> beanParam(String table, Class<T> cls) {
        return new BeanQueryParam<>(null, table, QueryParam.DEFAULT_SIZE, cls);
    }

    public static <T> BeanQueryParam<T> beanParam(String schema, String table, Class<T> cls) {
        return new BeanQueryParam<>(schema, table, QueryParam.DEFAULT_SIZE, cls);
    }

    public static <T> BeanQueryParam<T> beanParam(String table, int size, Class<T> cls) {
        return new BeanQueryParam<>(null, table, size, cls);
    }

    public static <T> BeanQueryParam<T> beanParam(String schema, String table, int size, Class<T> cls) {
        return new BeanQueryParam<>(schema, table, size, cls);
    }

    public static SqlSentenceQueryParam sqlSentenceParam(String table, String sqlSentence) {
        return new SqlSentenceQueryParam(null, table, QueryParam.DEFAULT_SIZE, sqlSentence);
    }

    public static SqlSentenceQueryParam sqlSentenceParam(String schema, String table, int size, String sqlSentence) {
        return new SqlSentenceQueryParam(schema, table, size, sqlSentence);
    }

    /**
     * sql返回结果重名民为total
     */
    public static SqlSentenceQueryParam countParam(String table) {
        return new SqlSentenceQueryParam(null, table, -1, "COUNT(1) as total");
    }

    /**
     * sql返回结果重名民为total
     */
    public static SqlSentenceQueryParam countParam(String schema, String table) {
        return new SqlSentenceQueryParam(schema, table, -1, "COUNT(1) as total");
    }

    /**
     * 默认的{@link RowProcessor}对象
     * {@link ResultSet}转为Bean时使用{@link MysqlBeanProcessor}
     *
     * @see MysqlBeanProcessor
     */
    public static RowProcessor defaultRowProcessor() {
        return DEFAULT_ROW_PROCESSOR;
    }

    /**
     * 查询结果: 单个Bean
     *
     * @see #DEFAULT_ROW_PROCESSOR
     */
    public static <T> BeanHandler<T> beanHandler(Class<T> cls) {
        return new BeanHandler<>(cls, DEFAULT_ROW_PROCESSOR);
    }

    /**
     * 查询结果: List<Bean>
     *
     * @see #DEFAULT_ROW_PROCESSOR
     */
    public static <T> BeanListHandler<T> beanListHandler(Class<T> cls) {
        return new BeanListHandler<>(cls, DEFAULT_ROW_PROCESSOR);
    }

    /**
     * 查询结果: Map<K, Bean>
     *
     * @see #DEFAULT_ROW_PROCESSOR
     */
    public static <K, V> BeanMapHandler<K, V> beanMapHandler(Class<V> cls) {
        return new BeanMapHandler<>(cls, DEFAULT_ROW_PROCESSOR);
    }

    /**
     * 查询结果: Map<K, Bean>
     *
     * @see #DEFAULT_ROW_PROCESSOR
     */
    public static <K, V> BeanMapHandler<K, V> beanMapHandler(Class<V> cls, int columnIndex) {
        return INSTANCE.newBeanMapHandlerInstance(cls, columnIndex, null);
    }

    /**
     * 查询结果: Map<K, Bean>
     *
     * @see #DEFAULT_ROW_PROCESSOR
     */
    public static <K, V> BeanMapHandler<K, V> beanMapHandler(Class<V> cls, String columnName) {
        return INSTANCE.newBeanMapHandlerInstance(cls, 1, columnName);
    }

    /**
     * 查询结果: 单个对象 T, 一般都是基本类型了, 比如{@link String}, {@link Integer}, {@link java.util.Date}等
     */
    public static <T> ScalarHandler<T> scalarHandler(String columnName) {
        return new ScalarHandler<>(columnName);
    }

    /**
     * 查询结果: List<T>
     *
     * @param columnIndex 从1开始
     * @param <T>         基本数据类型, 比如 String, {@link java.math.BigDecimal}等
     */
    public static <T> ColumnListHandler<T> columnListHandler(int columnIndex) {
        return new ColumnListHandler<>(columnIndex);
    }

    /**
     * 查询结果: List<T>
     *
     * @param columnName 查询字段名
     * @param <T>        基本数据类型, 比如 String, {@link java.math.BigDecimal}等
     */
    public static <T> ColumnListHandler<T> columnListHandler(String columnName) {
        return new ColumnListHandler<>(columnName);
    }

    /**
     * 查询结果: Object[]
     */
    public static ArrayHandler arrayHandler() {
        return INSTANCE.arrayHandler;
    }

    /**
     * 查询结果: List<Object[]>
     */
    public static ArrayListHandler arrayListHandler() {
        return INSTANCE.arrayListHandler;
    }

    /**
     * 查询结果: Map<String, Object>
     * 查询单条记录, 以Map形式返回, K 字段名, V 对应值
     */
    public static MapHandler mapHandler() {
        return INSTANCE.mapHandler;
    }

    /**
     * 查询结果: List<Map<String, Object>>
     * 查询多条记录, 每条记录为Map, K 字段名, V 对应值
     */
    public static MapListHandler mapListHandler() {
        return INSTANCE.mapListHandler;
    }
}
