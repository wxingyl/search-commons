# 模块介绍

## commons-lang

基础jar包，没有依赖任何第三方jar包。其实现了公司规定的Result和ErrorCode规范。所以搜索各个项目给其他团队提供的client包都可以基于该jar包。

另外还封装了作为搜索参数和简单返回结果的工具包，比如RangeFilter，Sort等。

目前最新版本1.1.1

##canal-client

基于阿里 [Canal](https://github.com/alibaba/canal) mysql数据库binlog的增量订阅&消费组件，封装其基本使用，统一维护canal instance运行，自定义实例，表，行改动的事件处理以及改动的数据过滤, 具体使用参见[文档](canal-client/docs)

目前最新版本1.1.1

## commons-component

依赖`commons-lang`，搜索公共组件包，目前只是一些简单的Utils方法，里面依赖了一些第三方jar包：

``` xml
<dependency>
    <groupId>com.google.guava</groupId>
    <artifactId>guava</artifactId>
    <version>19.0</version>
</dependency>
<dependency>
    <groupId>org.apache.commons</groupId>
    <artifactId>commons-lang3</artifactId>
    <version>3.4</version>
</dependency>
<dependency>
    <groupId>com.fasterxml.jackson.core</groupId>
    <artifactId>jackson-databind</artifactId>
    <version>2.6.4</version>
</dependency>
<dependency>
    <groupId>org.slf4j</groupId>
    <artifactId>slf4j-api</artifactId>
    <version>1.7.12</version>
</dependency>        
```

这4个jar包基本上是每个项目都需要的，所以这儿单独拿出来，并且基于这些第三库封装了一些工具类。

如果需要排除这些引入的jar包，避免冲突，如下直接拷贝拿去：

``` xml
<exclusions>
    <exclusion>
        <groupId>com.google.guava</groupId>
        <artifactId>guava</artifactId>
    </exclusion>
    <exclusion>
        <groupId>org.apache.commons</groupId>
        <artifactId>commons-lang3</artifactId>
    </exclusion>
    <exclusion>
        <groupId>com.fasterxml.jackson.core</groupId>
        <artifactId>jackson-databind</artifactId>
    </exclusion>
    <exclusion>
        <groupId>org.slf4j</groupId>
        <artifactId>slf4j-api</artifactId>
    </exclusion>
</exclusions>
```

是不是很人性化~~~~~~

目前最新版本1.0.1

## commons-cache-memory

依赖`commons-component`, `canal-client`，少量数据内存缓存，该模块封装实现了简单的缓存，支持动态实时修改(需要外部程序支持，比如canal等)，同时支持多机器互发通知更新缓存。

封装了notify/receive,多台机器，多系统之间实现消息逐个通知，类似广播的概念。目前搜索内存缓存少量数据使用到，实时索引在masterHost修改，将改动notify到注册的slaveHost机器, 完成数据同步修改。

目前最新版本1.0.1

## commons-cache-redis

通过redis实现cache操作，实现了`commons-lang`中的[Cache.java](commons-lang/src/main/java/com/tqmall/search/commons/lang/Cache.java)，同时通过`jedis`封装了redis的访问，方便jedis的使用, 具体见[RedisClient.java](commons-cache-redis/src/main/java/com/tqmall/search/redis/RedisClient.java)

目前最新版本1.0

## commons-nlp

依赖`commons-lang`，简单的nlp相关工具类封装，包括：

####Trie相关算法

实现前缀查询树，逆向前缀查询树，最大/最小—正向/反向匹配算法，Aho-Corasick模式匹配树等算法。封装、抽象Trie相关算法，方便自定义扩展

还没有实现双数组Trie树，后续考虑添加

#### 繁体转简体

繁体字转换为简体，目前不需要简体转繁体，所以就算了

#### 汉字转拼音

通过汉字得到拼音，以及拼音首字母等功能接口

#### 分词

分词的一些工具类，词库加载，不同粒度分词等。

目前支持对英语单词，阿拉伯数字，小数，汉字3个粒度分词，如下：

```java
public enum SegmentType {
    //小粒度分词, 根据词库最小匹配
    MIN,
    //大粒度分词, 根据词库最大匹配
    MAX,
    //尽可能多的分词, 根据词典匹配所有结果
    FULL
}
```

目前最新版本1.0，基于该版本实现了ElasticSearch的分词，汉子繁体转简体以及汉子转拼音，具体见项目[elasticsearch-analysis-benz](https://github.com/wxingyl/elasticsearch-analysis-benz)

## commons-db

封装数据库常用操作，主要使用datasource链接数据库，通过db-utils操作。

最新版本1.0




更多内容，不断添加~~~~~~

## 版本

### rc版本

rc版本为预发版本，未达到上线标准，能用，但是存在很多已知或者未知的bug，以及部分功能没有实现，所以外部线上环境不要使用所有的rc版本。

### 特殊说明

1. 原先项目只有commons-lang和commons-core两个模块，这两个模块目前已经删除，被commons-lang和其他几个功能模块代替，建议不要再使用~~~

### pom依赖

所有的jar没有放到maven中央仓库，所以要使用自己先mvn install, mvn deploy, 之后使用就顺顺当当的了~~~~~~