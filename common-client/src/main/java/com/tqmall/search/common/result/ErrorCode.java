package com.tqmall.search.common.result;

/**
 * Created by xing on 15/12/13.
 * 错误码接口
 */
public interface ErrorCode {

    /**
     * 获取错误码
     * @return 错误码
     */
    String getCode();

    /**
     * 该错误码对应的Message
     * @return 错误提示信息
     */
    String getMessage();
}
