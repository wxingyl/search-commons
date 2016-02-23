package com.tqmall.search.canal;

import com.alibaba.otter.canal.protocol.CanalEntry;
import com.alibaba.otter.canal.protocol.Message;
import com.google.protobuf.InvalidProtocolBufferException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * Created by xing on 16/2/22.
 * 执行具体的{@link CanalInstanceHandle}, 一个canalInstance占用一个单独线程
 * 该类建议单例执行
 */
public class CanalExecutor {

    private static final Logger log = LoggerFactory.getLogger(CanalExecutor.class);

    private final ThreadFactory threadFactory;

    private final ReadWriteLock lock = new ReentrantReadWriteLock();

    private Map<String, CanalInstance> canalInstanceMap = new HashMap<>();

    /**
     * 默认使用{@link Executors#defaultThreadFactory()}
     */
    public CanalExecutor() {
        this(Executors.defaultThreadFactory());
    }

    public CanalExecutor(ThreadFactory threadFactory) {
        this.threadFactory = threadFactory;
    }

    /**
     * 指定canal实例是否在运行
     *
     * @param instanceName 实例名称
     */
    public boolean isRunning(String instanceName) {
        lock.readLock().lock();
        try {
            CanalInstance instance = canalInstanceMap.get(instanceName);
            return instance != null && instance.running;
        } finally {
            lock.readLock().unlock();
        }
    }

    /**
     * 获取当前所有的canal实例名称, 该接口只是方便使用加的, 一般在系统关闭的时候需要批量{@link #stopInstance(String)}使用
     *
     * @return canalInstance name 数组
     */
    public String[] allCanalInstance() {
        lock.readLock().lock();
        try {
            return canalInstanceMap.keySet().toArray(new String[canalInstanceMap.size()]);
        } finally {
            lock.readLock().unlock();
        }
    }

    /**
     * 新添加canal实例处理对象, 如果原先添加过, 则被覆盖~~~
     *
     * @param handle 实例处理对象
     */
    public void addInstanceHandle(CanalInstanceHandle handle) {
        lock.writeLock().lock();
        try {
            canalInstanceMap.put(handle.instanceName(), new CanalInstance(handle));
        } finally {
            lock.writeLock().unlock();
        }
    }

    /**
     * 以上次停止的时间点, 开始监听数据更新, 也就是startRtTime不起作用
     *
     * @param instanceName 实例名
     */
    public void startInstance(String instanceName) {
        startInstance(instanceName, 0L);
    }

    /**
     * 启动指定的canal实例
     * 每次启动都是从
     *
     * @param instanceName 实例名
     * @param startRtTime  处理实时数据变化的起始时间点, 为0则从canal服务器记录的上次更新点获取Message
     */
    public void startInstance(String instanceName, long startRtTime) {
        lock.writeLock().lock();
        try {
            CanalInstance instance = canalInstanceMap.get(instanceName);
            if (instance == null || instance.running) {
                log.warn("canal instance " + instanceName + " is not exist or running: " + instance);
                return;
            }
            Thread thread = threadFactory.newThread(instance);
//            instance.t = thread;
            instance.startRtTime = startRtTime;
            thread.start();
        } finally {
            lock.writeLock().unlock();
        }
    }

    /**
     * 停止指定的canal实例
     *
     * @param instanceName 实例名
     */
    public void stopInstance(String instanceName) {
        lock.writeLock().lock();
        try {
            CanalInstance instance = canalInstanceMap.get(instanceName);
            if (instance != null && instance.running) {
                instance.running = false;
            }
        } finally {
            lock.writeLock().unlock();
        }
    }

    /**
     * 一个canal实例
     */
    static class CanalInstance implements Runnable {

        final CanalInstanceHandle handle;
        /**
         * 标识运行状态
         */
        volatile boolean running;
        /**
         * 处理实时数据变化的起始时间点, 为0则从canal服务器记录的上次更新点获取Message
         */
        volatile long startRtTime;

        /**
         * 待处理数据集合列表, 供{@link CanalInstanceHandle#rowChangeHandle(String, String, List)}使用
         * 只能本线程访问, 线程不安全的
         */
        private List<RowChangedData> rowChangedDataList = new LinkedList<>();

        /**
         * 最近处理的schema
         * 只能本线程访问, 线程不安全的
         */
        private String lastSchema;

        /**
         * 最近处理的table
         * 只能本线程访问, 线程不安全的
         */
        private String lastTable;

        /**
         * 最近处理的tableEvent
         * 只能本线程访问, 线程不安全的
         */
        private CanalEntry.EventType lastEventType;

        /**
         * 该实例运行的线程对象, 先留着, 后面说不定有用
         */
//        private Thread t;
        public CanalInstance(CanalInstanceHandle handle) {
            this.handle = handle;
        }

        private void runHandleRowChange() {
            if (rowChangedDataList.isEmpty()) return;
            try {
                handle.rowChangeHandle(lastSchema, lastTable, rowChangedDataList);
            } catch (RuntimeException e) {
                if (!handle.exceptionHandle(HandleExceptionContext.build(e)
                        .schema(lastSchema)
                        .table(lastTable)
                        .eventType(lastEventType)
                        .changedData(rowChangedDataList)
                        .create())) {
                    //不忽略异常, 那砸门就抛出去, 停止canal监听
                    throw e;
                }
            } finally {
                rowChangedDataList.clear();
            }
        }

        /**
         * 不断从canal server获取数据
         */
        @Override
        public void run() {
            log.info("start launching canalInstance: " + handle.instanceName());
            handle.connect();
            running = true;
            //开始之前先灭掉
            lastSchema = null;
            lastTable = null;
            lastEventType = null;
            long lastBatchId = 0L;
            try {
                while (running) {
                    Message message = handle.getWithoutAck();
                    lastBatchId = message.getId();
                    if (message.getId() <= 0 || message.getEntries().isEmpty()) continue;
                    for (CanalEntry.Entry e : message.getEntries()) {
                        if (e.getEntryType() != CanalEntry.EntryType.ROWDATA || !e.hasStoreValue()) continue;
                        CanalEntry.Header header = e.getHeader();
                        if (header.getExecuteTime() < startRtTime
                                || header.getEventType().getNumber() > CanalEntry.EventType.DELETE_VALUE) continue;
                        try {
                            CanalEntry.RowChange rowChange = CanalEntry.RowChange.parseFrom(e.getStoreValue());
                            if (rowChange.getIsDdl()) continue;
                            //尽量集中处理
                            if (!header.getTableName().equals(lastTable) || !header.getSchemaName().equals(lastSchema)
                                    || !header.getEventType().equals(lastEventType)) {
                                runHandleRowChange();
                                lastSchema = header.getSchemaName();
                                lastTable = header.getTableName();
                                lastEventType = header.getEventType();
                            }
                            rowChangedDataList.addAll(RowChangedData.build(rowChange));
                        } catch (InvalidProtocolBufferException e1) {
                            log.error("canal instance: " + handle.instanceName() + " parse store value have exception: ", e1);
                        }
                    }
                    runHandleRowChange();
                    handle.ack(lastBatchId);
                }
            } catch (RuntimeException e) {
                running = false;
                log.error("canalInstance: " + handle.instanceName() + " occurring a serious RuntimeException and lead to stop this canalInstance", e);
                //既然处理失败了, 那就回滚呗~~~
                handle.rollback(lastBatchId);
            } finally {
                handle.disConnect();
                //以防万一~~~
                rowChangedDataList.clear();
            }
            log.info("canalInstance: " + handle.instanceName() + " has stopped");
        }

        @Override
        public String toString() {
            return "CanalInstance{" + handle.instanceName() + ", running=" + running + "startRtTime=" + startRtTime + '}';
        }
    }
}
