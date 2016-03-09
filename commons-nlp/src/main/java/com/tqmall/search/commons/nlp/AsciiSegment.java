package com.tqmall.search.commons.nlp;


import com.tqmall.search.commons.nlp.trie.TextMatch;

import java.util.List;
import java.util.Objects;

/**
 * Created by xing on 16/3/8.
 * ascii相关分词, 包括数字, 英文单词分词
 *
 * @author xing
 */
public class AsciiSegment implements TextMatch<Integer> {

    @Override
    public List<Hit<Integer>> match(char[] text) {
        Objects.requireNonNull(text);
        return match(text, 0, text.length);
    }

    /**
     * 拿到数字, 英文单词, 或者数字与英文连接词
     *
     * @return Hit中的value对应词的类型
     */
    @Override
    public List<Hit<Integer>> match(char[] text, int startPos, int length) {
        int endPos = startPos + length;
        for (int i = startPos; i < endPos; i++) {
            //TODO 实现英文, 数字分词
        }
        return null;
    }

}
