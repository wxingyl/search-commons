package com.tqmall.search.canal;

import com.alibaba.otter.canal.protocol.CanalEntry;
import com.alibaba.otter.canal.protocol.Message;
import com.alibaba.otter.canal.protocol.exception.CanalClientException;
import com.google.protobuf.InvalidProtocolBufferException;
import com.tqmall.search.canal.handle.CanalInstanceHandle;
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
 * 该类实例通过{@link Runtime#addShutdownHook(Thread)}添加的jvm退出回调hook, jvm退出时主动停止每个运行的canalInstance
 * 该类建议单例执行
 */
public class CanalExecutor {

    private static final Logger log = LoggerFactory.getLogger(CanalExecutor.class);

    private final ThreadFactory threadFactory;

    private final ReadWriteLock lock = new ReentrantReadWriteLock();

    private final Map<String, CanalInstance> canalInstanceMap = new HashMap<>();

    /**
     * 运行的canal 获取数据异常之后等待的时间, 单位ms, 默认2s
     */
    private long retryFetchInterval = 2000L;

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
                log.info("run shutdown, going to stop all running canalInstances");
                lock.writeLock().lock();
                try {
                    List<CanalInstance> needStoppedInstances = new ArrayList<>();
                    for (Map.Entry<String, CanalInstance> e : canalInstanceMap.entrySet()) {
                        if (e.getValue().runningSwitch) {
                            log.warn("shutdown canal instance " + e.getKey());
                            e.getValue().runningSwitch = false;
                            needStoppedInstances.add(e.getValue());
                        }
                    }
                    while (!needStoppedInstances.isEmpty()) {
                        Iterator<CanalInstance> it = needStoppedInstances.iterator();
                        while (it.hasNext()) {
                            CanalInstance c = it.next();
                            synchronized (c.lock) {
                                if (c.running) {
                                    try {
                                        //最起码我要等这个canalInstance执行结束, 那就等等吧
                                        c.lock.wait();
                                    } catch (InterruptedException e) {
                                        log.error("there is a exception when waiting canal: " + c.handle.instanceName() + " thread stop", e);
                                    }
                                } else {
                                    it.remove();
                                }
                            }
                        }
                    }
                    log.info("run shutdown finish, all canalInstances have been stopped");
                } finally {
                    lock.writeLock().unlock();
                }
            }
        }));
    }

    /**
     * 运行的canal 获取数据异常之后等待的时间
     *
     * @param retryFetchInterval 单位ms, 默认2s
     */
    public void setRetryFetchInterval(long retryFetchInterval) {
        this.retryFetchInterval = retryFetchInterval;
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
            return instance != null && instance.runningSwitch;
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
     * 新添加canal实例处理对象, 如果原先添加过, 并且当前没有正在运行, 则被覆盖, 如果当前实例正在运行, 则抛出{@link IllegalArgumentException}
     *
     * @param handle 实例处理对象
     */
    public void addInstanceHandle(CanalInstanceHandle handle) {
        lock.writeLock().lock();
        try {
            CanalInstance canalInstance = canalInstanceMap.get(handle.instanceName());
            if (canalInstance == null || !canalInstance.runningSwitch) {
                canalInstanceMap.put(handle.instanceName(), new CanalInstance(handle));
            } else {
                throw new IllegalArgumentException("CanalInstanceHandle " + handle.instanceName() + " is running, you should stop it first");
            }
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
        if (startRtTime < 0) {
            throw new IllegalArgumentException("zero is the min value of startRtTime");
        }
        lock.writeLock().lock();
        try {
            CanalInstance instance = canalInstanceMap.get(instanceName);
            if (instance == null || instance.runningSwitch) {
                log.warn("canal instance " + instanceName + " is not exist or running: " + instance);
                return;
            }
            Thread thread = threadFactory.newThread(instance);
            instance.startRtTime = startRtTime;
            thread.start();
            synchronized (instance.lock) {
                while (!instance.running) {
                    try {
                        instance.lock.wait();
                    } catch (InterruptedException e) {
                        log.error("start canal: " + instanceName + " have exception when waiting thread running", e);
                    }
                }
            }
        } finally {
            lock.writeLock().unlock();
        }
    }

    /**
     * 开启所有为开启的canalInstance
     *
     * @param startRtTime 处理实时数据变化的起始时间点, 为0则从canal服务器记录的上次更新点获取Message
     */
    public void startAllInstance(long startRtTime) {
        if (startRtTime < 0) {
            throw new IllegalArgumentException("zero is the min value of startRtTime");
        }
        lock.writeLock().lock();
        try {
            List<CanalInstance> startedInstances = new ArrayList<>();
            for (CanalInstance instance : canalInstanceMap.values()) {
                if (!instance.runningSwitch) {
                    Thread thread = threadFactory.newThread(instance);
                    instance.startRtTime = startRtTime;
                    thread.start();
                    startedInstances.add(instance);
                }
            }
            while (!startedInstances.isEmpty()) {
                Iterator<CanalInstance> it = startedInstances.iterator();
                while (it.hasNext()) {
                    CanalInstance c = it.next();
                    synchronized (c.lock) {
                        if (c.running) {
                            it.remove();
                        } else {
                            try {
                                //最起码我要等这个canalInstance执行结束, 那就等等吧
                                c.lock.wait();
                            } catch (InterruptedException e) {
                                log.error("there is a exception when starting canal: " + c.handle.instanceName() + " thread", e);
                            }
                        }
                    }
                }
            }
        } finally {
            lock.writeLock().unlock();
        }
    }

    /**
     * 停止指定的canal实例
     * 停止时会等待canal运行线程退出{@link CanalInstance#run()}方法, 即{@link CanalInstance#running} 为true才退出
     *
     * @param instanceName 实例名
     */
    public void stopInstance(String instanceName) {
        lock.writeLock().lock();
        try {
            CanalInstance instance = canalInstanceMap.get(instanceName);
            if (instance != null && instance.runningSwitch) {
                instance.runningSwitch = false;
                synchronized (instance.lock) {
                    while (instance.running) {
                        try {
                            instance.lock.wait();
                        } catch (InterruptedException e) {
                            log.error("stop canal: " + instanceName + " have exception when waiting thread stop", e);
                        }
                    }
                }
            }
        } finally {
            lock.writeLock().unlock();
        }
    }

    /**
     * 一个canal实例
     */
    private class CanalInstance implements Runnable {

        final CanalInstanceHandle handle;
        /**
         * 标识运行状态, 当前是否正在运行, 由变量{@link #running}标识
         */
        volatile boolean runningSwitch;
        /**
         * 处理实时数据变化的起始时间点, 为0则从canal服务器记录的上次更新点获取Message
         */
        volatile long startRtTime;

        final Object lock = new Object();
        /**
         * 是否还在执行, 即正在执行{@link #run()}
         * 该对象需要线程安全, 修改或者读取需要拿到锁{@link #lock}
         */
        boolean running;

        public CanalInstance(CanalInstanceHandle handle) {
            this.handle = handle;
        }

        /**
         * 当前链接出现异常, 重新链接~~~
         */
        private void reconnect() {
            boolean reconnectSucceed = false;
            while (!reconnectSucceed && runningSwitch) {
                try {
                    handle.disConnect();
                } catch (CanalClientException ignore) {
                }
                try {
                    Thread.sleep(retryFetchInterval);
                } catch (InterruptedException ignore) {
                }
                try {
                    handle.connect();
                    reconnectSucceed = true;
                } catch (CanalClientException rc) {
                    log.warn("canalInstance: " + handle.instanceName() + " reconnect server failed: " + rc.getMessage());
                }
            }
        }

        /**
         * 消费当前消息
         */
        private void consumerMessage(Message message) {
            try {
                for (CanalEntry.Entry e : message.getEntries()) {
                    if (e.getEntryType() != CanalEntry.EntryType.ROWDATA || !e.hasStoreValue()) continue;
                    CanalEntry.Header header = e.getHeader();
                    if (header.getExecuteTime() < startRtTime
                            || header.getEventType().getNumber() > CanalEntry.EventType.DELETE_VALUE
                            || !handle.startHandle(header)) continue;
                    try {
                        CanalEntry.RowChange rowChange = CanalEntry.RowChange.parseFrom(e.getStoreValue());
                        if (rowChange.getIsDdl()) continue;
                        handle.rowChangeHandle(rowChange);
                    } catch (InvalidProtocolBufferException e1) {
                        log.error("canal instance: " + handle.instanceName() + " parse store value have exception: ", e1);
                    }
                }
                handle.ack(message.getId());
            } finally {
                handle.finishMessageHandle();
            }
        }

        /**
         * 不断从canal server获取数据
         */
        @Override
        public void run() {
            synchronized (lock) {
                if (running) {
                    throw new IllegalStateException("canalInstance: " + handle.instanceName() + " has running, it must be have error");
                }
            }
            log.info("start launching canalInstance: " + handle.instanceName());
            //如果连接出现异常, 相关配置,变量还没有修改, 所以不用做任何处理,当前线程退出就行
            handle.connect();
            //下面2条代码的顺便不要随意更换~~~
            runningSwitch = true;
            synchronized (lock) {
                running = true;
                lock.notifyAll();
            }
            long lastBatchId = 0L;
            try {
                while (runningSwitch) {
                    Message message;
                    try {
                        message = handle.getWithoutAck();
                    } catch (CanalClientException e) {
                        log.error("read message from canalInstance: " + handle.instanceName() + " have exception, need wait "
                                + retryFetchInterval + "ms", e);
                        reconnect();
                        continue;
                    }
                    long nextFetchTime = System.currentTimeMillis() + handle.fetchInterval();
                    lastBatchId = message.getId();
                    if (lastBatchId > 0 && !message.getEntries().isEmpty()) {
                        consumerMessage(message);
                    }
                    long sleepTime = nextFetchTime - System.currentTimeMillis();
                    //超过20ms那我们就先sleep一下~~~
                    if (sleepTime > 20L) {
                        try {
                            Thread.sleep(sleepTime);
                        } catch (InterruptedException ignored) {
                        }
                    }
                }
            } catch (RuntimeException e) {
                runningSwitch = false;
                log.error("canalInstance: " + handle.instanceName() + " occurring a serious RuntimeException and lead to stop this canalInstance", e);
                //既然处理失败了, 那就回滚呗~~~
                handle.rollback(lastBatchId);
            } finally {
                synchronized (lock) {
                    running = false;
                    lock.notifyAll();
                }
                handle.disConnect();
            }
            log.info("canalInstance: " + handle.instanceName() + " has stopped");
        }

        @Override
        public String toString() {
            return "CanalInstance{" + handle.instanceName() + ", running=" + runningSwitch + "startRtTime=" + startRtTime + '}';
        }
    }
}
