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

    protected final Map<String, V> fieldValueMap = new HashMap<>();

    public RowChangedData(Map<String, V> dataMap) {
        if (!CommonsUtils.isEmpty(dataMap)) {
            fieldValueMap.putAll(dataMap);
        }
    }

    @Override
    public V apply(String s) {
        return fieldValueMap.get(s);
    }

    public static final class Insert extends RowChangedData<String> {

        private static final long serialVersionUID = -2687037454927572799L;

        public static final Function<CanalEntry.RowData, Insert> CONVERT = new Function<CanalEntry.RowData, Insert>() {
            @Override
            public Insert apply(CanalEntry.RowData rowData) {
                if (rowData == null) return null;
                Insert insert = new Insert(null);
                for (CanalEntry.Column c : rowData.getAfterColumnsList()) {
                    insert.fieldValueMap.put(c.getName(), c.getValue());
                }
                return insert;
            }
        };

        public Insert(Map<String, String> dataMap) {
            super(dataMap);
        }

    }

    public static final class Delete extends RowChangedData<String> {

        private static final long serialVersionUID = 6254540878604970123L;

        public static final Function<CanalEntry.RowData, Delete> CONVERT = new Function<CanalEntry.RowData, Delete>() {
            @Override
            public Delete apply(CanalEntry.RowData rowData) {
                if (rowData == null) return null;
                Delete delete = new Delete(null);
                for (CanalEntry.Column c : rowData.getBeforeColumnsList()) {
                    delete.fieldValueMap.put(c.getName(), c.getValue());
                }
                return delete;
            }
        };

        public Delete(Map<String, String> dataMap) {
            super(dataMap);
        }

    }

    public static final class Update extends RowChangedData<Pair> {

        private static final long serialVersionUID = 585376007150297603L;

        public static final Function<CanalEntry.RowData, Update> CONVERT = new Function<CanalEntry.RowData, Update>() {
            @Override
            public Update apply(CanalEntry.RowData rowData) {
                if (rowData == null) return null;
                Update update = new Update(null);
                for (CanalEntry.Column c : rowData.getBeforeColumnsList()) {
                    update.fieldValueMap.put(c.getName(), new Pair(c.getValue(), null, false));
                }
                for (CanalEntry.Column c : rowData.getAfterColumnsList()) {
                    Pair p = update.fieldValueMap.get(c.getName());
                    if (p != null) {
                        p.after = c.getValue();
                        p.changed = c.getUpdated();
                    }
                }
                return update;
            }
        };

        public Update(Map<String, Pair> dataMap) {
            super(dataMap);
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
            Map<String, String> dataMap = new HashMap<>();
            for (Map.Entry<String, Pair> e : fieldValueMap.entrySet()) {
                dataMap.put(e.getKey(), e.getValue().before);
            }
            return new Delete(dataMap);
        }

        /**
         * 将{@link Update}实例转化成{@link Insert}, 当然,能不能转换自己在外面判断
         *
         * @return insert实例
         */
        public Insert transferToInsert() {
            Map<String, String> dataMap = new HashMap<>();
            for (Map.Entry<String, Pair> e : fieldValueMap.entrySet()) {
                dataMap.put(e.getKey(), e.getValue().after);
            }
            return new Insert(dataMap);
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
    }

    /**
     * 通过{@link CanalEntry.RowChange} 构造{@link RowChangedData}
     *
     * @param rowChange canal 对应修改的数据
     * @return 如果事件类型不对, 则返回一个空的List
     */
    public static List<RowChangedData> build(CanalEntry.RowChange rowChange) {
        Function<CanalEntry.RowData, ? extends RowChangedData> function;
        switch (rowChange.getEventType()) {
            case INSERT:
                function = Insert.CONVERT;
                break;
            case DELETE:
                function = Delete.CONVERT;
                break;
            case UPDATE:
                function = Update.CONVERT;
                break;
            default:
                function = null;
        }
        if (function != null) {
            List<RowChangedData> resultList = new ArrayList<>(rowChange.getRowDatasCount());
            for (CanalEntry.RowData r : rowChange.getRowDatasList()) {
                RowChangedData data = function.apply(r);
                if (data != null) {
                    resultList.add(data);
                }
            }
            return resultList;
        } else return null;
    }

    /**
     * 判断{@link RowChangedData}对象事件类型
     */
    public static CanalEntry.EventType getEventType(RowChangedData data) {
        return data instanceof RowChangedData.Update ? CanalEntry.EventType.UPDATE :
                data instanceof RowChangedData.Insert ? CanalEntry.EventType.INSERT : CanalEntry.EventType.DELETE;
    }

}
