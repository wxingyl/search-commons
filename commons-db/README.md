```
使用方法参考
<?xml version="1.0" encoding="UTF-8"?>
<beans xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
       xmlns="http://www.springframework.org/schema/beans"
       xsi:schemaLocation="http://www.springframework.org/schema/beans
			http://www.springframework.org/schema/beans/spring-beans-4.0.xsd"
>
    <!--连接池定义-->
    <bean id="baseDataSource"
          class="org.apache.commons.dbcp2.BasicDataSource"
          destroy-method="close">
        <!--maxActive: 最大连接数量-->
        <property name="maxTotal" value="15"/>
        <!--minIdle: 最小空闲连接-->
        <property name="minIdle" value="2"/>
        <!--maxIdle: 最大空闲连接-->
        <property name="maxIdle" value="5"/>
        <!--initialSize: 初始化连接-->
        <property name="initialSize" value="1"/>
        <!-- 连接被泄露时是否打印 -->
        <property name="logAbandoned" value="true"/>
        <!--removeAbandoned: 是否自动回收超时连接-->
        <property name="removeAbandonedOnBorrow" value="true"/>
        <!--removeAbandonedTimeout: 超时时间(以秒数为单位)-->
        <property name="removeAbandonedTimeout" value="10"/>
        <!--maxWait: 超时等待时间以毫秒为单位 1000等于1秒-->
        <property name="maxWaitMillis" value="60000"/>
        <!-- 在空闲连接回收器线程运行期间休眠的时间值,以毫秒为单位. -->
        <property name="timeBetweenEvictionRunsMillis" value="10000"/>
        <!--  在每次空闲连接回收器线程(如果有)运行时检查的连接数量 -->
        <property name="numTestsPerEvictionRun" value="10"/>
        <!-- 1000 * 60 * 30  连接在池中保持空闲而不被空闲连接回收器线程-->
        <property name="minEvictableIdleTimeMillis" value="10000"/>
        <property name="validationQuery" value="SELECT NOW() FROM DUAL"/>
    </bean>

    <!-- 数据源配置,可配置多数据源 -->
    <bean id="shopDataSource" parent="baseDataSource">
        <property name="driverClassName" value="${esJdbcDriver}"/>
        <property name="url" value="${shopDbUrl}"/>
        <property name="username" value="${shopDbUser}"/>
        <property name="password" value="${shopDbPassword}"/>
    </bean>

    <bean id="shopTqbaseDataSource" parent="baseDataSource">
        <property name="driverClassName" value="${esJdbcDriver}"/>
        <property name="url" value="${shopTqbaseDbUrl}"/>
        <property name="username" value="${shopTqbaseDbUser}"/>
        <property name="password" value="${shopTqbaseDbPassword}"/>
    </bean>

    <bean id="saintDataSource" parent="baseDataSource">
        <property name="driverClassName" value="${esJdbcDriver}"/>
        <property name="url" value="${saintDbUrl}"/>
        <property name="username" value="${saintDbUser}"/>
        <property name="password" value="${saintDbPassword}"/>
    </bean>

    <bean id="ucenterDataSource" parent="baseDataSource">
        <property name="driverClassName" value="${esJdbcDriver}"/>
        <property name="url" value="${ucDbUrl}"/>
        <property name="username" value="${ucDbUser}"/>
        <property name="password" value="${ucDbPassword}"/>
    </bean>

    <bean id="windDataSource" parent="baseDataSource">
        <property name="driverClassName" value="${esJdbcDriver}"/>
        <property name="url" value="${windDbUrl}"/>
        <property name="username" value="${windDbUser}"/>
        <property name="password" value="${windDbPassword}"/>
    </bean>

    <bean id="legendDataSource" parent="baseDataSource">
        <property name="driverClassName" value="${esJdbcDriver}"/>
        <property name="url" value="${legendDbUrl}"/>
        <property name="username" value="${legendDbUser}"/>
        <property name="password" value="${legendDbPassword}"/>
    </bean>

    <bean id="otterDataSource" parent="baseDataSource">
        <property name="driverClassName" value="${esJdbcDriver}"/>
        <property name="url" value="${otterDbUrl}"/>
        <property name="username" value="${otterDbUser}"/>
        <property name="password" value="${otterDbPassword}"/>
    </bean>

    <bean id="cloudEpcDataSource" parent="baseDataSource">
        <property name="driverClassName" value="${esJdbcDriver}"/>
        <property name="url" value="${cloudEpcDbUrl}"/>
        <property name="username" value="${cloudEpcDbUser}"/>
        <property name="password" value="${cloudEpcDbPassword}"/>
    </bean>

    <!-此处为Dao定义,sql调用请在其它类中调用dao去调用,dao只处理查询及返回-->
    <bean id="shopDao" class="com.tqmall.search.dal.dao.BaseDao">
        <constructor-arg name="dataSource" ref="shopDataSource"/>
    </bean>

    <bean id="legendDao" class="com.tqmall.search.dal.dao.BaseDao">
        <constructor-arg name="dataSource" ref="legendDataSource"/>
    </bean>
    <bean id="saintDao" class="com.tqmall.search.dal.dao.BaseDao">
        <constructor-arg name="dataSource" ref="saintDataSource"/>
    </bean>

    <bean id="ucenterDao" class="com.tqmall.search.dal.dao.BaseDao">
        <constructor-arg name="dataSource" ref="ucenterDataSource"/>
    </bean>

    <bean id="otterDao" class="com.tqmall.search.dal.dao.BaseDao">
        <constructor-arg name="dataSource" ref="otterDataSource"/>
    </bean>

    <bean id="windDao" class="com.tqmall.search.dal.dao.BaseDao">
        <constructor-arg name="dataSource" ref="windDataSource"/>
    </bean>

    <bean id="shopTqbaseDao" class="com.tqmall.search.dal.dao.BaseDao">
        <constructor-arg name="dataSource" ref="shopTqbaseDataSource"/>
    </bean>

    <bean id="cloudEpcDao" class="com.tqmall.search.dal.dao.BaseDao">
        <constructor-arg name="dataSource" ref="cloudEpcDataSource"/>
    </bean>

</beans>
```