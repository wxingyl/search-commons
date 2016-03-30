package com.tqmall.search.vo;


import com.google.common.collect.Lists;
import com.tqmall.search.common.result.ErrorCode;
import com.tqmall.search.common.utils.ErrorCodeUtils;

import java.util.List;

/**
 * Created by xing on 15/12/4.
 * 错误类型记录
 */
public enum SearchErrorCode implements ErrorCode {

    /**
     * 发生未知异常的错误, 也就是应该发生500的接口
     * %s 是发生异常的message
     */
    SERVICE_ERROR(0, "系统内部错误: %s"),
    /**
     * 通用错误异常, 自己随便Message
     */
    CUSTOM_ERROR(1, "%s"),
    AUTHORIZATION_FAILED(2, "授权失败"),
    ILLEGAL_ARG(3, "参数异常"),
    ILLEGAL_ARG_VALUE(4, "参数值: %s 无效"),

    /**
     * 全量索引相关操作
     */
    INDEX_SALVE_WOP_FORBID(121, "slave机器禁止修改索引"),
    FULL_INDEX_FREQUENT(122, "更新索引过于频繁, 之间间隔至少%d s"),
    FULL_INDEX_RUNTIME_ERROR(123, "执行全量索引创建异常: %s"),
    INDEX_OP_ACTION_NOT_FOUND(124, "索引key: %s 对应的IndexOperatorAction不存在");

    static {
        ErrorCodeUtils.setSystemCode(801);
        List<SearchErrorCode> warnList = Lists.newArrayList(AUTHORIZATION_FAILED, FULL_INDEX_FREQUENT, INDEX_OP_ACTION_NOT_FOUND);
        for (SearchErrorCode e : warnList) {
            e.code = ErrorCodeUtils.buildWarnCode(e.exceptionCode);
        }
        for (SearchErrorCode e : values()) {
            if (e.code != null || e == INDEX_SALVE_WOP_FORBID) continue;
            e.code = ErrorCodeUtils.buildErrorCode(e.exceptionCode);
        }
        INDEX_SALVE_WOP_FORBID.code = ErrorCodeUtils.buildFatalCode(INDEX_SALVE_WOP_FORBID.exceptionCode);
        ErrorCodeUtils.removeSystemCode();
    }

    private int exceptionCode;

    private String code;

    private String message;

    SearchErrorCode(int exceptionCode, String message) {
        this.exceptionCode = exceptionCode;
        this.message = message;
    }

    public SearchErrorCode formatMessage(Object... args) {
        this.message = String.format(this.message, args);
        return this;
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
