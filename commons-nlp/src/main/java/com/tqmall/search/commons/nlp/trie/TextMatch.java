package com.tqmall.search.commons.nlp.trie;

import com.tqmall.search.commons.nlp.Hit;

import java.util.List;

/**
 * Created by xing on 16/3/4.
 * 文本匹配接口定义
 */
public interface TextMatch<V> {
    /**
     * 匹配字符串, 未匹配的字符串不做任何处理
     *
     * @param text 需要匹配的文本
     * @return 匹配结果
     */
    List<Hit<V>> match(char[] text);

    /**
     * 匹配字符串, 未匹配的字符串不做任何处理
     *
     * @param text     需要匹配的文本
     * @param startPos 开始下标
     * @param length   char数组的长度
     * @return 匹配结果
     */
    List<Hit<V>> match(char[] text, int startPos, int length);
}
