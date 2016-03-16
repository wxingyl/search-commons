package com.tqmall.search.commons.algorithm;

import com.tqmall.search.commons.match.Hit;
import com.tqmall.search.commons.match.MatchBinaryReverseTrie;
import com.tqmall.search.commons.trie.Node;
import com.tqmall.search.commons.trie.NormalNode;
import com.tqmall.search.commons.trie.RootNodeType;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.*;

/**
 * Created by xing on 16/3/16.
 * MatchBinaryReverseTrie test class
 *
 * @author xing
 */
public class MatchBinaryReverseTrieTest {

    private static MatchBinaryReverseTrie<Void> reverseTrie;

    @BeforeClass
    public static void init() {
        List<String> lexicon = new ArrayList<>();
        lexicon.add("地铁");
        lexicon.add("北京大学地铁站");
        lexicon.add("北京大学");
        lexicon.add("北京");
        lexicon.add("大学");
        lexicon.add("清华地铁站");
        lexicon.add("西湖地铁站");
        lexicon.add("杭州西湖");
        lexicon.add("西湖");
        lexicon.add("杭州");
        reverseTrie = new MatchBinaryReverseTrie<>(RootNodeType.NORMAL.<Void>defaultTrie());
        for (String s : lexicon) {
            reverseTrie.put(s, null);
        }
    }

    @AfterClass
    public static void destroy() {
        reverseTrie.clear();
        reverseTrie = null;
    }

    /**
     * read interface test
     */
    @Test
    public void readTest() {
        Node<Void> node = reverseTrie.getNode("西湖");
        Node<Void> expectNode = new NormalNode<>('西', Node.Status.WORD, null);
        Assert.assertEquals(expectNode, node);

        node = reverseTrie.getNode("地铁站");
        expectNode = new NormalNode<>('地', Node.Status.NORMAL, null);
        Assert.assertEquals(expectNode, node);

        node = reverseTrie.getNode("北京");
        expectNode = new NormalNode<>('北', Node.Status.LEAF_WORD, null);
        Assert.assertEquals(expectNode, node);

        node = reverseTrie.getNode("北京大学地铁站");
        expectNode = new NormalNode<>('北', Node.Status.LEAF_WORD, null);
        Assert.assertEquals(expectNode, node);
    }

    @Test
    public void removeTest() {
        Assert.assertFalse(reverseTrie.remove("地铁站"));
        Assert.assertTrue(reverseTrie.remove("西湖"));
        Node<Void> node = reverseTrie.getNode("西湖");
        Node<Void> expectNode = new NormalNode<>('西', Node.Status.NORMAL, null);
        Assert.assertEquals(expectNode, node);
        node = reverseTrie.getNode("杭州西湖");
        expectNode = new NormalNode<>('杭', Node.Status.LEAF_WORD, null);
        Assert.assertEquals(expectNode, node);

        Assert.assertTrue(reverseTrie.put("西湖", null));
        node = reverseTrie.getNode("西湖");
        expectNode = new NormalNode<>('西', Node.Status.WORD, null);
        Assert.assertEquals(expectNode, node);
    }

    @Test
    public void prefixSearchTest() {
        final Comparator<Map.Entry<String, Void>> cmp = new Comparator<Map.Entry<String, Void>>() {
            @Override
            public int compare(Map.Entry<String, Void> o1, Map.Entry<String, Void> o2) {
                return o1.getKey().compareTo(o2.getKey());
            }
        };
        List<Map.Entry<String, Void>> result = reverseTrie.prefixSearch("地铁站");
        Collections.sort(result, cmp);
        List<Map.Entry<String, Void>> expect = new ArrayList<>();
        expect.add(new AbstractMap.SimpleImmutableEntry<String, Void>("北京大学地铁站", null));
        expect.add(new AbstractMap.SimpleImmutableEntry<String, Void>("清华地铁站", null));
        expect.add(new AbstractMap.SimpleImmutableEntry<String, Void>("西湖地铁站", null));
        Collections.sort(expect, cmp);
        Assert.assertEquals(expect, result);

        result = reverseTrie.prefixSearch("湖");
        Collections.sort(result, cmp);
        expect = new ArrayList<>();
        expect.add(new AbstractMap.SimpleImmutableEntry<String, Void>("杭州西湖", null));
        expect.add(new AbstractMap.SimpleImmutableEntry<String, Void>("西湖", null));
        Collections.sort(expect, cmp);
        Assert.assertEquals(expect, result);

        result = reverseTrie.prefixSearch("京");
        Collections.sort(result, cmp);
        expect = new ArrayList<>();
        expect.add(new AbstractMap.SimpleImmutableEntry<String, Void>("北京", null));
        Collections.sort(expect, cmp);
        Assert.assertEquals(expect, result);

        Assert.assertNull(reverseTrie.prefixSearch("星"));
    }

    @Test
    public void matchTest() {
        String text = "北京大学地铁站和杭州西湖";
        Assert.assertTrue(reverseTrie.put("地铁站", null));
        List<Hit<Void>> hits = reverseTrie.maxMatch(text);
        List<Hit<Void>> expectHits = new ArrayList<>();
        expectHits.add(new Hit<Void>(0, "北京大学地铁站", null));
        expectHits.add(new Hit<Void>(8, "杭州西湖", null));
        Assert.assertEquals(expectHits, hits);

        hits = reverseTrie.minMatch(text);
        expectHits = new ArrayList<>();
        expectHits.add(new Hit<Void>(0, "北京", null));
        expectHits.add(new Hit<Void>(2, "大学", null));
        expectHits.add(new Hit<Void>(4, "地铁站", null));
        expectHits.add(new Hit<Void>(8, "杭州", null));
        expectHits.add(new Hit<Void>(10, "西湖", null));
        Assert.assertEquals(expectHits, hits);
        Assert.assertTrue(reverseTrie.remove("地铁站"));
    }

}
