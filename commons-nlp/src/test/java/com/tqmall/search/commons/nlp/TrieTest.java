package com.tqmall.search.commons.nlp;

import com.tqmall.search.commons.nlp.trie.BinaryTrie;
import com.tqmall.search.commons.nlp.trie.CjkRootNode;
import com.tqmall.search.commons.nlp.trie.Trie;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.*;

/**
 * Created by xing on 16/1/28.
 * tire相关测试
 */
public class TrieTest {

    private static Trie<String> binaryTrie;

    @BeforeClass
    public static void init() {
        binaryTrie = new BinaryTrie<>(new CjkRootNode<String>());
    }

    @Test
    public void binaryTrieTest() {
        Map<String, String> dataMap = new HashMap<>();
        dataMap.put("一", "yi");
        dataMap.put("一心一意", "yi xin yi yi");
        dataMap.put("一切", "yi qie");
        dataMap.put("一心", "yi xin");
        dataMap.put("王星星", "wang xing xing");
        dataMap.put("星星", "xing xing");
        dataMap.put("王星", "wang xing");
        dataMap.put("王", "wang");
        dataMap.put("王xing", "wang");
        dataMap.put("老王", "lao wang");

        for (Map.Entry<String, String> e : dataMap.entrySet()) {
            binaryTrie.put(e.getKey(), e.getValue());
        }
        String key = "yi心";
        try {
            binaryTrie.put(key, null);
            throw new AssertionError("插入非法的词\"" + key + "\"没有抛出异常");
        } catch (IllegalArgumentException e) {
            System.out.println("正常抛出异常: " + e.getMessage());
        }

        binaryTrieGetValue(dataMap, "一");
        binaryTrieGetValue(dataMap, "一切");
        for (String k : dataMap.keySet()) {
            binaryTrieGetValue(dataMap, k);
        }
        binaryTrieGetValue(dataMap, "一心一");

        binaryTriePrefixSearch("一", 4);
        binaryTriePrefixSearch("一心", 2);
        binaryTriePrefixSearch("一心一", 1);

        binaryTriePrefixSearch("星", 1);
        binaryTriePrefixSearch("王", 4);
        binaryTriePrefixSearch("星星", 1);
        binaryTriePrefixSearch("老", 1);
        binaryTriePrefixSearch("王xi", 1);
        binaryTriePrefixSearch("王星", 2);
    }

    private void binaryTrieGetValue(Map<String, String> dataMap, String key) {
        String ret = binaryTrie.getValue(key);
        Assert.assertEquals("binaryTrieGetValue, key: " + key + ", getValue = " + ret, dataMap.get(key), ret);
    }

    private void binaryTriePrefixSearch(String key, int expected) {
        List<Map.Entry<String, String>> list = binaryTrie.prefixSearch(key);
        System.out.println("binaryTriePrefixSearch, key: " + key + ", list: " + list);
        Set<String> keySet = new HashSet<>();
        for (Map.Entry<String, String> e : list) {
            keySet.add(e.getKey());
        }
        Assert.assertEquals(expected, keySet.size());
    }

}
