package com.tqmall.search.commons.result;

/**
 * Created by xing on 15/12/13.
 * 错误码接口
 *
 * @see com.tqmall.search.commons.utils.ErrorCodeBuilder errorCode对象创建器
 */
public interface ErrorCode {

    /**
     * 获取错误码
     *
     * @return 错误码
     */
    String getCode();

    /**
     * 该错误码对应的Message
     *
     * @return 错误提示信息
     */
    String getMessage();

    enum Level {
        FATAL(1),
        ERROR(2),
        WARN(3);

        private int code;

        Level(int code) {
            this.code = code;
        }

        public int getCode() {
            return code;
        }

        /**
         * 根据code值获取对应level
         *
         * @param code level对应的code值
         * @return 如果没有找到, 抛出{@link IllegalArgumentException}
         */
        public static Level valueOf(int code) {
            for (Level l : values()) {
                if (l.code == code) return l;
            }
            throw new IllegalArgumentException("can not find code = " + code + " Level");
        }
    }
}
