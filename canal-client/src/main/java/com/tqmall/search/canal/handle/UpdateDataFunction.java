package com.tqmall.search.canal.handle;

import com.tqmall.search.canal.RowChangedData;
import com.tqmall.search.commons.lang.Function;

/**
 * Created by xing on 16/2/25.
 * update 更新实例获取指定field对应value的{@link Function}, 这样做仅仅是为了避免频繁的创建匿名{@link Function}对象
 * 可以如下代码使用:
 * <pre>{@code
 *
 *  RowChangedData.Update update = it.next();
 *  UpdateDataFunction.setUpdateData(update);
 *  try {
 *      //do some work
 *  } finally {
 *      //要记得清楚掉, 避免内存泄露
 *      UpdateDataFunction.setUpdateData(null);
 *  }
 *
 * }</pre>
 *
 * @see RowChangedData.Update
 */
final class UpdateDataFunction {

    private final static ThreadLocal<UpdateDataFunction> INSTANCE = new ThreadLocal<UpdateDataFunction>() {
        @Override
        protected UpdateDataFunction initialValue() {
            return new UpdateDataFunction();
        }
    };

    private UpdateDataFunction() {
    }

    /**
     * 获取当前线程设置的{@link #updateData}对象
     */
    public static RowChangedData.Update currentUpdateData() {
        return INSTANCE.get().updateData;
    }

    public static void setUpdateData(RowChangedData.Update updateData) {
        INSTANCE.get().updateData = updateData;
    }

    /**
     * 使用返回的{@link Function}之前必须先调用{@link #setUpdateData(RowChangedData.Update)}
     *
     * @return {@link RowChangedData.Update} before 各个column的{@link Function}
     */
    public static Function<String, String> before() {
        return INSTANCE.get().beforeFunction;
    }

    /**
     * 使用返回的{@link Function}之前必须先调用{@link #setUpdateData(RowChangedData.Update)}
     *
     * @return {@link RowChangedData.Update} after 各个column的{@link Function}
     */
    public static Function<String, String> after() {
        return INSTANCE.get().afterFunction;
    }

    private RowChangedData.Update updateData;

    private final Function<String, String> beforeFunction = new Function<String, String>() {
        @Override
        public String apply(String column) {
            return updateData == null ? null : updateData.getBefore(column);
        }
    };

    private final Function<String, String> afterFunction = new Function<String, String>() {
        @Override
        public String apply(String column) {
            return updateData == null ? null : updateData.getAfter(column);
        }
    };
}
