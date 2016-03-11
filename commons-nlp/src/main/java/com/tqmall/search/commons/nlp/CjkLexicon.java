package com.tqmall.search.commons.nlp;

import com.tqmall.search.commons.exception.LoadLexiconException;
import com.tqmall.search.commons.lang.Function;
import com.tqmall.search.commons.ac.AcBinaryTrie;
import com.tqmall.search.commons.ac.AcTrieNodeFactory;
import com.tqmall.search.commons.match.Hit;
import com.tqmall.search.commons.match.MatchBinaryTrie;
import com.tqmall.search.commons.trie.*;
import com.tqmall.search.commons.utils.SearchStringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

/**
 * Created by xing on 16/2/8.
 * 中文分词词库, 包括汉语词库以及停止词, 提供最大, 最小, 全匹配, 通过{@link AcBinaryTrie}实现
 * 词库文件中, 每个词可以指定词的{@link TokenType}, 通过{@link TokenType#fromString(String)}解析对应类型, 默认{@link TokenType#CN}
 *
 * @see TokenType
 * @see TokenType#fromString(String)
 */
public class CjkLexicon {

    private static final Logger log = LoggerFactory.getLogger(CjkLexicon.class);

    private final AcBinaryTrie<TokenType> acBinaryTrie;

    private final MatchBinaryTrie<TokenType> matchBinaryTrie;

    private final MatchBinaryTrie<TokenType> quantifierTrie;

    /**
     * {@link AcTrieNodeFactory}默认使用{@link NodeFactories.RootType#CJK}
     */
    public CjkLexicon(InputStream lexicon) {
        this(NodeFactories.<TokenType>defaultAcTrie(NodeFactories.RootType.CJK), lexicon);
    }

    /**
     * 读取词库文件, 如果存在异常则抛出{@link LoadLexiconException}
     *
     * @param nodeFactory 具体初始化节点的Factory
     * @param lexicon     词库输入流
     * @see LoadLexiconException
     */
    public CjkLexicon(AcTrieNodeFactory<TokenType> nodeFactory, final InputStream lexicon) {
        matchBinaryTrie = new MatchBinaryTrie<>(nodeFactory);
        long startTime = System.currentTimeMillis();
        log.info("start loading cjk lexicon: " + lexicon);
        quantifierTrie = new MatchBinaryTrie<>(NodeFactories.<TokenType>defaultTrie(NodeFactories.RootType.NORMAL));
        final AcBinaryTrie.Builder<TokenType> builder = AcBinaryTrie.build();
        try {
            NlpUtils.loadLexicon(lexicon, new Function<String, Boolean>() {
                @Override
                public Boolean apply(String s) {
                    int index = s.indexOf(' ');
                    TokenType tokenType;
                    if (index < 0) {
                        tokenType = TokenType.CN;
                    } else {
                        String str = s.substring(index + 1).trim();
                        tokenType = TokenType.fromString(str);
                        s = s.substring(0, index);
                        if (tokenType == null) {
                            log.warn("load cjk lexicon: " + lexicon + ", word: " + s + " tokenType: " + str + " is invalid, instead of " + TokenType.CN);
                        } else if (tokenType == TokenType.QUANTIFIER) {
                            quantifierTrie.put(s, TokenType.QUANTIFIER);
                        }
                    }
                    builder.put(s, tokenType);
                    return true;
                }
            }, true);
        } catch (IOException e) {
            log.error("read cjk lexicon: " + lexicon + " break out IOException", e);
            throw new LoadLexiconException("init cjk lexicon: " + lexicon + " break out exception", e);
        }
        acBinaryTrie = builder.create(matchBinaryTrie);
        log.info("load cjk lexicon: " + lexicon + " finish, total cost: " + (System.currentTimeMillis() - startTime) + "ms");
        NlpUtils.loadLexicon(NlpConst.QUANTIFIER_FILE_NAME, new Function<String, Boolean>() {
            @Override
            public Boolean apply(String s) {
                quantifierTrie.put(s, TokenType.QUANTIFIER);
                return true;
            }
        });
    }

    /**
     * full匹配, 尽可能的返回所有能够匹配到的结果
     *
     * @param text     待分词文本
     * @param startPos 待处理文本的起始位置
     * @param length   待处理文本的长度
     * @return 匹配结果
     */
    public List<Hit<TokenType>> fullMatch(char[] text, int startPos, int length) {
        return acBinaryTrie.match(text, startPos, length);
    }

    /**
     * 最大匹配
     *
     * @param text     待分词文本
     * @param startPos 待处理文本的起始位置
     * @param length   待处理文本的长度
     * @return 匹配结果
     */
    public List<Hit<TokenType>> maxMatch(char[] text, int startPos, int length) {
        return matchBinaryTrie.maxMatch(text, startPos, length);
    }

    /**
     * 最小匹配
     *
     * @param text     待分词文本
     * @param startPos 待处理文本的起始位置
     * @param length   待处理文本的长度
     * @return 匹配结果
     */
    public List<Hit<TokenType>> minMatch(char[] text, int startPos, int length) {
        return matchBinaryTrie.minMatch(text, startPos, length);
    }

    /**
     * 添加量词
     *
     * @return 添加是否成功
     */
    public boolean addQuantifier(String quantifier) {
        quantifier = SearchStringUtils.filterString(quantifier);
        return quantifier != null && quantifierTrie.put(quantifier.toLowerCase(), TokenType.QUANTIFIER);
    }

    /**
     * 删除量词
     *
     * @return 添加是否成功
     */
    public boolean removeQuantifier(String quantifier) {
        quantifier = SearchStringUtils.filterString(quantifier);
        return quantifier != null && quantifierTrie.remove(quantifier.toLowerCase());
    }

    public List<Hit<TokenType>> quantifierMatch(char[] text, int startPos, int length) {
        return quantifierTrie.maxMatch(text, startPos, length);
    }

}
