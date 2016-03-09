package com.tqmall.search.commons.nlp;

import com.tqmall.search.commons.exception.LoadLexiconException;
import com.tqmall.search.commons.lang.Function;
import com.tqmall.search.commons.lang.StrValueConvert;
import com.tqmall.search.commons.nlp.trie.*;
import com.tqmall.search.commons.utils.StrValueConverts;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

/**
 * Created by xing on 16/2/8.
 * 中文分词词库, 包括汉语词库以及停止词, 提供最大, 最小, 全匹配, 通过{@link AcBinaryTrie}实现
 * 词库文件中, 每个词可以指定词的type, 在这儿默认都是{@link NlpConst#TOKEN_TYPE_CN}, 这部分以后可以扩展, 做什么词性标注什么的,
 * 砸门现在的实现要求比较简单, 这些东东都不需要的
 */
public class CjkLexicon {

    private static final Logger log = LoggerFactory.getLogger(CjkLexicon.class);

    private final AcBinaryTrie<Integer> acBinaryTrie;

    private BinaryMatchTrie<Integer> binaryMatchTrie;

    /**
     * 使用默认的{@link AcNormalNode#defaultCjkAcTrieNodeFactory()} 也就是词的开通只支持汉字
     */
    public CjkLexicon(InputStream lexicon) {
        this(AcNormalNode.<Integer>defaultCjkAcTrieNodeFactory(), lexicon);
    }

    /**
     * 读取词库文件, 如果存在异常则抛出{@link LoadLexiconException}
     *
     * @param nodeFactory 具体初始化节点的Factory
     * @param lexicon     词库输入流
     * @see LoadLexiconException
     */
    public CjkLexicon(AcTrieNodeFactory<Integer> nodeFactory, InputStream lexicon) {
        final AcBinaryTrie.Builder<Integer> builder = AcBinaryTrie.build();
        builder.nodeFactory(nodeFactory);
        long startTime = System.currentTimeMillis();
        log.info("start loading cjk lexicon: " + lexicon);
        try {
            final StrValueConvert<Integer> tokenTypeConvert = StrValueConverts.getConvert(Integer.TYPE, NlpConst.TOKEN_TYPE_CN);
            NlpUtils.loadLexicon(lexicon, new Function<String, Boolean>() {
                @Override
                public Boolean apply(String s) {
                    int index = s.indexOf(' ');
                    if (index < 0) {
                        builder.put(s, NlpConst.TOKEN_TYPE_CN);
                    } else {
                        Integer type = tokenTypeConvert.convert(s.substring(index + 1).trim());
                        if (type < 0 || type >= NlpConst.TOKEN_TYPES.length) {
                            type = NlpConst.TOKEN_TYPE_CN;
                        }
                        builder.put(s, type);
                    }
                    return true;
                }
            }, true);
        } catch (IOException e) {
            log.error("read cjk lexicon: " + lexicon + " break out IOException", e);
            throw new LoadLexiconException("init cjk lexicon: " + lexicon + " break out exception", e);
        }
        acBinaryTrie = builder.create(new Function<AcTrieNodeFactory<Integer>, AbstractTrie<Integer>>() {

            @Override
            public AbstractTrie<Integer> apply(AcTrieNodeFactory<Integer> acTrieNodeFactory) {
                binaryMatchTrie = new BinaryMatchTrie<>(acTrieNodeFactory);
                return binaryMatchTrie;
            }

        });
        log.info("load cjk lexicon: " + lexicon + " finish, total cost: " + (System.currentTimeMillis() - startTime) + "ms");
    }

    /**
     * full匹配, 尽可能的返回所有能够匹配到的结果
     *
     * @param text     待分词文本
     * @param startPos 待处理文本的起始位置
     * @param length   待处理文本的长度
     * @return 匹配结果
     */
    public List<Hit<Integer>> fullMatch(char[] text, int startPos, int length) {
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
    public List<Hit<Integer>> maxMatch(char[] text, int startPos, int length) {
        return binaryMatchTrie.maxMatch(text, startPos, length);
    }

    /**
     * 最小匹配
     *
     * @param text     待分词文本
     * @param startPos 待处理文本的起始位置
     * @param length   待处理文本的长度
     * @return 匹配结果
     */
    public List<Hit<Integer>> minMatch(char[] text, int startPos, int length) {
        return binaryMatchTrie.minMatch(text, startPos, length);
    }
}
