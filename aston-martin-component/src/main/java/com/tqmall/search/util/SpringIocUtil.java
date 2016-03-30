package com.tqmall.search.util;

import com.google.common.collect.Maps;
import com.tqmall.search.common.Environment;
import com.tqmall.search.common.param.Param;
import com.tqmall.search.common.utils.SearchStringUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.NoUniqueBeanDefinitionException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.annotation.Scope;

import javax.annotation.PostConstruct;
import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * success!
 * Created by yangrui on 15/5/19.
 * 从tqmallstall搬过来的
 * 该bean需要优先加载, 在scan之前
 */
@Slf4j
@Scope(value = ConfigurableBeanFactory.SCOPE_SINGLETON)
public class SpringIocUtil implements ApplicationContextAware {

    @Value("${current.environment}")
    private String currentEnvironment;

    @Value("${all.master.host}")
    private String allMasterHost;

    private static ApplicationContext applicationContext;

    @SuppressWarnings("unchecked")
    public static <T> T getBean(String id) {
        try {
            return (T) applicationContext.getBean(id);
        } catch (BeansException e) {
            log.warn("获取Bean, name: " + id + " 不存在, " + e.getMessage());
            return null;
        }
    }

    @SuppressWarnings("unchecked")
    public static <T> Set<T> getBeanNamesForType(Class<T> cls) {
        String[] beanNames = applicationContext.getBeanNamesForType(cls);
        Set<T> set = new HashSet<>();
        for (String beanName : beanNames) {
            set.add((T) applicationContext.getBean(beanName));
        }
        return set;
    }

    public static <T> T getBean(Class<T> cls) {
        try {
            return applicationContext.getBean(cls);
        } catch (NoUniqueBeanDefinitionException e) {
            String[] beanNames = applicationContext.getBeanNamesForType(cls);
            for (String str : beanNames) {
                T o = applicationContext.getBean(str, cls);
                if (o.getClass().getName().equals(cls.getName())) {
                    return o;
                }
            }
            return null;
        } catch (BeansException e) {
            log.warn("获取Bean, class: " + cls.getName() + " 不存在, " + e.getMessage());
            return null;
        }
    }

    @Override
    public void setApplicationContext(ApplicationContext arg0) throws BeansException {
        applicationContext = arg0;
    }

    /**
     * 该函数跟{@link #getBean(Class)}类似, 但是不错异常处理, 其获取的bean是确定在,如果没有就跑出异常, 调用方无需做空判断
     * 如果获取肯定存在的bean, 建议调用词方法
     * @param cls 需要的Bean类型class
     * @return not null, 如果为null则跑出{@link BeansException}
     * @see #getBean(Class)
     * @see BeansException
     */
    public static <T> T getConfirmBean(Class<T> cls) {
        return applicationContext.getBean(cls);
    }

    /**
     * 配置的allMasterHost, 为常量值, 多个环境之间通过逗号','分隔, 每个环境通过name=ip:port格式配置:
     * 1. name 中可以配置多个环境, 通过';'分隔, eg:
     * "goods=localhost:8080,order=localhost:8080" 等同于 "goods;order=localhost:8080"
     * 2. 如果name为所有, 比如本地, 测试, 开发, stable环境等, 可直接指定"all"代替所有, eg:
     * all=localhost:8080 等同于 goods;order;legend;saint=localhost:8080
     * 3. ip:port中, 如果ip为本地ip, 可以使用127.0.0.1, localhost, 或者直接省略ip, 直接通过port指定, eg:
     * "goods;order=localhost:8080" 等同于 "goods;order=8080"
     * 4. 所有关于字母相关的,大小写不相关, "goods"与"Goods"等同, "localHost"与"localhost"等同
     * 5. 如果配置的masterHost列表中有ip:port更本地的localIp:port相同,则当前环境也就支持该环下境, 如果在currentEnvironment中没有配置, 则主动添加
     * 数据格式错误抛出{@link IllegalArgumentException}
     *
     * @see #parseCurrentEnvironment(String)
     * @see #parseAllMasterHost(String)
     * @see Environment#init(Set, Map)
     * @see IllegalArgumentException
     */
    @PostConstruct
    private void init() {
        Environment.init(parseCurrentEnvironment(currentEnvironment),
                parseAllMasterHost(allMasterHost));
    }

    /**
     * 解析current.environment配置
     *
     * @param currentEnvironment current.environment配置的值
     * @return 不可修改的Set
     * @see Collections#unmodifiableSet(Set)
     */
    public static Set<Environment> parseCurrentEnvironment(String currentEnvironment) {
        currentEnvironment = Param.filterString(currentEnvironment);
        if (currentEnvironment == null) {
            throw new IllegalArgumentException("currentEnvironment没有有效字符");
        }

        //currentEnvironment解析
        Set<Environment> currentEnvSet = new HashSet<>();
        currentEnvironment = currentEnvironment.toUpperCase();
        if (currentEnvironment.equals("ALL")) {
            Collections.addAll(currentEnvSet, Environment.values());
        } else {
            for (String env : StringUtils.split(currentEnvironment, ',')) {
                currentEnvSet.add(Environment.valueOf(env));
            }
        }
        return Collections.unmodifiableSet(currentEnvSet);
    }

    /**
     * 解析all.master.host配置
     *
     * @param allMasterHost all.master.host配置的值
     * @return 不可修改的Map
     * @see Collections#unmodifiableMap(Map)
     */
    public static Map<Environment, String> parseAllMasterHost(String allMasterHost) {
        allMasterHost = Param.filterString(allMasterHost);
        if (allMasterHost == null) {
            throw new IllegalArgumentException("allMasterHost没有有效字符");
        }

        //allMasterHost初始化
        Map<Environment, String> envHostInfoMap = Maps.newHashMap();
        for (String s : StringUtils.split(allMasterHost, ',')) {
            String[] masterHost = StringUtils.split(s, '=');
            if (masterHost.length != 2) {
                throw new IllegalArgumentException("allMasterHost中: " + s + "不符合name=ip:port格式");
            }
            masterHost = SearchStringUtils.stringArrayTrim(masterHost);
            masterHost[0] = masterHost[0].toUpperCase();
            if ("ALL".equals(masterHost[0])) {
                for (Environment e : Environment.values()) {
                    envHostInfoMap.put(e, masterHost[1]);
                }
            } else {
                for (String name : SearchStringUtils.stringArrayTrim(StringUtils.split(masterHost[0], ';'))) {
                    envHostInfoMap.put(Environment.valueOf(name), masterHost[1]);
                }
            }
        }
        return Collections.unmodifiableMap(envHostInfoMap);
    }

}
