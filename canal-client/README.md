

#canal-client使用

##maven依赖

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

##Demo Class

[CanalClientDemo.java](src/test/java/com/tqmall/search/canal/CanalClientDemo.java)