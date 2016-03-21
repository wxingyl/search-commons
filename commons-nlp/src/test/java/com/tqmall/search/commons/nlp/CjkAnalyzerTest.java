package com.tqmall.search.commons.nlp;

import com.tqmall.search.commons.analyzer.CjkLexicon;
import com.tqmall.search.commons.analyzer.TokenType;
import com.tqmall.search.commons.lang.Function;
import com.tqmall.search.commons.match.Hit;
import com.tqmall.search.commons.match.Hits;
import com.tqmall.search.commons.trie.RootNodeType;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.List;

/**
 * Created by xing on 16/2/11.
 * segment分词测试
 */
public class CjkAnalyzerTest {

    private static CjkLexicon cjkLexicon;

    @BeforeClass
    public static void init() {
        cjkLexicon = new CjkLexicon(RootNodeType.CJK, NlpUtils.getPathOfClass(CjkAnalyzerTest.class, "/segment.txt"));
    }

    @AfterClass
    public static void destroy() {
        cjkLexicon = null;
    }

    @Test
    public void segmentTest() {
        System.out.println("fullSegment");
        runSegment(new Function<String, List<Hit<TokenType>>>() {
            @Override
            public List<Hit<TokenType>> apply(String text) {
                return cjkLexicon.fullMatch(text.toCharArray(), 0, text.length());
            }
        });
        System.out.println();
        System.out.println("minSegment");
        runSegment(new Function<String, List<Hit<TokenType>>>() {
            @Override
            public List<Hit<TokenType>> apply(String text) {
                return cjkLexicon.minMatch(text.toCharArray(), 0, text.length());
            }
        });
        System.out.println();
        System.out.println("maxSegment");
        runSegment(new Function<String, List<Hit<TokenType>>>() {
            @Override
            public List<Hit<TokenType>> apply(String text) {
                return cjkLexicon.maxMatch(text.toCharArray(), 0, text.length());
            }
        });
        System.out.println();
    }

    public void runSegment(Function<String, List<Hit<TokenType>>> function) {
        String text = "北京大学";
        List<Hit<TokenType>> list;
        list = function.apply(text);
        printHits(text, list);
        text = "北京的大学";
        list = function.apply(text);
        printHits(text, list);
        text = "商品服务";
        list = function.apply(text);
        printHits(text, list);
        text = "商品和服务";
        list = function.apply(text);
        printHits(text, list);
        text = "商品和氏璧";
        list = function.apply(text);
        printHits(text, list);
        text = "B-tree中的每个结点根据实际情况可以包含大量的关键字信息";
        list = function.apply(text);
        printHits(text, list);
        text = "东方不败笑傲江湖都是好看的电视剧";
        list = function.apply(text);
        printHits(text, list);
        text = "商品共和服";
        list = function.apply(text);
        printHits(text, list);
    }

    private void printHits(String text, List<Hit<TokenType>> list) {
        System.out.println(text + ": " + Hits.valueOf(text, list));
    }

}
