package com.tqmall.search.common.cache;

import com.tqmall.search.common.result.ErrorCode;
import com.tqmall.search.common.utils.ErrorCodeUtils;

/**
 * Created by xing on 15/12/28.
 * Utils中的ErrorCode, 只在util内部使用
 */
public enum CacheErrorCode implements ErrorCode {

    //notify相关错误码
    NOTIFY_HANDLE_ARG_INVALID(1, "参数不全, notifier无法处理外部请求"),
    NOTIFY_RUNTIME_ERROR(2, "notifier执行出错, %s"),
    NOTIFY_UNREGISTER_UNKNOWN_HOST(3, "未知的Host地址: %s, notifier无法处理注销请求"),

    RECEIVER_RUNTIME_ERROR(21, "receiver执行出错, %s"),

    HOST_INFO_INVALID(41, "HostInfo对象值错误: %s");

    static {
        ErrorCodeUtils.setSystemCode(803);
        for (CacheErrorCode e : CacheErrorCode.values()) {
            e.code = ErrorCodeUtils.buildErrorCode(e.exceptionCode);
        }
        ErrorCodeUtils.removeSystemCode();
    }

    private int exceptionCode;

    private String code;

    private String message;

    CacheErrorCode(int exceptionCode, String message) {
        this.exceptionCode = exceptionCode;
        this.message = message;
    }

    @Override
    public String getCode() {
        return code;
    }

    @Override
    public String getMessage() {
        return message;
    }
}
