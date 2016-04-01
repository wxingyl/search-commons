package com.tqmall.search.util;

import com.google.common.collect.Lists;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.lionsoul.jcseg.ASegment;
import org.lionsoul.jcseg.Dictionary;
import org.lionsoul.jcseg.SimpleShortSeg;
import org.lionsoul.jcseg.core.*;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.util.*;

/**
 * 分词对象获取
 * Created by wcong on 14-9-9.
 */
@Slf4j
public class JcsegFactory {

    private static JcsegTaskConfig pureConfig = new JcsegTaskConfig();

    static {
        pureConfig.LOAD_CJK_PINYIN = false; // 过滤词的拼音
        pureConfig.LOAD_CJK_SYN = false; // 过滤近义词
        pureConfig.EN_SECOND_SEG = false; // 关闭二次分词
    }

    private static JcsegTaskConfig fullConfig = new JcsegTaskConfig();

    private static ADictionary dic = DictionaryFactory.createDefaultDictionary(fullConfig);

    public final static Set<String> escapeSet = new HashSet<>();

    static {
        Collections.addAll(escapeSet, "+", "-", "&", "||", "!", "(", ")", "{", "}", "[", "]", "^", "\"", "~", "?", "/");
    }

    /**
     * 不返回 近义词 和拼音的分词对象
     *
     * @return pureSeg
     */
    public static ASegment getPureSeg() {
        ASegment pureSeg = null;
        try {
            pureSeg = (ASegment) SegmentFactory.createJcseg(JcsegTaskConfig.SIMPLE_MODE,
                    pureConfig, dic);
        } catch (JcsegException e) {
            e.printStackTrace();
        }
        return pureSeg;
    }

    /**
     * 分词为短词，且不返回 近义词 和拼音的分词对象
     *
     * @return pureSeg
     */
    private static ASegment getPureSegShort() {
        ASegment pureSeg = null;
        try {
            pureSeg = (ASegment) SegmentFactory.createJcseg(JcsegTaskConfig.SIMPLE_SHORT_MODE,
                    pureConfig, dic);
        } catch (JcsegException e) {
            e.printStackTrace();
        }
        return pureSeg;
    }

    /**
     * 返回全部的分词对象
     *
     * @return fullSeg
     */
    private static ASegment getFullSeg() {
        ASegment fullSeg = null;
        try {
            fullSeg = (ASegment) SegmentFactory.createJcseg(JcsegTaskConfig.COMPLEX_MODE,
                    fullConfig, dic);
        } catch (JcsegException e) {
            e.printStackTrace();
        }
        return fullSeg;
    }

    /**
     * 返回单字分词的分词对象
     * 该分词器没有词库
     *
     * @return singleSeg
     */
    private static ASegment getSingleSeg() {
        ASegment singleSeg = null;
        try {
            singleSeg = (ASegment) SegmentFactory.createJcseg(JcsegTaskConfig.SIMPLE_MODE,
                    new Object[]{pureConfig, new Dictionary(pureConfig, false)});
        } catch (JcsegException e) {
            e.printStackTrace();
        }
        return singleSeg;
    }

    public static List<String> CNSplit(String str, boolean synonyms) {
        List<String> retList = new LinkedList<String>();
        if (str == null) {
            return retList;
        }
        try {
            ASegment seg = synonyms ? getFullSeg() : getPureSeg();
            seg.reset(new StringReader(str));
            IWord word = null;
            while ((word = seg.next()) != null) {
                if (escapeSet.contains(word.getValue())) {
                    continue;
                }
                retList.add(word.getValue());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return retList;
    }

    public static List<IWord> CNSplitWord(String str, boolean synonyms) {
        List<IWord> retList = new LinkedList<IWord>();
        try {
            ASegment seg = synonyms ? getFullSeg() : getPureSeg();
            seg.reset(new StringReader(str));
            IWord word = null;
            while ((word = seg.next()) != null) {
                if (escapeSet.contains(word.getValue())) {
                    continue;
                }
                retList.add(word);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return retList;
    }

    public static List<String> CNSingleSplit(String str) {
        List<String> retList = new LinkedList<String>();
        if (str == null) {
            return retList;
        }
        try {
            ASegment seg = getSingleSeg();
            seg.reset(new StringReader(str));
            IWord word = null;
            while ((word = seg.next()) != null) {
                if (escapeSet.contains(word.getValue())) {
                    continue;
                }
                retList.add(word.getValue());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return retList;
    }

    /**
     * 中文完全分词：1.先分成词组(长词)2. 再分成词组(短词)
     * 3. 再分成单字 4. 去重 5. 返回
     *
     * @param str
     * @param synonyms
     * @return
     */
    public static List<String> CNSplitFull(String str, boolean synonyms) {
        if (str == null)
            return new LinkedList<String>();
        str = str.replaceAll("\\pP", " ");
        Set<String> set = new HashSet<String>();
        set.addAll(CNSplit(str, synonyms));
        set.addAll(CNSplitShort(str));
        set.addAll(CNSingleSplit(str));
        return new LinkedList<String>(set);
    }

    public static List<String> CNSplitFull(String str) {
        return CNSplitFull(str, true);
    }

    /**
     * 返回分词对象，较短的词优先返回。不使用近义词。否则请使用
     *
     * @param str
     * @return
     * @see #CNSplit(String, boolean)
     */
    private static List<String> CNSplitShort(String str) {
        List<String> retList = new LinkedList<String>();
        if (str == null) {
            return retList;
        }
        try {
            SimpleShortSeg seg = (SimpleShortSeg) getPureSegShort();
            seg.reset(str);
            IWord[] words = null;
            while ((words = seg.nextAll()) != null) {
                for (IWord word : words) {
                    if (escapeSet.contains(word.getValue())) {
                        continue;
                    }
                    retList.add(word.getValue());
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return retList;
    }

    /**
     * 通用的分词方法,也是要去掉的,换成全新的.
     *
     * @param chinese
     * @return
     */
    public static String cnSplitAndToSpell(String chinese) {
        if (StringUtils.isBlank(chinese)) return "";
        StringBuilder like = new StringBuilder();
        List<String> cnSplit = JcsegFactory.CNSplit(chinese, false);
        for (String cn : cnSplit) {
            String[] cnSpellFirst = PinYinUtils.convertToSpellPolyphonic(cn, true);
            if (cnSpellFirst != null)
                for (String c : cnSpellFirst) {
                    like.append(c).append(" ");
                }
            String[] cnSpell = PinYinUtils.convertToSpellPolyphonic(cn, false);
            if (cnSpell != null)
                for (String c : cnSpell) {
                    like.append(c).append(" ");
                }
        }
        like.append(ListUtil.implode(cnSplit, " "));
        return StringUtil.uniqueString(like.toString());
    }

    private static List<String> segment(String input, ISegment seg) {
        List<String> retList = Lists.newArrayList();
        if (!StringUtils.isEmpty(input)) {
            try (Reader reader = new StringReader(input)) {
                seg.reset(reader);
                IWord word;
                while ((word = seg.next()) != null) {
                    if (escapeSet.contains(word.getValue())) {
                        continue;
                    }
                    retList.add(word.getValue());
                }
            } catch (IOException e) {
                log.error("执行input: " + input + " 分词异常, ISegment: " + seg.getClass().getName(), e);
            }
        }

        return retList;
    }
}
