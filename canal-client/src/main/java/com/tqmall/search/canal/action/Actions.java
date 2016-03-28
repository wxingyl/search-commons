package com.tqmall.search.canal.action;

import com.alibaba.otter.canal.protocol.CanalEntry;
import com.tqmall.search.canal.RowChangedData;

import java.util.*;

/**
 * Created by xing on 16/3/28.
 * Action 相关操作类
 *
 * @author xing
 */
public final class Actions {

    private Actions() {
    }

    /**
     * 将{@link EventTypeAction}转化为{@link TableAction}
     * 注意: 返回结果中, 调用方法时{@link TableAction#onAction(List)}, 参数list不能修改{@link Collections#unmodifiableList(List)}
     *
     * @param eventTypeAction 需要转化的eventTypeAction
     * @return 对应的TableAction对象
     * @see ConvertEventTypeAction#doAction(List, CanalEntry.EventType, int, int)
     */
    public static TableAction convert(EventTypeAction eventTypeAction) {
        return new ConvertEventTypeAction(eventTypeAction);
    }

    static class ConvertEventTypeAction implements TableAction {

        private final EventTypeAction eventTypeAction;

        ConvertEventTypeAction(EventTypeAction eventTypeAction) {
            Objects.requireNonNull(eventTypeAction);
            this.eventTypeAction = eventTypeAction;
        }

        @Override
        public void onAction(final List<? extends RowChangedData> changedData) {
            Iterator<? extends RowChangedData> it = changedData.iterator();
            CanalEntry.EventType lastType = null;
            int startIndex = 0, endIndex = 0;
            while (it.hasNext()) {
                RowChangedData curData = it.next();
                CanalEntry.EventType curType = RowChangedData.getEventType(curData);
                endIndex++;
                if (lastType == null) {
                    lastType = curType;
                } else if (lastType != curType) {
                    doAction(changedData, lastType, startIndex, endIndex);
                    startIndex = endIndex;
                    lastType = null;
                }
            }
            if (lastType != null) {
                doAction(changedData, lastType, startIndex, endIndex);
            }
        }

        @SuppressWarnings({"rawstype", "unchecked"})
        private void doAction(List<? extends RowChangedData> changedData, CanalEntry.EventType eventType, int startPos, int endPos) {
            if (eventType == CanalEntry.EventType.UPDATE) {
                eventTypeAction.onUpdateAction(Collections.unmodifiableList((List<RowChangedData.Update>)
                        changedData.subList(startPos, endPos)));
            } else if (eventType == CanalEntry.EventType.INSERT) {
                eventTypeAction.onInsertAction(Collections.unmodifiableList((List<RowChangedData.Insert>)
                        changedData.subList(startPos, endPos)));
            } else {
                eventTypeAction.onDeleteAction(Collections.unmodifiableList((List<RowChangedData.Delete>)
                        changedData.subList(startPos, endPos)));
            }
        }
    }

}
