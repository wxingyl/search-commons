package com.tqmall.search.commons.algorithm;

import com.tqmall.search.commons.ac.AcBinaryTrie;
import com.tqmall.search.commons.match.Hit;
import com.tqmall.search.commons.nlp.Utils;
import com.tqmall.search.commons.trie.RootNodeType;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created by xing on 16/2/1.
 * AcTrie test
 */
public class AcTrieTest {

    private static AcBinaryTrie<Void> acStrBinaryTrie;

    @BeforeClass
    public static void init() {
        acStrBinaryTrie = AcBinaryTrie.<Void>build()
                .put("he", null)
                .put("she", null)
                .put("his", null)
                .put("hers", null)
                .put("nihao", null)
                .put("hao", null)
                .put("hs", null)
                .put("hsr", null)
                .create(RootNodeType.ASCII.<Void>defaultAcTrie());
    }

    @AfterClass
    public static void clear() {
        acStrBinaryTrie.clear();
        acStrBinaryTrie = null;
    }

    @Test
    public void acBinaryTrieTest() {
        List<Hit> expectList = new ArrayList<>();
        expectList.add(Utils.hitValueOf(1, "she", null));
        expectList.add(Utils.hitValueOf(2, "he", null));
        expectList.add(Utils.hitValueOf(2, "hers", null));

        String text = "ushers";
        List<Hit<Void>> retList = acStrBinaryTrie.match(text);
        Assert.assertNotNull(retList);
        Collections.sort(retList);
        System.out.println(text + ": " + retList);
        Assert.assertEquals(expectList, retList);

        expectList = new ArrayList<>();
        expectList.add(Utils.hitValueOf(4, "hs", null));
        expectList.add(Utils.hitValueOf(8, "she", null));
        expectList.add(Utils.hitValueOf(9, "he", null));
        expectList.add(Utils.hitValueOf(14, "nihao", null));
        expectList.add(Utils.hitValueOf(16, "hao", null));
        expectList.add(Utils.hitValueOf(20, "hs", null));
        expectList.add(Utils.hitValueOf(20, "hsr", null));
        expectList.add(Utils.hitValueOf(23, "nihao", null));
        expectList.add(Utils.hitValueOf(25, "hao", null));
        text = "sdmfhsgnshejfgnihaofhsrnihao";
        retList = acStrBinaryTrie.match(text);
        Assert.assertNotNull(retList);
        Collections.sort(retList);
        System.out.println(text + ": " + retList);
        Assert.assertEquals(expectList, retList);
    }
}
