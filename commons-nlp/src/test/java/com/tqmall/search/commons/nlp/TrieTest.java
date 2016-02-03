package com.tqmall.search.commons.nlp;

import com.tqmall.search.commons.nlp.trie.*;
import org.junit.AfterClass;
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

    private static TrieNodeFactory<String> cjkNodeFactory;

    @BeforeClass
    public static void init() {
        cjkNodeFactory = new TrieNodeFactory<String>() {
            @Override
            public Node<String> createRootNode() {
                return LargeRootNode.createCjkRootNode();
            }

            @Override
            public Node<String> createNormalNode(char c) {
                return new NormalNode<>(c);
            }

            @Override
            public Node<String> createChildNode(char c, String value) {
                return new NormalNode<>(c, value);
            }
        };
        binaryTrie = new BinaryTrie<>(cjkNodeFactory);
    }

    @AfterClass
    public static void clear() {
        binaryTrie.clear();
        binaryTrie = null;
        cjkNodeFactory = null;
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

        Assert.assertEquals(dataMap.size(), binaryTrie.size());

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

        System.out.println("删除测试");
        Assert.assertFalse(binaryTrie.remove("一心一"));
        Assert.assertTrue(binaryTrie.remove("一"));
        Assert.assertEquals(dataMap.size() - 1, binaryTrie.size());
        binaryTriePrefixSearch("一", 3);
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

    @Test
    public void binaryMatchTrieTest() {
        BinaryMatchTrie<String> matchTrie = new BinaryMatchTrie<>(cjkNodeFactory);
        matchTrie.put("长", "zhang");
        matchTrie.put("长沙", "chang sha");
        matchTrie.put("沙", "sha");
        matchTrie.put("星星", "xingxing");
        matchTrie.put("王", "wang");
        matchTrie.put("王星星", "xingxing.wang");

        String word = "长沙王星星";
        List<Hit<String>> result = matchTrie.textMaxMatch(word);
        System.out.println("word: " + word + " binaryMatchTrie result: " + result);
        List<Hit<String>> expectedResult = new ArrayList<>();
        expectedResult.add(new Hit<>(2, "长沙", "chang sha"));
        expectedResult.add(new Hit<>(5, "王星星", "xingxing.wang"));
        Assert.assertEquals(expectedResult, result);

        word = "长沙王星艳王星星";
        result = matchTrie.textMaxMatch(word);
        System.out.println("word: " + word + " binaryMatchTrie result: " + result);
        expectedResult.clear();
        expectedResult.add(new Hit<>(2, "长沙", "chang sha"));
        expectedResult.add(new Hit<>(3, "王", "wang"));
        expectedResult.add(new Hit<>(8, "王星星", "xingxing.wang"));
        Assert.assertEquals(expectedResult, result);

        word = "长沙王星艳王星星";
        result = matchTrie.textMinMatch(word);
        System.out.println("word: " + word + " binaryMatchTrie result: " + result);
        expectedResult.clear();
        expectedResult.add(new Hit<>(1, "长", "zhang"));
        expectedResult.add(new Hit<>(2, "沙", "sha"));
        expectedResult.add(new Hit<>(3, "王", "wang"));
        expectedResult.add(new Hit<>(6, "王", "wang"));
        expectedResult.add(new Hit<>(8, "星星", "xingxing"));
        Assert.assertEquals(expectedResult, result);
    }
}
