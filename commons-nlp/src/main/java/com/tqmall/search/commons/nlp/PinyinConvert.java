package com.tqmall.search.commons.nlp;

import com.tqmall.search.commons.nlp.trie.BinaryMatchTrie;
import com.tqmall.search.commons.nlp.trie.Node;
import com.tqmall.search.commons.utils.CommonsUtils;
import com.tqmall.search.commons.utils.SearchStringUtils;

import java.util.*;

/**
 * Created by xing on 16/1/24.
 * 汉字转拼音
 */
final class PinyinConvert {

    private final BinaryMatchTrie<String[]> binaryMatchTrie;

    PinyinConvert() {
        binaryMatchTrie = new BinaryMatchTrie<>(Node.<String[]>defaultCjkTrieNodeFactory());
        NlpUtils.loadLexicon(NlpConst.PINYIN_FILE_NAME, new NlpUtils.LineHandle() {
            @Override
            public boolean onHandle(String line) {
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
     * 单个cjk字符转化, 对于多音字, 只返回词库中的第一个
     */
    public String cjkConvert(char cjkChar) {
        List<Hit<String[]>> hits = binaryMatchTrie.textMaxMatch(cjkChar + "");
        if (CommonsUtils.isEmpty(hits)) return null;
        return hits.get(0).getValue()[0];
    }

    /**
     * 将汉字转换为对应拼音以及首字母字符串, 其他未识别字符都忽略
     * 如果忽略拼音首字母, 则返回结果中的{@link Map.Entry#getValue()}为null
     *
     * @param word             需要转换的汉字
     * @param ignoreWhitespace 是否忽略空白符, 如果不忽略则保留
     * @param needFirstLetter 是否需要拼音首字母
     * @return {@link Map.Entry#getKey()} 为转换的拼音text, {@link Map.Entry#getValue()} 为拼音首字母字符串
     */
    public Map.Entry<String, String> normalConvert(String word, boolean ignoreWhitespace, boolean needFirstLetter) {
        List<Hit<String[]>> hits = binaryMatchTrie.textMaxMatch(word);
        if (CommonsUtils.isEmpty(hits)) return null;
        StringBuilder pyStr = new StringBuilder();
        StringBuilder firstLetter = needFirstLetter ? new StringBuilder() : null;
        int lastEndPos = 0;
        for (Hit<String[]> h : hits) {
            int curStartPos = h.getStartPos();
            if (curStartPos != lastEndPos && !ignoreWhitespace) {
                while (lastEndPos < curStartPos) {
                    char ch = word.charAt(lastEndPos);
                    if (Character.isWhitespace(ch) || NlpUtils.isSpecialChar(ch)) {
                        pyStr.append(ch);
                    }
                    lastEndPos++;
                }
            }
            for (String s : h.getValue()) {
                pyStr.append(s);
                if (needFirstLetter) firstLetter.append(s.charAt(0));
            }
            lastEndPos = h.getEndPos();
        }
        return new AbstractMap.SimpleEntry<>(pyStr.toString(), firstLetter == null ? null : firstLetter.toString());
    }

    /**
     * 输入的汉字转换为拼音, 拼配结果中包括: 转换完的拼音字符串, 每个汉字的拼音首字母字符串以及未能识别的字符列表(分为cjk和非cjk)
     * 注意: 如果没有一个拼音匹配, 则返回null, 不再做任何处理
     *
     * @param word 需要转换的汉字
     * @return 转换结果, 如果没有一个拼音匹配, 则返回null, 不再做任何处理
     */
    public Result fullConvert(String word) {
        List<Hit<String[]>> hits = binaryMatchTrie.textMaxMatch(word);
        if (CommonsUtils.isEmpty(hits)) return null;
        int length = word.length();
        Map<Integer, Hit<String[]>> hitStartPosMap = new HashMap<>();
        for (Hit<String[]> h : hits) {
            hitStartPosMap.put(h.getStartPos(), h);
        }
        Result result = new Result(word);
        StringBuilder firstLetter = new StringBuilder();
        for (int i = 0; i < length; ) {
            Hit<String[]> hit = hitStartPosMap.get(i);
            if (hit != null) {
                for (String s : hit.getValue()) {
                    result.pinyinList.add(new PinyinCharacter(word.charAt(i), i, s));
                    firstLetter.append(s.charAt(0));
                    i++;
                }
            } else {
                result.addUnknown(new MatchCharacter(word.charAt(i), i));
                i++;
            }
        }
        result.pinyinFirstLetter = firstLetter.toString();
        return result;
    }

    /**
     * 拼音转化结果类封装
     */
    public static class Result {
        /**
         * 原始text
         */
        private final String srcText;

        /**
         * 拼音转换结果list
         */
        private final List<PinyinCharacter> pinyinList;
        /**
         * 拼音首字母列表
         */
        private String pinyinFirstLetter;
        /**
         * 未能正常转换的cjk字符
         */
        private List<MatchCharacter> unknownCjk;
        /**
         * 其他不能转换的字符, 即非cjk字符
         */
        private List<MatchCharacter> unknownOther;

        public Result(String srcText) {
            this.srcText = srcText;
            pinyinList = new ArrayList<>();
        }

        /**
         * 是否所有的{@link #srcText}都转换, 没有不能被识别的字符
         */
        public boolean isFullConvert() {
            return unknownCjk == null && unknownOther == null;
        }

        public List<PinyinCharacter> getPinyinList() {
            return pinyinList;
        }

        public String getSrcText() {
            return srcText;
        }

        void addUnknown(MatchCharacter ch) {
            if (NlpUtils.isCjkChar(ch.c)) {
                if (unknownCjk == null) unknownCjk = new LinkedList<>();
                unknownCjk.add(ch);
            } else {
                if (unknownOther == null) unknownOther = new LinkedList<>();
                unknownOther.add(ch);
            }
        }

        public List<MatchCharacter> getUnknownCjk() {
            return unknownCjk;
        }

        public List<MatchCharacter> getUnknownOther() {
            return unknownOther;
        }

        public String getPinyinFirstLetter() {
            return pinyinFirstLetter;
        }

        @Override
        public String toString() {
            StringBuilder sb = new StringBuilder(256);
            sb.append(srcText).append("->").append(pinyinList);
            if (unknownCjk != null) {
                sb.append(", cjkUnknown=").append(unknownCjk);
            }
            if (unknownOther != null) {
                sb.append(", otherUnknown=").append(unknownOther);
            }
            return sb.toString();
        }
    }

    public static class PinyinCharacter extends MatchCharacter {

        private String pinyin;

        public PinyinCharacter(char c, int srcTextPos, String pinyin) {
            super(c, srcTextPos);
            this.pinyin = pinyin;
        }

        public String getPinyin() {
            return pinyin;
        }

        @Override
        public String toString() {
            return super.toString() + ':' + pinyin;
        }
    }

    /**
     * 不能转化的字符
     */
    public static class MatchCharacter {

        private char c;

        /**
         * 在原始text{@link Result#srcText}中的位置
         */
        private int srcTextPos;

        public MatchCharacter(char c, int srcTextPos) {
            this.c = c;
            this.srcTextPos = srcTextPos;
        }

        public char getC() {
            return c;
        }

        public int getSrcTextPos() {
            return srcTextPos;
        }

        @Override
        public String toString() {
            return c + ":" + srcTextPos;
        }
    }
}
