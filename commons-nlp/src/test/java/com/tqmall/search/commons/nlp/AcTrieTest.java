package com.tqmall.search.commons.nlp;

import com.tqmall.search.commons.nlp.trie.AcStrBinaryTrie;
import com.tqmall.search.commons.nlp.trie.Hit;
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

    private static AcStrBinaryTrie acStrBinaryTrie;

    @BeforeClass
    public static void init() {
//        [endPos=4:she, endPos=4:he, endPos=6:hers]
//        [endPos=6:hs, endPos=11:she, endPos=11:he, endPos=19:nihao, endPos=19:hao, endPos=22:hs, endPos=23:hsr, endPos=28:nihao, endPos=28:hao]
        acStrBinaryTrie = AcStrBinaryTrie.build()
                .add("he", "she", "his", "hers")
                .add("nihao")
                .add("hao")
                .add("hs")
                .add("hsr")
                .create();
    }

    @Test
    public void acBinaryTrieTest() {
        Set<Hit>  answerSet = new HashSet<>();
        answerSet.add(new Hit<>(4, "she", null));
        answerSet.add(new Hit<>(4, "he", null));
        answerSet.add(new Hit<>(6, "hers", null));
        Set<Hit> runRet = new HashSet<Hit>(acStrBinaryTrie.parseText("ushers"));
        System.out.printf("ushers: " + runRet);
        Assert.assertEquals(answerSet, runRet);

        answerSet.clear();
        answerSet.add(new Hit<>(6, "hs", null));
        answerSet.add(new Hit<>(11, "she", null));
        answerSet.add(new Hit<>(11, "he", null));
        answerSet.add(new Hit<>(19, "nihao", null));
        answerSet.add(new Hit<>(19, "hao", null));
        answerSet.add(new Hit<>(22, "hs", null));
        answerSet.add(new Hit<>(23, "hsr", null));
        answerSet.add(new Hit<>(28, "nihao", null));
        answerSet.add(new Hit<>(28, "hao", null));
        runRet = new HashSet<Hit>(acStrBinaryTrie.parseText("sdmfhsgnshejfgnihaofhsrnihao"));
        System.out.printf("sdmfhsgnshejfgnihaofhsrnihao: " + runRet);
        Assert.assertEquals(answerSet, runRet);
    }
}
