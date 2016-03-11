package com.tqmall.search.commons.nlp;

import com.tqmall.search.commons.nlp.trie.AcBinaryTrie;
import com.tqmall.search.commons.nlp.trie.NodeFactories;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.HashSet;
import java.util.Set;

/**
 * Created by xing on 16/2/1.
 * AcTrie test
 */
public class AcTrieTest {

    private static AcBinaryTrie<Integer> acStrBinaryTrie;

    @BeforeClass
    public static void init() {
        acStrBinaryTrie = AcBinaryTrie.<Integer>build()
                .put("he", null)
                .put("she", null)
                .put("his", null)
                .put("hers", null)
                .put("nihao", null)
                .put("hao", null)
                .put("hs", null)
                .put("hsr", null)
                .create(NodeFactories.<Integer>defaultAcTrie(NodeFactories.RootType.ASCII));
    }

    @AfterClass
    public static void clear() {
        acStrBinaryTrie.clear();
        acStrBinaryTrie = null;
    }

    @Test
    public void acBinaryTrieTest() {
        Set<Hit> answerSet = new HashSet<>();
        answerSet.add(new Hit<>(1, "she", null));
        answerSet.add(new Hit<>(2, "he", null));
        answerSet.add(new Hit<>(2, "hers", null));

        String text = "ushers";
        Set<Hit> runRet = new HashSet<Hit>(acStrBinaryTrie.match(text.toCharArray()));
        System.out.println(text + ": " + runRet);
        Assert.assertEquals(answerSet, runRet);

        answerSet.clear();
        answerSet.add(new Hit<>(4, "hs", null));
        answerSet.add(new Hit<>(8, "she", null));
        answerSet.add(new Hit<>(9, "he", null));
        answerSet.add(new Hit<>(14, "nihao", null));
        answerSet.add(new Hit<>(16, "hao", null));
        answerSet.add(new Hit<>(20, "hs", null));
        answerSet.add(new Hit<>(20, "hsr", null));
        answerSet.add(new Hit<>(23, "nihao", null));
        answerSet.add(new Hit<>(25, "hao", null));
        text = "sdmfhsgnshejfgnihaofhsrnihao";
        runRet = new HashSet<Hit>(acStrBinaryTrie.match(text.toCharArray()));
        System.out.printf(text + ": " + runRet);
        Assert.assertEquals(answerSet, runRet);
    }
}
