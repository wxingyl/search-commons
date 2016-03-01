#canal-client

​			阿里的 [Canal](https://github.com/alibaba/canal)——mysql数据库binlog的增量订阅&消费组件，很好的Java中间件。官方的client中的CanalConnector提供了简单的连接，订阅，获取Message等基本操作，当我们想针对具体的schema, table或者column实现业务代码，如何做到业务代码与操作canal实例代码分离，让开发人员专注于业务开发，不用关心canal实例如何链接，订阅，获取数据，重连等？

##主要功能

1. canal实例运行封装，支持多个canal实例，不同canal服务器连接，提供一些连接配置
2. canal服务器异常，本地自动重连机制
3. 根据实例，表的数据变化自定义响应函数，处理数据变化，目前实现了3个级别处理：实例级别，表级别，表中多条记录事件类型级别
4. 表的处理支持感兴趣字段过滤
5. 表的处理支持多条件筛选
6. 执行自定义处理函数时发生RuntimeException，提供异常处理扩展接口

##主要类介绍

###CanalExecutor

com.tqmall.search.canal.CanalExecutor, canal实例执行器，管理各个canal实例。每个canal实例运行都在单独的后台线程运行，循环获取数据, 进行处理，该线程的创建通过构造函数中的`ThreadFactory`创建,在启动canal实例是执行线程创建~~~

```java
    private static final AtomicInteger EXECUTOR_NUMBER = new AtomicInteger(1);

    public CanalExecutor() {
        this(new ThreadFactory() {
            private final ThreadFactory defaultFactory = Executors.defaultThreadFactory();
            private final AtomicInteger threadNumber = new AtomicInteger(1);
            private final String namePrefix = "canal-" + EXECUTOR_NUMBER.getAndIncrement() + "-thread-";

            @Override
            public Thread newThread(Runnable r) {
                Thread thread = defaultFactory.newThread(r);
                thread.setName(namePrefix + threadNumber.getAndIncrement());
                return thread;
            }
        });
    }

    public CanalExecutor(ThreadFactory threadFactory) {
        this.threadFactory = threadFactory;
        //jvm退出时执行hook
        Runtime.getRuntime().addShutdownHook(threadFactory.newThread(new Runnable() {
            @Override
            public void run() {
                stopAll();
            }
        }));
    }
```

如上面代码，通过Runtime.getRuntime().addShutdownHook(Thread), jvm退出时，会停止掉当前所有正在运行的canal实例

主要方法：

####addInstanceHandle

```java
public void addInstanceHandle(CanalInstanceHandle handle)
```

添加一个实例，CanalInstanceHandle是对一个canal实例操作的封装，后续有介绍

####startInstance

```java
public void startInstance(String instanceName)
public void startInstance(String instanceName, long startRtTime)
public void startAllInstance(long startRtTime)
```

启动一个已经添加的，为启动的实例, 该方法会创建canal执行线程，并且等待该执行线程创建完成之后返回

####isRunning

```java
public boolean isRunning(String instanceName)
```

判断指定canal是否正在运行，当然如果该instanceName对应的canal实例不存在，返回结果也就为false了

####stopInstance

```java
public void stopInstance(String instanceName)
```

结束当前canal实例

####allCanalInstance

```java
public String[] allCanalInstance()
```

获取当前所有的canal实例名称

###CanalInstanceHandle

该接口定义了canal实例常用操作方法，有3个实现类：`InstanceSectionHandle`, `TableSectionHandle`, `EventTypeSectionHandle`, 分别对应实例，表，表中多条记录事件更新类型3个级别, 同时对应3个数据更新事件处理Action: `InstanceAction`, `TableAction`, `EventTypeAction`

CanalInstanceHandle实现类提供canal链接配置设置，如下：

```java
/**
 * 获取Message {@link CanalConnector#getWithoutAck(int, Long, TimeUnit)}的batchSize
 * 默认1000, 如果 <= 0, canal内部取默认1000
 */
public void setMessageBatchSize(int messageBatchSize)

/**
 * 获取Message {@link CanalConnector#getWithoutAck(int, Long, TimeUnit)}的超时时间
 * time unit is ms, 默认1s
 */
public void setMessageTimeout(Long messageTimeout)

/**
 * 轮询获取变更数据的时间间隔, 默认500ms, 如果对于实时性要求较高可以设置小一些
 * @see #fetchInterval()
 */
public void setFetchInterval(long fetchInterval)
```

对与异常处理，3个实现类均有默认处理或者设置自定义处理

1. `InstanceSectionHandle`异常处理扩展接口：

   ```java
   /**
    * @param exceptionHandleFunction 异常处理方法, 返回结果表示是否忽略, 如果返回null 则为false, 即不忽略, 默认不忽略
    */
   public void setExceptionHandleFunction(Function<ExceptionContext, Boolean> exceptionHandleFunction)
   ```

2. `TableSectionHandle` 和 `EventTypeSectionHandle` 异常处理扩展接口：

   ```java
   /**
    * 是否忽略处理异常, 默认忽略
    * 优先处理{@link #handleExceptionFunction}
    */
   public void setIgnoreHandleException(boolean ignoreHandleException)
   /**
    * 异常处理方法, 优先根据该Function处理
    *
    * @param handleExceptionFunction 该function的返回结果标识是否忽略该异常, 同{@link #ignoreHandleException}
    * @see #ignoreHandleException
    */
   public void setExceptionHandleFunction(Function<HandleExceptionContext, Boolean> handleExceptionFunction)
   ```

对于`TableSectionHandle` 和 `EventTypeSectionHandle`, 可以设置`userLocalTableFilter`, 如下：

```java
    /**
     * {@link #canalConnector}连接时, 需要执行订阅{@link CanalConnector#subscribe()} / {@link CanalConnector#subscribe(String)}
     * 该变量标识是否使用本地, 即在{@link #actionFactory}中注册的schema, table
     * 如果为true, 订阅时生成filter, 提交直接替换canal server服务端配置的filter信息
     * 如果为false, 以canal server服务端配置的filter信息为准
     * 默认为true, 使用本地的filter配置
     */
    public void setUserLocalTableFilter(boolean userLocalTableFilter)
```

###Actionable

事件响应公共抽象接口定义，有3种Action: `InstanceAction`, `TableAction`, `EventTypeAction`，分别对应上面3个`CanalInstanceHandle`实现类。

###Schema

数据库schema对象封装, 为了保证table在创建完成之后不可修改, 做了只能通过提供的静态方法构造的限制，该类中封装内部类Table，同数据库中的schema.table结构。每个table可以绑定对应的`Actionable`对象。

其构造通过类`Schemas`中的工厂方法，主要方法：

```java
Schemas#buildSchema(String)
Schemas.Builder
Schemas#buildTable(String)
```

###ActionFactory

schema.table的action提供者接口定义
```java
public interface ActionFactory<T extends Actionable> extends Iterable<Schema<T>> {

    /**
     * 通过schemaName, tableName获取对应的{@link Schema.Table}对象
     * 如果对应的table不存在, 返回null
     */
    Schema<T>.Table getTable(String schemaName, String tableName);

    /**
     * 通过schemaName获得对应的{@link Schema}对象
     * 如果对应的table不存在, 返回null
     */
    Schema<T> getSchema(String schemaName);
}
```

对应2个实现类`SingleSchemaActionFactory`和`MultiSchemaActionFactory`

###CurrentHandleTable

可以设定当前正在处理的`Schema.Table`接口定义, 配合`TableAction`, `EventTypeAction`一起使用, 对应相关类有：

1. `MultiThreadCurrentHandleTable`和`SingleThreadCurrentHandleTable`
2. `AbstractTableAction`和`AbstractEventTypeAction`

###UML图

详细的[UML图](canal-client_UML.png), 另外有文件`canal-client.mdj`，通过软件[StarUML](http://staruml.io/)编辑


##使用

###maven依赖

pom依赖

```xml
<dependency>
	<groupId>com.tqmall.search</groupId>
	<artifactId>canal-client</artifactId>
	<version>1.0-rc1</version>
</dependency>
```

另外需要注意

1. 该jar包依赖了搜索的commons-lang, commons-lang里面没有任何依赖:

   ```xml
   <dependency>
       <groupId>com.tqmall.search</groupId>
       <artifactId>commons-lang</artifactId>
       <version>1.0-rc2</version>
   </dependency>
   ```

2. 使用的canal.client版本是1.0.21: 

   ```xml
   <dependency>
       <groupId>com.alibaba.otter</groupId>
       <artifactId>canal.client</artifactId>
       <version>1.0.21</version>
   </dependency>
   ```

   canal.client里面依赖了很多第三方jar，所以使用时注意冲突，主要有

   ```xml
    <!-- zk -->
    <dependency>
        <groupId>org.apache.zookeeper</groupId>
        <artifactId>zookeeper</artifactId>
    </dependency>
    <dependency>
        <groupId>com.github.sgroschupf</groupId>
        <artifactId>zkclient</artifactId>
    </dependency>
    <!-- external -->
    <dependency>
        <groupId>commons-io</groupId>
        <artifactId>commons-io</artifactId>
    </dependency>
    <dependency>
        <groupId>commons-lang</groupId>
        <artifactId>commons-lang</artifactId>
        <version>2.6</version>
    </dependency>
    <dependency>
        <groupId>org.springframework</groupId>
        <artifactId>spring</artifactId>
    </dependency>
    <dependency>
        <groupId>com.alibaba</groupId>
        <artifactId>fastjson</artifactId>
    </dependency>
    <dependency>
        <groupId>com.google.guava</groupId>
        <artifactId>guava</artifactId>
    </dependency>
    <!-- log -->
    <dependency>
        <groupId>ch.qos.logback</groupId>
        <artifactId>logback-core</artifactId>
    </dependency>
    <dependency>
        <groupId>ch.qos.logback</groupId>
        <artifactId>logback-classic</artifactId>
    </dependency>
    <dependency>
        <groupId>org.slf4j</groupId>
        <artifactId>jcl-over-slf4j</artifactId>
    </dependency>
    <dependency>
        <groupId>org.slf4j</groupId>
        <artifactId>slf4j-api</artifactId>
    </dependency>
   ```

​	

###Demo Class

[CanalClientDemo.java](src/test/java/com/tqmall/search/canal/CanalClientDemo.java)

