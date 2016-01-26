package com.tqmall.search.commons.exception;

/**
 * Created by xing on 16/1/26.
 * 加载词库文件异常
 */
public class LoadLexiconException extends RuntimeException {

    private static final long serialVersionUID = 7202255526214130763L;

    public LoadLexiconException(String lexiconFileName, Throwable cause) {
        super("加载词库文件" + lexiconFileName + "发生异常", cause);
    }

}
