package com.tqmall.search.canal;

import com.alibaba.otter.canal.protocol.CanalEntry;
import com.tqmall.search.commons.lang.Function;
import com.tqmall.search.commons.utils.CommonsUtils;

import java.io.Serializable;
import java.util.*;

/**
 * Created by xing on 16/2/22.
 * {@link CanalEntry}中一行数据改动封装
 * 对于RowChangedData对象, 可以通过{@link #getEventType(RowChangedData)}判断对象类型
 *
 * @see #getEventType(RowChangedData)
 */
public abstract class RowChangedData<V> implements Function<String, V>, Serializable {

    private static final long serialVersionUID = -8712239138384357603L;

    final Map<String, V> fieldValueMap = new HashMap<>();

    RowChangedData() {
    }

    RowChangedData(CanalEntry.RowData rowData, Set<String> interestedColumns) {
        initByRowData(rowData, interestedColumns);
    }

    @Override
    public V apply(String s) {
        return fieldValueMap.get(s);
    }

    @Override
    public String toString() {
        return fieldValueMap.toString();
    }

    /**
     * 通过{@link CanalEntry.RowData}初始化数据, 仅限于内部调用
     *
     * @param rowData           canal变化的列值
     * @param interestedColumns 感兴趣的列, 如果{@link CommonsUtils#isEmpty(Collection)}为true则全部包含
     */
    abstract void initByRowData(CanalEntry.RowData rowData, Set<String> interestedColumns);

    public static final class Insert extends RowChangedData<String> {

        private static final long serialVersionUID = -2687037454927572799L;

        Insert() {
        }

        public Insert(CanalEntry.RowData rowData, Set<String> interestedColumns) {
            super(rowData, interestedColumns);
        }

        @Override
        public String toString() {
            return CanalEntry.EventType.INSERT.toString() + ':' + super.toString();
        }

        @Override
        protected final void initByRowData(CanalEntry.RowData rowData, Set<String> interestedColumns) {
            final boolean isEmpty = CommonsUtils.isEmpty(interestedColumns);
            for (CanalEntry.Column c : rowData.getAfterColumnsList()) {
                if (isEmpty || interestedColumns.contains(c.getName())) {
                    fieldValueMap.put(c.getName(), c.getValue());
                }
            }
        }
    }

    public static final class Delete extends RowChangedData<String> {

        private static final long serialVersionUID = 6254540878604970123L;

        Delete() {
        }

        public Delete(CanalEntry.RowData rowData, Set<String> interestedColumns) {
            super(rowData, interestedColumns);
        }

        @Override
        public String toString() {
            return CanalEntry.EventType.DELETE.toString() + ':' + super.toString();
        }

        @Override
        void initByRowData(CanalEntry.RowData rowData, Set<String> interestedColumns) {
            final boolean isEmpty = CommonsUtils.isEmpty(interestedColumns);
            for (CanalEntry.Column c : rowData.getBeforeColumnsList()) {
                if (isEmpty || interestedColumns.contains(c.getName())) {
                    fieldValueMap.put(c.getName(), c.getValue());
                }
            }
        }
    }

    public static final class Update extends RowChangedData<Pair> {

        private static final long serialVersionUID = 585376007150297603L;

        public Update(CanalEntry.RowData rowData, Set<String> interestedColumns) {
            super(rowData, interestedColumns);
        }

        @Override
        final void initByRowData(CanalEntry.RowData rowData, Set<String> interestedColumns) {
            final boolean isEmpty = CommonsUtils.isEmpty(interestedColumns);
            for (CanalEntry.Column c : rowData.getBeforeColumnsList()) {
                if (isEmpty || interestedColumns.contains(c.getName())) {
                    fieldValueMap.put(c.getName(), new Pair(c.getValue(), null, false));
                }
            }
            for (CanalEntry.Column c : rowData.getAfterColumnsList()) {
                Pair p = fieldValueMap.get(c.getName());
                if (p != null) {
                    p.after = c.getValue();
                    p.changed = c.getUpdated();
                }
            }
        }

        public String getBefore(String column) {
            Pair pair;
            return (pair = fieldValueMap.get(column)) == null ? null : pair.before;
        }

        public String getAfter(String column) {
            Pair pair;
            return (pair = fieldValueMap.get(column)) == null ? null : pair.after;
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
            case UPDATE:
                for (CanalEntry.RowData r : rowChange.getRowDatasList()) {
                    resultList.add(new Update(r, interestedColumns));
                }
                break;
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
