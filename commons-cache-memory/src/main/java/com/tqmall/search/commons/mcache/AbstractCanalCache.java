package com.tqmall.search.commons.mcache;

import com.tqmall.search.canal.RowChangedData;
import com.tqmall.search.canal.action.EventTypeAction;
import com.tqmall.search.commons.lang.Function;
import com.tqmall.search.commons.lang.StrValueConvert;
import com.tqmall.search.commons.utils.StrValueConverts;

import java.util.List;

/**
 * Created by xing on 16/3/29.
 * 能够支持通过canal实时更新的{@link AbstractStrongCache}
 *
 * @author xing
 */
public abstract class AbstractCanalCache<K, V> extends AbstractStrongCache<K, V> implements EventTypeAction {

    protected final String keyFiled;

    protected final StrValueConvert<K> keyConvert;

    protected AbstractCanalCache(String keyFiled, Class<K> keyCls) {
        this.keyFiled = keyFiled;
        this.keyConvert = StrValueConverts.getBasicConvert(keyCls);
    }

    protected AbstractCanalCache(String keyFiled, StrValueConvert<K> keyConvert) {
        this.keyFiled = keyFiled;
        this.keyConvert = keyConvert;
    }

    protected abstract V initValue(Function<String, String> data);

    @Override
    public void onUpdateAction(List<RowChangedData.Update> updatedData) {
        if (initialized()) {
            for (RowChangedData.Update update : updatedData) {
                if (update.isChanged(keyFiled)) {
                    //如果key修改, 那先remove, 再add
                    updateValue(update.getBefore(keyFiled, keyConvert), null);
                }
                updateValue(update.getAfter(keyFiled, keyConvert), initValue(update.getAfters()));
            }
        }
    }

    @Override
    public void onInsertAction(List<RowChangedData.Insert> insertedData) {
        if (initialized()) {
            for (RowChangedData.Insert insert : insertedData) {
                updateValue(keyConvert.convert(insert.apply(keyFiled)), initValue(insert));
            }
        }
    }

    @Override
    public void onDeleteAction(List<RowChangedData.Delete> deletedData) {
        if (initialized()) {
            for (RowChangedData.Delete delete : deletedData) {
                updateValue(keyConvert.convert(delete.apply(keyFiled)), null);
            }
        }
    }
}
