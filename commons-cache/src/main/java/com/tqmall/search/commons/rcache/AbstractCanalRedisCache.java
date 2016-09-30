package com.tqmall.search.commons.rcache;

import com.tqmall.search.canal.RowChangedData;
import com.tqmall.search.canal.action.Actions;
import com.tqmall.search.canal.action.EventTypeAction;
import com.tqmall.search.canal.action.TableAction;
import com.tqmall.search.commons.lang.Function;
import com.tqmall.search.redis.RedisClient;

import java.util.ArrayList;
import java.util.List;

/**
 * date 16/4/19 下午11:20
 * 缓存支持canal实时更新
 * cache实时更新的Action实现为{@link EventTypeAction}, 可以通过{@link Actions#convert(EventTypeAction)}转换为{@link TableAction}
 *
 * @author 尚辰
 * @see Actions#convert(EventTypeAction)
 */
public abstract class AbstractCanalRedisCache<K, V> extends AbstractRedisCache<K, V> implements EventTypeAction {

    protected final String keyFiled;

    protected AbstractCanalRedisCache(RedisClient client, Class<V> valueCls, String keyFiled) {
        super(client, valueCls);
        this.keyFiled = keyFiled;
    }

    protected AbstractCanalRedisCache(RedisClient client, Class<V> valueCls,
                                      String mapKey, String keyFiled) {
        super(client, valueCls, mapKey);
        this.keyFiled = keyFiled;
    }

    /**
     * 从canal的更新中实例化value
     *
     * @param data canal的更新数据对象
     */
    protected abstract V initValue(Function<String, String> data);

    @Override
    public void onDeleteAction(List<RowChangedData.Delete> deletedData) {
        List<String> delKeys = new ArrayList<>();
        for (RowChangedData.Delete d : deletedData) {
            String k;
            if ((k = d.apply(keyFiled)) != null) {
                delKeys.add(k);
            }
        }
        if (!delKeys.isEmpty()) {
            client.hDel(mapKey, delKeys.toArray(new String[delKeys.size()]));
        }
    }

    @Override
    public void onInsertAction(List<RowChangedData.Insert> insertedData) {
        for (RowChangedData.Insert insert : insertedData) {
            String k = insert.apply(keyFiled);
            if (k == null) continue;
            V v = initValue(insert);
            if (v != null) {
                client.hSet(mapKey, k, v);
            }
        }
    }

    @Override
    public void onUpdateAction(List<RowChangedData.Update> updatedData) {
        for (RowChangedData.Update update : updatedData) {
            String k;
            if (update.isChanged(keyFiled) && (k = update.getBefore(keyFiled)) != null) {
                client.hDel(mapKey, k);
            }
            k = update.getAfter(keyFiled);
            V v = initValue(update.getAfters());
            if (k != null && v != null) {
                client.hSet(mapKey, k, v);
            }
        }
    }
}
