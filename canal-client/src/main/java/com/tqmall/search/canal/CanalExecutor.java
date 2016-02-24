package com.tqmall.search.canal;

import com.alibaba.otter.canal.protocol.CanalEntry;
import com.alibaba.otter.canal.protocol.Message;
import com.google.protobuf.InvalidProtocolBufferException;
import com.tqmall.search.canal.handle.CanalInstanceHandle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * Created by xing on 16/2/22.
 * 执行具体的{@link CanalInstanceHandle}, 一个canalInstance占用一个单独线程
 * 该类实例通过{@link Runtime#addShutdownHook(Thread)}添加的jvm退出回调hook, jvm退出时主动停止每个运行的canalInstance
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
        //jvm退出时执行hook
        Runtime.getRuntime().addShutdownHook(threadFactory.newThread(new Runnable() {
            @Override
            public void run() {
                lock.writeLock().lock();
                log.info("run shutdown, going to stop all running canalInstances");
                try {
                    List<CanalInstance> needStoppedInstances = new ArrayList<>();
                    for (Map.Entry<String, CanalInstance> e : canalInstanceMap.entrySet()) {
                        if (e.getValue().running) {
                            log.warn("shutdown canal instance " + e.getKey());
                            e.getValue().running = false;
                            needStoppedInstances.add(e.getValue());
                        }
                    }
                    for (; ; ) {
                        boolean needContinue = false;
                        for (CanalInstance c : needStoppedInstances) {
                            if (!c.exited) {
                                needContinue = true;
                                break;
                            }
                        }
                        if (!needContinue) break;
                    }
                    log.info("run shutdown finish, all canalInstances have been stopped");
                } finally {
                    lock.writeLock().unlock();
                }
            }
        }));
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
     * 每次启动都是从{@link #threadFactory}获取新的线程启动, 并且启动时会等待待启动线程执行到{@link CanalInstance#run()}方法里面之后再退出
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
            for (; ; ) {
                //这儿需要等待线程启动完成
                if (!instance.exited) {
                    return;
                }
            }
        } finally {
            lock.writeLock().unlock();
        }
    }

    /**
     * 停止指定的canal实例
     * 停止时会等待canal运行线程退出{@link CanalInstance#run()}方法, 即{@link CanalInstance#exited} 为true才退出
     *
     * @param instanceName 实例名
     */
    public void stopInstance(String instanceName) {
        lock.writeLock().lock();
        try {
            CanalInstance instance = canalInstanceMap.get(instanceName);
            if (instance != null && instance.running) {
                instance.running = false;
                for (; ; ) {
                    if (instance.exited) return;
                }
            }
        } finally {
            lock.writeLock().unlock();
        }
    }

    /**
     * 一个canal实例
     */
    static class CanalInstance implements Runnable {

        private final CanalInstanceHandle handle;
        /**
         * 标识运行状态
         */
        volatile boolean running;
        /**
         * 处理实时数据变化的起始时间点, 为0则从canal服务器记录的上次更新点获取Message
         */
        volatile long startRtTime;
        /**
         * 是否完全退出, 即{@link #run()}执行完成
         */
        volatile boolean exited;

        /**
         * 该实例运行的线程对象, 先留着, 后面说不定有用
         */
//        private Thread t;
        public CanalInstance(CanalInstanceHandle handle) {
            this.handle = handle;
        }

        /**
         * 不断从canal server获取数据
         */
        @Override
        public void run() {
            log.info("start launching canalInstance: " + handle.instanceName());
            handle.connect();
            //下面2条代码的顺便不要随意更换~~~
            running = true;
            exited = false;
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
                                || header.getEventType().getNumber() > CanalEntry.EventType.DELETE_VALUE
                                || !handle.headerFilter(header)) continue;
                        try {
                            CanalEntry.RowChange rowChange = CanalEntry.RowChange.parseFrom(e.getStoreValue());
                            if (rowChange.getIsDdl()) continue;
                            handle.rowChangeHandle(header, rowChange);
                        } catch (InvalidProtocolBufferException e1) {
                            log.error("canal instance: " + handle.instanceName() + " parse store value have exception: ", e1);
                        }
                    }
                    handle.finishHandle();
                    handle.ack(lastBatchId);
                }
            } catch (RuntimeException e) {
                running = false;
                log.error("canalInstance: " + handle.instanceName() + " occurring a serious RuntimeException and lead to stop this canalInstance", e);
                //既然处理失败了, 那就回滚呗~~~
                handle.rollback(lastBatchId);
            } finally {
                exited = true;
                handle.disConnect();
            }
            log.info("canalInstance: " + handle.instanceName() + " has stopped");
        }

        @Override
        public String toString() {
            return "CanalInstance{" + handle.instanceName() + ", running=" + running + "startRtTime=" + startRtTime + '}';
        }
    }
}
