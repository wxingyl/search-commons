package com.tqmall.search.commons.match;

import com.tqmall.search.commons.nlp.NlpUtils;

import java.util.List;

/**
 * Created by xing on 16/3/18.
 * TextMatch抽象类定义, 提供{@link #match(String)}方法
 *
 * @author xing
 */
public abstract class AbstractTextMatch<V> {

    /**
     * 匹配字符串, 未匹配的字符串不做任何处理
     *
     * @param text     需要匹配的文本
     * @param off 开始下标
     * @param len   char数组的长度
     * @return 匹配结果
     */
    public abstract List<Hit<V>> match(char[] text, int off, int len);

    public final List<Hit<V>> match(String text) {
        char[] textArray = NlpUtils.stringToCharArray(text);
        if (textArray == null) return null;
        return match(textArray, 0, textArray.length);
    }

}
