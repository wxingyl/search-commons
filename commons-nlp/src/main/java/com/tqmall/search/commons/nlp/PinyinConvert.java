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

import java.io.Serializable;
import java.util.ArrayList;
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
        NlpUtils.loadClassPathLexicon(PinyinConvert.class, NlpConst.PINYIN_FILE_NAME, new Function<String, Boolean>() {
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
    private boolean appendChar(char c, final int appendFlag) {
        if (c >= '0' && c <= '9') {
            return (appendFlag & NlpConst.APPEND_CHAR_DIGIT) != 0;
        } else if ((c >= 'a' && c <= 'z') || (c >= 'A' && c <= 'Z')) {
            return (appendFlag & NlpConst.APPEND_CHAR_LETTER) != 0;
        } else if (Character.isWhitespace(c)) {
            return (appendFlag & NlpConst.APPEND_CHAR_WHITESPACE) != 0;
        } else {
            return (appendFlag & NlpConst.APPEND_CHAR_OTHER) != 0;
        }
    }

    private String convert(final String text, final int appendFlag, final StringBuilder firstLetter) {
        List<Hit<String[]>> hits = matchBinaryReverseTrie.maxMatch(text);
        if (CommonsUtils.isEmpty(hits)) return null;
        StringBuilder py = new StringBuilder();
        int lastEndIndex = 0;
        //为了考虑哪些非CJK字符, 只能顺序遍历了~~~
        for (Hit<String[]> h : hits) {
            int curStartPos = h.getStart();
            if (appendFlag != 0) {
                while (lastEndIndex < curStartPos) {
                    if (appendChar(text.charAt(lastEndIndex), appendFlag)) {
                        py.append(text.charAt(lastEndIndex));
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
            while (lastEndIndex < text.length()) {
                if (appendChar(text.charAt(lastEndIndex), appendFlag)) {
                    py.append(text.charAt(lastEndIndex));
                }
                lastEndIndex++;
            }
        }
        return py.toString();
    }

    /**
     * 单个cjk字符转化, 对于多音字, 只返回词库中的第一个
     * 由于多音字的原因, 该方法不是太建议使用
     */
    public String convert(char cjkChar) {
        List<Hit<String[]>> hits = matchBinaryReverseTrie.maxMatch(new char[]{cjkChar}, 0, 1);
        if (CommonsUtils.isEmpty(hits)) return null;
        return hits.get(0).getValue()[0];
    }

    /**
     * 字符串拼音转换
     *
     * @param text       需要转换的汉字
     * @param appendFlag 需要包含的字符: 数字,空格等字符标记位
     * @return 转换结果
     * @see NlpConst#APPEND_CHAR_WHITESPACE
     * @see NlpConst#APPEND_CHAR_LETTER
     * @see NlpConst#APPEND_CHAR_DIGIT
     * @see NlpConst#APPEND_CHAR_OTHER
     */
    public String convert(String text, final int appendFlag) {
        return convert(text, appendFlag, null);
    }

    /**
     * 字符串拼音转换, 并且返回汉字拼音首字母
     *
     * @param text       需要转换的汉字
     * @param appendFlag 需要包含的字符: 数字,空格等字符标记位
     * @return 转换结果 {@link Map.Entry#getKey()} 拼音转换结果, {@link Map.Entry#getValue()} 拼音首字母
     * @see NlpConst#APPEND_CHAR_WHITESPACE
     * @see NlpConst#APPEND_CHAR_LETTER
     * @see NlpConst#APPEND_CHAR_DIGIT
     * @see NlpConst#APPEND_CHAR_OTHER
     */
    public Map.Entry<String, String> firstLetterConvert(String text, final int appendFlag) {
        StringBuilder sb = new StringBuilder();
        String py = convert(text, appendFlag, sb);
        if (py == null) return null;
        else return CommonsUtils.newImmutableMapEntry(py, sb.toString());
    }

    /**
     * 中文文本拼音转换, 返回具体每个cjk字符对应的拼音
     * @param text cjk文本
     * @return text中每个字符对应的拼音
     */
    public List<CjkChar> convert(String text) {
        List<Hit<String[]>> hits = matchBinaryReverseTrie.maxMatch(text);
        if (CommonsUtils.isEmpty(hits)) return null;
        List<CjkChar> retList = new ArrayList<>();
        for (Hit<String[]> h : hits) {
            int startPos = h.getStart();
            for (String py : h.getValue()) {
                retList.add(new CjkChar(text.charAt(startPos), startPos, py));
                startPos++;
            }
        }
        return retList;
    }

    /**
     * 添加对应汉字的拼音, 多个汉字通过空格分离
     *
     * @param word 汉语词组
     * @param py   对应拼音
     * @return 是否添加成功
     */
    public boolean addPinyinLexicon(String word, String py) {
        word = SearchStringUtils.filterString(word);
        String[] pyList = SearchStringUtils.split(py, ' ');
        if (word == null || word.length() != pyList.length) {
            throw new IllegalArgumentException("cjk word: " + word + ", py: " + py + " can not match");
        }
        return matchBinaryReverseTrie.put(word, pyList);
    }

    /**
     * 删除指定词的分词
     *
     * @return 删除是否成功
     */
    public boolean removePinyinLexicon(String word) {
        word = SearchStringUtils.filterString(word);
        if (word == null) {
            throw new IllegalArgumentException("word is empty");
        }
        return matchBinaryReverseTrie.remove(word);
    }

    public static class CjkChar implements Serializable {

        private static final long serialVersionUID = 1L;

        private final char character;

        private final int position;

        private final String pinyin;

        public CjkChar(char character, int position, String pinyin) {
            this.character = character;
            this.position = position;
            this.pinyin = pinyin;
        }

        public char getCharacter() {
            return character;
        }

        public String getPinyin() {
            return pinyin;
        }

        public int getPosition() {
            return position;
        }

        public char getFirstLetter() {
            return pinyin.charAt(0);
        }
    }

}
