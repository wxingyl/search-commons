# 模块介绍

## commons-lang

基础jar包，没有依赖任何第三方jar包。其实现了公司规定的Result和ErrorCode规范。所以搜索各个项目给其他团队提供的client包都可以基于该jar包。

另外还封装了作为搜索参数和简单返回结果的工具包，比如RangeFilter，Sort等。

## commons-component

搜索公共组件包，目前只是一些简单的Utils方法，里面依赖了一些第三方jar包：

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

## commons-cache

少量数据内存缓存，该模块封装实现了简单的缓存，支持动态实时修改(需要外部程序支持，比如canal等)，同时支持多机器互发通知更新缓存。

封装了notify/receive,多态机器，多系统之间实现消息逐个通知，类似广播的概念。目前搜索内存缓存少量数据使用到，实时索引在masterHost修改，将改动notify到注册的slaveHost机器, 完成数据同步修改。

## commons-db

封装数据库常用操作，主要使用datasource链接数据库，通过db-utils操作。

目前还未实现，后续添加完善

## commons-nlp

nlp相关工具类封装，包括：

#### 繁体转简体

繁体字转换为简体，目前不需要简体转繁体，所以就算了

#### 汉字转拼音

通过汉字得到拼音，以及拼音首字母等功能接口

#### 分词

分词的一些工具类，词库加载，不同粒度分词等，目前还没有，后续添加完善

## commons-qp

查询时识别关键字信息，其跟上面的分词息息相关，目前还没有，后续添加完善



#### 关键字识别工具





更多内容，不断添加~~~~~~

## 版本

### rc版本

rc版本为预发版本，未达到上线标准，能用，但是存在很多已知或者未知的bug，以及部分功能没有实现，所以外部线上环境不要使用所有的rc版本。

### 特殊说明

1. 目前最新版本`v1.0.1`，注意`v1.0`版本能用，但是存在bug：master机器重启，其恰好在slave机器监控周期之内，导致slave机器不知道master的重启，没有重新注册，导致master丢失slave机器列表，无法实现后续通知。
2. `commons-core` 模块的`v1.0`版本不建议使用，除了bug影响，另外`v1.0.1`对notify模块的实现做了较大改动，以及receive模块的接口返回类型做了修改，不兼容`v1.0`。使用版本从`v1.0.1`开始，就当`v1.0`版本不存在。

### pom依赖

`commons-client`

``` xml
<dependency>
	<groupId>com.tqmall.search</groupId>
	<artifactId>commons-client</artifactId>
	<version>v1.0.1</version>
</dependency>
```

`commons-core`, 其依赖了对应版本最新的`commons-client`模块

``` xml
<dependency>
	<groupId>com.tqmall.search</groupId>
	<artifactId>commons-core</artifactId>
	<version>1.0.1</version>
</dependency>
```