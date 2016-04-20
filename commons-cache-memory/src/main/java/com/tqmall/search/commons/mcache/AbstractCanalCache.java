package com.tqmall.search.commons.mcache;

import com.tqmall.search.canal.RowChangedData;
import com.tqmall.search.canal.action.Actions;
import com.tqmall.search.canal.action.EventTypeAction;
import com.tqmall.search.canal.action.TableAction;
import com.tqmall.search.commons.lang.Function;
import com.tqmall.search.commons.lang.StrValueConvert;
import com.tqmall.search.commons.utils.StrValueConverts;

import java.util.List;

/**
 * Created by xing on 16/3/29.
 * 能够支持通过canal实时更新的{@link AbstractStrongCache}
 * cache实时更新的Action实现为{@link EventTypeAction}, 可以通过{@link Actions#convert(EventTypeAction)}转换为{@link TableAction}
 *
 * @author xing
 * @see Actions#convert(EventTypeAction)
 */
public abstract class AbstractCanalCache<K, V> extends AbstractStrongCache<K, V> implements EventTypeAction {

    protected final String keyField;

    protected final StrValueConvert<K> keyConvert;

    protected AbstractCanalCache(String keyField, Class<K> keyCls) {
        this.keyField = keyField;
        this.keyConvert = StrValueConverts.getBasicConvert(keyCls);
    }

    protected AbstractCanalCache(String keyField, StrValueConvert<K> keyConvert) {
        this.keyField = keyField;
        this.keyConvert = keyConvert;
    }

    protected abstract V initValue(Function<String, String> data);

    @Override
    public void onUpdateAction(List<RowChangedData.Update> updatedData) {
        if (initialized()) {
            for (RowChangedData.Update update : updatedData) {
                String k;
                if (update.isChanged(keyFiled) && (k = update.getBefore(keyFiled)) != null) {
                    //如果key修改, 那先remove, 再add
                    updateValue(keyConvert.convert(k), null);
                }
                if ((k = update.getAfter(keyFiled)) != null) {
                    updateValue(keyConvert.convert(k), initValue(update.getAfters()));
                }
            }
        }
    }

    @Override
    public void onInsertAction(List<RowChangedData.Insert> insertedData) {
        if (initialized()) {
            for (RowChangedData.Insert insert : insertedData) {
                String k;
                if ((k = insert.apply(keyFiled)) != null) {
                    updateValue(keyConvert.convert(k), initValue(insert));
                }
            }
        }
    }

    @Override
    public void onDeleteAction(List<RowChangedData.Delete> deletedData) {
        if (initialized()) {
            for (RowChangedData.Delete delete : deletedData) {
                String k;
                if ((k = delete.apply(keyFiled)) != null) {
                    updateValue(keyConvert.convert(k), null);
                }
            }
        }
    }
}
