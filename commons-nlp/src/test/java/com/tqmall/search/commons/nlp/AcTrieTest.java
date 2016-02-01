package com.tqmall.search.commons.nlp;

import com.tqmall.search.commons.nlp.trie.AcBinaryTrie;
import com.tqmall.search.commons.nlp.trie.AcTrie;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * Created by xing on 16/2/1.
 * AcTrie test
 */
public class AcTrieTest {

    private static AcTrie<Void> acBinaryTrie;

    @BeforeClass
    public static void init() {
        acBinaryTrie = AcBinaryTrie.<Void>build()
                .put("he", null)
                .put("she", null)
                .put("his", null)
                .put("hers", null)
                .put("nihao", null)
                .put("hao", null)
                .put("hs", null)
                .put("hsr", null)
                .create();
    }

    @Test
    public void acBinaryTrieTest() {
        System.out.println(acBinaryTrie.parseText("ushers"));
        System.out.println(acBinaryTrie.parseText("sdmfhsgnshejfgnihaofhsrnihao"));
    }
}
