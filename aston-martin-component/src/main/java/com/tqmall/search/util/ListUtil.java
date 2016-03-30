package com.tqmall.search.util;


import org.apache.commons.lang3.StringUtils;
import org.elasticsearch.common.Strings;
import org.elasticsearch.search.SearchHitField;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ReflectionUtils;

import java.beans.IntrospectionException;
import java.beans.PropertyDescriptor;
import java.io.Serializable;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.util.*;

/**
 * Created by wcong on 14-6-7.
 */
public class ListUtil {

    /**
     * list 分组
     *
     * @param list 原始的list
     * @param num  每组的数量
     * @return
     */
    public static <T> List<List<T>> arrayChunk(List<T> list, int num) {
        List<List<T>> chunkList = new LinkedList<>();
        if (num == 0 || CollectionUtils.isEmpty(list)) {
            return chunkList;
        }
        Integer chunkNum = list.size() / num;
        if ((list.size() % num) != 0) {
            chunkNum += 1;
        }
        Integer startIndex = 0;
        for (Integer i = 0; i < chunkNum - 1; i++) {
            chunkList.add(list.subList(startIndex, startIndex + num));
            startIndex += num;
        }
        chunkList.add(list.subList(startIndex, list.size()));
        return chunkList;
    }

    public static <T> List<List<T>> listChunk(List<T> list, int num) {
        List<List<T>> chunkedList = new LinkedList<>();
        if (num == 0 || list.isEmpty()) {
            return chunkedList;
        }
        Integer chunkNum = list.size() / num;
        if ((list.size() % num) != 0) {
            chunkNum += 1;
        }
        Integer startIndex = 0;
        for (Integer i = 0; i < chunkNum - 1; i++) {
            chunkedList.add(list.subList(startIndex, startIndex + num));
            startIndex += num;
        }
        chunkedList.add(list.subList(startIndex, list.size()));
        return chunkedList;
    }

    /**
     * 把list里面的内容以 join 连接
     * 如：0^^1^^2^^3^^4^^5^^6^^7^^8^^9^^10
     */
    public static <T> String implode(Iterable<T> list, String join) {
        StringBuilder sb = new StringBuilder();
        for (T t : list) {
            sb.append(t);
            sb.append(join);
        }
        if (sb.length() > 0) sb.delete(sb.length() - join.length(), sb.length());
        return sb.toString();
    }

    /**
     * 把Array里面的内容以 join 连接
     * 如：0^^1^^2^^3^^4^^5^^6^^7^^8^^9^^10
     */
    public static String implode(Object[] list, String join) {
        StringBuilder sb = new StringBuilder();
        if (list.length == 0) {
            return sb.toString();
        }
        for (Object t : list) {
            sb.append(t);
            sb.append(join);
        }
        sb.delete(sb.length() - join.length(), sb.length());
        return sb.toString();
    }

    public static boolean contain(Object[] array, Object judge) {
        for (Object obj : array) {
            if (obj.equals(judge)) {
                return true;
            }
        }
        return false;
    }

    public static <T> List<String> convertToString(List<T> list) {
        List<String> retList = new LinkedList<>();
        for (T t : list) {
            retList.add((String) t);
        }
        return retList;
    }

    /**
     * 用于把一个包含对象的List，根据传入的属性名转换成对应的只包含此列的List。如果对象本身就是字符串，只需要强制转换，请使用方法 convertToString(List<T> list)
     *
     * @param originList
     * @param property
     * @return List<String>
     * @author yibo.liu
     */
    public static <T> List<String> convertObjectListToStringList(List<T> originList, String property) {
        List<String> resultList = new LinkedList<>();
        Method getMethod = null;
        try {
            for (T t : originList) {
                if (getMethod == null) {
                    getMethod = t.getClass().getMethod("get"
                            + StringUtils.upperCase(property.substring(0, 1))
                            + property.substring(1));
                }
                resultList.add(String.valueOf(getMethod.invoke(t)));
            }
        } catch (NoSuchMethodException | InvocationTargetException | IllegalAccessException e) {
            e.printStackTrace();
        }
        return resultList;
    }

    /**
     * list中过滤掉null的value
     */
    public static <T> void filterNullValue(List<T> list) {
        if (list == null || list.isEmpty()) return;
        if (list instanceof RandomAccess) {
            int i;
            while ((i = list.indexOf(null)) >= 0) {
                list.remove(i);
            }
        } else {
            Iterator<T> it = list.iterator();
            while (it.hasNext()) {
                if (it.next() == null) it.remove();
            }
        }
    }

    /**
     * 字符串比较,升序,null在后面
     *
     * @return 等于0, 相等.小于0, o1小于o2.大于0, o2小于o1.
     */
    public static int sortString(String o1, String o2) {
        return (((o1 != null && o2 != null)) ? o1.compareToIgnoreCase(o2) : ((o1 == null && o2 != null) ? -1 : 1));
    }

    public static <T> void sort(List<T> list, final Comparator<? super T>... cs) {
        Object[] a = list.toArray();
        Comparator<? super T> comparator = new Comparator<T>() {
            @Override
            public int compare(T o1, T o2) {
                int r = 0;
                for (Comparator<? super T> c : cs) {
                    if ((r = c.compare(o1, o2)) != 0) {
                        return r;
                    }
                }
                return r;
            }
        };
        Arrays.sort(a, (Comparator) comparator);
        ListIterator i = list.listIterator();
        for (int j = 0; j < a.length; j++) {
            i.next();
            i.set(a[j]);
        }
    }

    /**
     * 把map转换为bean ，其中map的key字段由下划线分隔的会自动转换为驼峰，bean中没有对应的属性会自动跳过，不报错，如果有需要，请自行排查错误<br/>
     *
     * @param map 要求map的key为字符串string类型，因为此key将会是bean中的field，即属性名。如果map中的value是map，则需要此map的key也是string类型。
     * @param cls 返回值类，即转换结果
     * @param <T> 类的泛型
     * @return <T>
     */
    public static <T> T convertMapToBean(Map<String, ?> map, Class<T> cls) {
        try {
            T bean = cls.newInstance();
            for (Map.Entry<String, ?> entry : map.entrySet()) {
                String key = entry.getKey();
                Object v = entry.getValue();

                if (v != null && v instanceof SearchHitField) {//针对searchHits的fields特殊处理
                    v = ((SearchHitField) v).getValue();
                }
                if (v == null) {
                    continue;
                }

                String k = Strings.toCamelCase(String.valueOf(key));
                try {
                    PropertyDescriptor propertyDescriptor = new PropertyDescriptor(k, cls);
                    Method m = propertyDescriptor.getWriteMethod();

                    Class type = v.getClass();
                    if (type == propertyDescriptor.getPropertyType()) {//如果两个类型一样，写直接写入
                        m.invoke(bean, v);
                        continue;
                    }
                    if (v instanceof Collection || v instanceof Map) {
                        Field f = ReflectionUtils.findField(cls, k);
                        if (f.getGenericType() instanceof ParameterizedType) {//如果有泛型，没有泛型的时候，就直接set，不进行处理
                            if (v instanceof Collection) {//collection
                                Class typeClass = (Class) (((ParameterizedType) f.getGenericType()).getActualTypeArguments()[0]);
                                v = convertList((Collection) v, typeClass);
                            } else {//map
                                Class typeClass = (Class) (((ParameterizedType) f.getGenericType()).getActualTypeArguments()[0]);
                                v = convertMapToBean((Map<String, ?>) v, typeClass);
                            }
                        } else {//如果没有泛型，则直接转换成对应的类，那么，这里就只有map->class，没有collection
                            if (v instanceof Map) {//map
                                Class typeClass = f.getType();
                                v = convertMapToBean((Map<String, ?>) v, typeClass);
                            }
                        }
                        m.invoke(bean, v);
                        continue;
                    }

                    String value = String.valueOf(v);
                    type = propertyDescriptor.getPropertyType();//属性类型
                    if (type == Integer.class) {
                        m.invoke(bean, Integer.valueOf(value));
                    } else if (type == Long.class) {
                        m.invoke(bean, Long.valueOf(value));
                    } else if (type == Double.class) {
                        m.invoke(bean, Double.valueOf(value));
                    } else if (type == Float.class) {
                        m.invoke(bean, Float.valueOf(value));
                    } else {//默认string类型，不支持更多类型，如果有其它类型，可能出错，需要单独处理。
                        if (StringUtil.isDate(String.valueOf(value))) { // 日期格式
                            value = StringUtil.formatDate(String.valueOf(value));
                        }
                        m.invoke(bean, value);
                    }

                } catch (IntrospectionException e) {
                    //此处如果没有此属性，则不处理，跳过
                } catch (Exception e) {
                    e.printStackTrace();
                    //其它错误
                }
            }
            return bean;
        } catch (InstantiationException | IllegalAccessException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 提供给 {@link #convertMapToBean(Map, Class)} 使用，暂时可以处理一层泛型集合
     *
     * @param c
     * @param cls
     * @return
     */
    public static Collection convertList(Collection c, Class cls) {
        List list = new ArrayList();
        for (Object v : c) {
            if (v instanceof Collection) {
                list.add(convertList((Collection) v, cls));
            } else if (v instanceof Map) {
                list.add(convertMapToBean((Map) v, cls));
            } else {
                list.add(v);
            }
        }
        return list;
    }

    public static boolean contaign(Object[] array, Object judge) {
        for (Object obj : array) {
            if (obj.equals(judge)) {
                return true;
            }
        }
        return false;
    }

    public static class NullSet<E>
            extends AbstractSet<E>
            implements Serializable {
        public static NullSet nullSet = new NullSet();

        private NullSet() {
        }

        @Override
        public Iterator<E> iterator() {
            return NullIterator.nullIterator;
        }

        @Override
        public int size() {
            return 0;
        }

        @Override
        public boolean add(E e) {
            return true;
        }

    }

    public static class NullIterator<E> implements Iterator<E> {
        public static NullIterator nullIterator = new NullIterator();

        private NullIterator() {
        }

        @Override
        public boolean hasNext() {
            return false;
        }

        @Override
        public E next() {
            throw new NoSuchElementException();
        }

        @Override
        public void remove() {

        }
    }
}
