package com.tqmall.search.commons.nlp.trie;

import com.tqmall.search.commons.nlp.Hits;

/**
 * Created by xing on 16/3/4.
 * 文本匹配接口定义
 */
public interface TextMatch<V> {
    /**
     * 匹配字符串
     *
     * @param text 需要匹配的文本
     * @return 匹配结果
     */
    Hits<V> textMatch(char[] text);
}
