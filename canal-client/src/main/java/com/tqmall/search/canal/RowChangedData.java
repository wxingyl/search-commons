package com.tqmall.search.canal;

import com.alibaba.otter.canal.protocol.CanalEntry;
import com.tqmall.search.commons.lang.Function;
import com.tqmall.search.commons.lang.StrValueConvert;
import com.tqmall.search.commons.utils.CommonsUtils;
import com.tqmall.search.commons.utils.StrValueConverts;

import java.io.Serializable;
import java.util.*;

/**
 * Created by xing on 16/2/22.
 * {@link CanalEntry}中一行数据改动封装
 * 对于RowChangedData对象, 可以通过{@link #getEventType(RowChangedData)}判断对象类型
 *
 * @see #getEventType(RowChangedData)
 */
public abstract class RowChangedData<V> implements Function<String, V>, Serializable, AutoCloseable {

    private static final long serialVersionUID = -8712239138384357603L;

    final Map<String, V> fieldValueMap;

    RowChangedData() {
        this.fieldValueMap = new HashMap<>();
    }

    RowChangedData(Map<String, V> fieldValueMap) {
        this.fieldValueMap = fieldValueMap;
    }

//    RowChangedData(CanalEntry.RowData rowData, Set<String> interestedColumns) {
//        initByRowData(rowData, interestedColumns);
//    }

    @Override
    public final V apply(String s) {
        return fieldValueMap.get(s);
    }

    @Override
    public void close() {
        fieldValueMap.clear();
    }

    @Override
    public String toString() {
        return fieldValueMap.toString();
    }

    public static abstract class StrRowChangedData extends RowChangedData<String> {

        private static final long serialVersionUID = -6170440278516894897L;

        StrRowChangedData() {
        }

        public StrRowChangedData(Map<String, String> fieldValueMap) {
            super(fieldValueMap);
        }

        /**
         * 常用的基本类型class会通过{@link StrValueConverts#getBasicConvert(Class)}获取对应的{@link StrValueConvert}对象
         * 对于没有实现的class会抛出{@link IllegalArgumentException}
         *
         * @see StrValueConverts#getBasicConvert(Class)
         * @see StrValueConvert
         */
        public final <T> T getValue(String column, Class<T> cls) {
            return getValue(column, StrValueConverts.getBasicConvert(cls));
        }

        /**
         * 基本的数据类型建议调用{@link #getValue(String, Class)}
         */
        public final <T> T getValue(String column, StrValueConvert<T> convert) {
            return convert.convert(fieldValueMap.get(column));
        }
    }

    public static final class Insert extends StrRowChangedData {

        private static final long serialVersionUID = -2687037454927572799L;

        Insert() {
        }

        public Insert(CanalEntry.RowData rowData, Set<String> interestedColumns) {
            if (CommonsUtils.isEmpty(interestedColumns)) {
                for (CanalEntry.Column c : rowData.getAfterColumnsList()) {
                    fieldValueMap.put(c.getName(), c.getIsNull() ? null : c.getValue());
                }
            } else {
                for (CanalEntry.Column c : rowData.getAfterColumnsList()) {
                    if (interestedColumns.contains(c.getName())) {
                        fieldValueMap.put(c.getName(), c.getIsNull() ? null : c.getValue());
                    }
                }
            }
        }

        @Override
        public String toString() {
            return CanalEntry.EventType.INSERT.toString() + ':' + super.toString();
        }

    }

    public static final class Delete extends StrRowChangedData {

        private static final long serialVersionUID = 6254540878604970123L;

        Delete() {
        }

        public Delete(CanalEntry.RowData rowData, Set<String> interestedColumns) {
            if (CommonsUtils.isEmpty(interestedColumns)) {
                for (CanalEntry.Column c : rowData.getBeforeColumnsList()) {
                    fieldValueMap.put(c.getName(), c.getIsNull() ? null : c.getValue());
                }
            } else {
                for (CanalEntry.Column c : rowData.getBeforeColumnsList()) {
                    if (interestedColumns.contains(c.getName())) {
                        fieldValueMap.put(c.getName(), c.getIsNull() ? null : c.getValue());
                    }
                }
            }
        }

        @Override
        public String toString() {
            return CanalEntry.EventType.DELETE.toString() + ':' + super.toString();
        }

    }

    public static final class Update extends RowChangedData<Pair> {

        private static final long serialVersionUID = 585376007150297603L;

        public Update(Map<String, Pair> dataMap) {
            super(dataMap);
        }

        public final String getBefore(String column) {
            Pair pair;
            return (pair = fieldValueMap.get(column)) == null ? null : pair.before;
        }

        /**
         * 常用的基本类型class会通过{@link StrValueConverts#getBasicConvert(Class)}获取对应的{@link StrValueConvert}对象
         * 对于没有实现的class会抛出{@link IllegalArgumentException}
         *
         * @see StrValueConverts#getBasicConvert(Class)
         * @see StrValueConvert
         */
        public final <T> T getBefore(String column, Class<T> cls) {
            return getBefore(column, StrValueConverts.getBasicConvert(cls));
        }

        /**
         * 基本的数据类型建议调用{@link #getBefore(String)}
         */
        public final <T> T getBefore(String column, StrValueConvert<T> convert) {
            return convert.convert(fieldValueMap.get(column) != null ? fieldValueMap.get(column).getBefore() : null);
        }

        public final Function<String, String> getBefores() {
            return new Function<String, String>() {
                @Override
                public String apply(String s) {
                    return getBefore(s);
                }
            };
        }

        public String getAfter(String column) {
            Pair pair;
            return (pair = fieldValueMap.get(column)) == null ? null : pair.after;
        }

        /**
         * 常用的基本类型class会通过{@link StrValueConverts#getBasicConvert(Class)}获取对应的{@link StrValueConvert}对象
         * 对于没有实现的class会抛出{@link IllegalArgumentException}
         *
         * @see StrValueConverts#getBasicConvert(Class)
         * @see StrValueConvert
         */
        public <T> T getAfter(String column, Class<T> cls) {
            return getAfter(column, StrValueConverts.getBasicConvert(cls));
        }

        /**
         * 基本的数据类型建议调用{@link #getAfter(String)}
         */
        public <T> T getAfter(String column, StrValueConvert<T> convert) {
            return convert.convert(fieldValueMap.get(column) != null ? fieldValueMap.get(column).getAfter() : null);
        }

        public Function<String, String> getAfters() {
            return new Function<String, String>() {
                @Override
                public String apply(String s) {
                    return getAfter(s);
                }
            };
        }

        public boolean isChanged(String column) {
            Pair pair;
            return (pair = fieldValueMap.get(column)) != null && pair.changed;
        }

        /**
         * 将{@link Update}实例转化成{@link Delete}, 当然,能不能转换自己在外面判断
         *
         * @return delete实例
         */
        public Delete transferToDelete() {
            Delete delete = new Delete();
            for (Map.Entry<String, Pair> e : fieldValueMap.entrySet()) {
                delete.fieldValueMap.put(e.getKey(), e.getValue().before);
            }
            return delete;
        }

        /**
         * 将{@link Update}实例转化成{@link Insert}, 当然,能不能转换自己在外面判断
         *
         * @return insert实例
         */
        public Insert transferToInsert() {
            Insert insert = new Insert();
            for (Map.Entry<String, Pair> e : fieldValueMap.entrySet()) {
                insert.fieldValueMap.put(e.getKey(), e.getValue().after);
            }
            return insert;
        }

        @Override
        public String toString() {
            return CanalEntry.EventType.UPDATE.toString() + ':' + super.toString();
        }
    }

    /**
     * {@link Update}专用数据结果
     */
    public static class Pair implements Serializable {

        private static final long serialVersionUID = -7764854062126189422L;
        /**
         * 修改之前数据
         */
        private String before;
        /**
         * 修改之后数据
         */
        private String after;
        /**
         * 是否有改动
         */
        private boolean changed;

        public Pair(String before, String after, boolean changed) {
            this.before = before;
            this.after = after;
            this.changed = changed;
        }

        public String getAfter() {
            return after;
        }

        public String getBefore() {
            return before;
        }

        public boolean isChanged() {
            return changed;
        }

        @Override
        public String toString() {
            return before + ',' + after + ',' + changed;
        }
    }

    /**
     * 通过{@link CanalEntry.RowChange} 构造{@link RowChangedData}
     *
     * @param rowChange canal 对应修改的数据
     * @return 如果事件类型不对, 则返回null
     */
    public static List<RowChangedData> build(CanalEntry.RowChange rowChange, Set<String> interestedColumns) {
        List<RowChangedData> resultList = new ArrayList<>(rowChange.getRowDatasCount());
        switch (rowChange.getEventType()) {
            case INSERT:
                for (CanalEntry.RowData r : rowChange.getRowDatasList()) {
                    resultList.add(new Insert(r, interestedColumns));
                }
                break;
            case DELETE:
                for (CanalEntry.RowData r : rowChange.getRowDatasList()) {
                    resultList.add(new Delete(r, interestedColumns));
                }
                break;
            case UPDATE: {
                final Map<String, Pair> dataMap = new HashMap<>();
                final boolean isEmpty = CommonsUtils.isEmpty(interestedColumns);
                for (CanalEntry.RowData rowData : rowChange.getRowDatasList()) {
                    if (isEmpty) {
                        for (CanalEntry.Column c : rowData.getAfterColumnsList()) {
                            dataMap.put(c.getName(), new Pair(null, c.getIsNull() ? null : c.getValue(), c.getUpdated()));
                        }
                    } else {
                        boolean useless = true;
                        for (CanalEntry.Column c : rowData.getAfterColumnsList()) {
                            if (interestedColumns.contains(c.getName())) {
                                dataMap.put(c.getName(), new Pair(null, c.getIsNull() ? null : c.getValue(), c.getUpdated()));
                                if (useless && c.getUpdated()) {
                                    useless = false;
                                }
                            }
                        }
                        if (useless) continue;
                    }
                    for (CanalEntry.Column c : rowData.getBeforeColumnsList()) {
                        Pair p = dataMap.get(c.getName());
                        if (p != null) {
                            p.before = c.getIsNull() ? null : c.getValue();
                        }
                    }
                    resultList.add(new Update(dataMap));
                }
                break;
            }
            default:
                return null;
        }
        return resultList;
    }

    /**
     * 判断{@link RowChangedData}对象事件类型
     */
    public static CanalEntry.EventType getEventType(RowChangedData data) {
        return data instanceof RowChangedData.Update ? CanalEntry.EventType.UPDATE :
                data instanceof RowChangedData.Insert ? CanalEntry.EventType.INSERT : CanalEntry.EventType.DELETE;
    }

    public static final byte INSERT_TYPE_FLAG = 1;

    public static final byte UPDATE_TYPE_FLAG = 1 << 1;

    public static final byte DELETE_TYPE_FLAG = 1 << 2;

    public static byte getEventTypeFlag(CanalEntry.EventType eventType) {
        if (eventType == CanalEntry.EventType.INSERT) {
            return INSERT_TYPE_FLAG;
        } else if (eventType == CanalEntry.EventType.UPDATE) {
            return UPDATE_TYPE_FLAG;
        } else if (eventType == CanalEntry.EventType.DELETE) {
            return DELETE_TYPE_FLAG;
        } else {
            throw new UnsupportedOperationException(eventType + " is unsupported, only support INSERT, UPDATE, DELETE types");
        }
    }

}
