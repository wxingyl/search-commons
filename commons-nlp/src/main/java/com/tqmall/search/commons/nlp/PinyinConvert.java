package com.tqmall.search.commons.nlp;

import com.tqmall.search.commons.lang.Function;
import com.tqmall.search.commons.nlp.trie.BinaryMatchTrie;
import com.tqmall.search.commons.nlp.trie.Node;
import com.tqmall.search.commons.utils.CommonsUtils;
import com.tqmall.search.commons.utils.SearchStringUtils;

import java.util.*;

/**
 * Created by xing on 16/1/24.
 * 汉字转拼音
 */
public final class PinyinConvert {

    private final BinaryMatchTrie<String[]> binaryMatchTrie;

    public PinyinConvert() {
        binaryMatchTrie = new BinaryMatchTrie<>(Node.<String[]>defaultCjkTrieNodeFactory());
        NlpUtils.loadLexicon(NlpConst.PINYIN_FILE_NAME, new Function<String, Boolean>() {
            @Override
            public Boolean apply(String line) {
                String[] array = SearchStringUtils.split(line, '=');
                String[] value;
                if (array[0].length() > 1) {
                    value = SearchStringUtils.split(array[1], ' ');
                } else {
                    value = new String[]{array[1]};
                }
                binaryMatchTrie.put(array[0], value);
                return true;
            }
        });
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

    private String convert(char[] word, final int appendFlag, StringBuilder firstLetter) {
        List<Hit<String[]>> hits = binaryMatchTrie.textMaxMatch(word);
        if (CommonsUtils.isEmpty(hits)) return null;
        StringBuilder py = new StringBuilder();
        int lastEndIndex = 0;
        for (Hit<String[]> h : hits) {
            int curStartPos = h.getStartPos();
            if (appendFlag != 0) {
                while (lastEndIndex < curStartPos) {
                    if (appendChar(word[lastEndIndex], appendFlag)) {
                        py.append(word[lastEndIndex]);
                    }
                    lastEndIndex++;
                }
            }
            for (String s : h.getValue()) {
                py.append(s);
                if (firstLetter != null) firstLetter.append(s.charAt(0));
            }
            lastEndIndex = h.getEndPos();
        }
        if (appendFlag != 0) {
            while (lastEndIndex < word.length) {
                if (appendChar(word[lastEndIndex], appendFlag)) {
                    py.append(word[lastEndIndex]);
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
        List<Hit<String[]>> hits = binaryMatchTrie.textMaxMatch(new char[]{cjkChar});
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
    public String convert(char[] word, final int appendFlag) {
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
    public Map.Entry<String, String> firstLetterConvert(char[] word, final int appendFlag) {
        StringBuilder sb = new StringBuilder();
        String py = convert(word, appendFlag, sb);
        if (py == null) return null;
        else return new AbstractMap.SimpleImmutableEntry<>(py, sb.toString());
    }

}
