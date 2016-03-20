package com.tqmall.search.commons.nlp;

import com.tqmall.search.commons.lang.Function;
import com.tqmall.search.commons.lang.LazyInit;
import com.tqmall.search.commons.lang.Supplier;
import com.tqmall.search.commons.match.Hit;
import com.tqmall.search.commons.match.MatchBinaryReverseTrie;
import com.tqmall.search.commons.trie.RootNodeType;
import com.tqmall.search.commons.utils.CommonsUtils;
import com.tqmall.search.commons.utils.SearchStringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.AbstractMap;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Created by xing on 16/1/24.
 * 汉字转拼音
 */
public final class PinyinConvert {

    private static final Logger log = LoggerFactory.getLogger(PinyinConvert.class);

    private static final LazyInit<PinyinConvert> INSTANCE = new LazyInit<>(new Supplier<PinyinConvert>() {
        @Override
        public PinyinConvert get() {
            return new PinyinConvert();
        }
    });

    /**
     * 单例, 通过该接口获取实例对象
     */
    public static PinyinConvert instance() {
        return INSTANCE.getInstance();
    }

    private final MatchBinaryReverseTrie<String[]> matchBinaryReverseTrie;

    PinyinConvert() {
        matchBinaryReverseTrie = new MatchBinaryReverseTrie<>(RootNodeType.CJK.<String[]>defaultTrie());
        log.info("start loading pinyin lexicon file: " + NlpConst.PINYIN_FILE_NAME);
        NlpUtils.loadLexicon(NlpConst.PINYIN_FILE_NAME, new Function<String, Boolean>() {
            @Override
            public Boolean apply(String line) {
                String[] array = SearchStringUtils.split(line, '=');
                String[] value;
                //多个汉字的词语拼音, 通过' '分隔
                if (array[0].length() > 1) {
                    value = SearchStringUtils.split(array[1], ' ');
                } else {
                    value = new String[]{array[1]};
                }
                matchBinaryReverseTrie.put(array[0], value);
                return true;
            }
        });
        log.info("load pinyin lexicon file: " + NlpConst.PINYIN_FILE_NAME + " finish");
    }

    /**
     * 是否添加字符
     */
    private boolean appendChar(char ch, final int appendFlag) {
        if (ch >= '0' && ch <= '9') {
            return (appendFlag & NlpConst.APPEND_CHAR_DIGIT) != 0;
        } else if ((ch >= 'a' && ch <= 'z') || (ch >= 'A' && ch <= 'Z')) {
            return (appendFlag & NlpConst.APPEND_CHAR_LETTER) != 0;
        } else if (Character.isWhitespace(ch)) {
            return (appendFlag & NlpConst.APPEND_CHAR_WHITESPACE) != 0;
        } else {
            return (appendFlag & NlpConst.APPEND_CHAR_OTHER) != 0;
        }
    }

    private String convert(final String word, final int appendFlag, final StringBuilder firstLetter) {
        List<Hit<String[]>> hits = matchBinaryReverseTrie.maxMatch(word);
        if (CommonsUtils.isEmpty(hits)) return null;
        //后面的算法要求hits有序
        Collections.sort(hits);
        StringBuilder py = new StringBuilder();
        int lastEndIndex = 0;
        for (Hit<String[]> h : hits) {
            int curStartPos = h.getStart();
            if (appendFlag != 0) {
                while (lastEndIndex < curStartPos) {
                    if (appendChar(word.charAt(lastEndIndex), appendFlag)) {
                        py.append(word.charAt(lastEndIndex));
                    }
                    lastEndIndex++;
                }
            }
            for (String s : h.getValue()) {
                py.append(s);
                if (firstLetter != null) firstLetter.append(s.charAt(0));
            }
            lastEndIndex = h.getEnd();
        }
        if (appendFlag != 0) {
            while (lastEndIndex < word.length()) {
                if (appendChar(word.charAt(lastEndIndex), appendFlag)) {
                    py.append(word.charAt(lastEndIndex));
                }
                lastEndIndex++;
            }
        }
        return py.toString();
    }

    /**
     * 单个cjk字符转化, 对于多音字, 只返回词库中的第一个
     */
    public String convert(char cjkChar) {
        List<Hit<String[]>> hits = matchBinaryReverseTrie.maxMatch(new char[]{cjkChar}, 0, 1);
        if (CommonsUtils.isEmpty(hits)) return null;
        return hits.get(0).getValue()[0];
    }

    /**
     * 字符串拼音转换
     *
     * @param word       需要转换的汉字
     * @param appendFlag 需要包含的字符: 数字,空格等字符标记位
     * @return 转换结果
     * @see NlpConst#APPEND_CHAR_WHITESPACE
     * @see NlpConst#APPEND_CHAR_LETTER
     * @see NlpConst#APPEND_CHAR_DIGIT
     * @see NlpConst#APPEND_CHAR_OTHER
     */
    public String convert(String word, final int appendFlag) {
        return convert(word, appendFlag, null);
    }

    /**
     * 字符串拼音转换, 并且返回汉字拼音首字母
     *
     * @param word       需要转换的汉字
     * @param appendFlag 需要包含的字符: 数字,空格等字符标记位
     * @return 转换结果 {@link Map.Entry#getKey()} 拼音转换结果, {@link Map.Entry#getValue()} 拼音首字母
     * @see NlpConst#APPEND_CHAR_WHITESPACE
     * @see NlpConst#APPEND_CHAR_LETTER
     * @see NlpConst#APPEND_CHAR_DIGIT
     * @see NlpConst#APPEND_CHAR_OTHER
     */
    public Map.Entry<String, String> firstLetterConvert(String word, final int appendFlag) {
        StringBuilder sb = new StringBuilder();
        String py = convert(word, appendFlag, sb);
        if (py == null) return null;
        else return new AbstractMap.SimpleImmutableEntry<>(py, sb.toString());
    }

}
