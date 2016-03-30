package com.tqmall.search.util;

import net.sourceforge.pinyin4j.PinyinHelper;
import net.sourceforge.pinyin4j.format.HanyuPinyinCaseType;
import net.sourceforge.pinyin4j.format.HanyuPinyinOutputFormat;
import net.sourceforge.pinyin4j.format.HanyuPinyinToneType;
import net.sourceforge.pinyin4j.format.HanyuPinyinVCharType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

/**
 * Created by chenyongfu on 15-2-2.
 */
public class PinYinUtils {

    private static Logger logger = LoggerFactory.getLogger(PinYinUtils.class);
    //匹配一个中文和非中文字符，可用于替换，替换后只留下多个中文词组
    private final static String regexpSingleWord = "[^\\u4e00-\\u9fa5a]|(?<![\\u4e00-\\u9fa5a])[\\u4e00-\\u9fa5a](?![\\u4e00-\\u9fa5a])";

    private static HanyuPinyinOutputFormat defaultFormat = new HanyuPinyinOutputFormat();

    static {
        defaultFormat.setCaseType(HanyuPinyinCaseType.UPPERCASE);
        defaultFormat.setToneType(HanyuPinyinToneType.WITHOUT_TONE);
        defaultFormat.setVCharType(HanyuPinyinVCharType.WITH_V);
    }

    /**
     * 汉字转换位汉语拼音首字母，list
     *
     * @param chines 汉字
     * @return 拼音首字母 list
     */
    public static List<String> convertToFirstSpellList(String chines) {
        List<String> firstSpellList = new LinkedList<>();
        if (chines == null) {
            return firstSpellList;
        }
        char[] nameChar = chines.trim().toCharArray();
        for (int i = 0; i < nameChar.length; i++) {
            if (nameChar[i] > 128) {
                try {
                    String[] pinyinArr = PinyinHelper.toHanyuPinyinStringArray(nameChar[i], defaultFormat);
                    if (pinyinArr != null && pinyinArr.length > 0 && pinyinArr[0].length() > 0) {
                        firstSpellList.add(String.valueOf(pinyinArr[0].charAt(0)));
                    }
                } catch (Exception e) {
                    logger.error(chines, e);
                }
            } else {
                firstSpellList.add(String.valueOf(nameChar[i]));
            }
        }
        return firstSpellList;
    }

    /**
     * 只转换单词的，也就是两个汉语词以上，其它的过滤掉
     *
     * @param chines
     * @return
     */
    public static String convertToFirstSpellOnlyWord(String chines) {
        chines = chines.replaceAll(regexpSingleWord, " ").replace("\\s+", " ");
        return convertToFirstSpell(chines);
    }

    /**
     * 汉字转换位汉语拼音首字母，英文字符不变
     *
     * @param chines 汉字
     * @return 拼音
     */
    public static String convertToFirstSpell(String chines) {
        StringBuilder pinyinName = new StringBuilder();
        char[] nameChar = chines.toCharArray();
        for (int i = 0; i < nameChar.length; i++) {
            char ch = nameChar[i];
            if ((ch >= 'a' && ch <= 'z') || (ch >= 'A' && ch <= 'Z')) {
                pinyinName.append(nameChar[i]);
            } else if (!Character.isWhitespace(ch)) {
                try {
                    String[] pinyinArr = PinyinHelper.toHanyuPinyinStringArray(ch, defaultFormat);
                    if (pinyinArr != null && pinyinArr.length > 0 && pinyinArr[0].length() > 0) {
                        pinyinName.append(pinyinArr[0].charAt(0));
                    }
                } catch (Exception e) {
                    logger.error(chines, e);
                }
            }
        }
        return pinyinName.toString();
    }

    /**
     * 汉字转换位汉语拼音首字母，英文字符不变<br/>
     * 仅转换成后缀字符串，不需要转换成字母，请使用
     *
     * @param chines 汉字
     * @return 拼音
     * @See StringUtil.convertToSuffixArr
     */
    public static String[] convertToFirstSpellArr(String chines) {
        if (chines == null || chines.trim().length() == 0) {
            return new String[]{};
        }
        String q = chines.trim();
        List<String> strList = new LinkedList<String>();
        for (int i = 0; i < q.length(); i++) {
            String[] itemArr = convertToSpellPolyphonic(q.substring(i), true);
            if (itemArr != null && itemArr.length > 0) {
                for (String item : itemArr) {
                    if (item != null && item.length() > 0) {
                        strList.add(item);
                    }
                }
            }
        }
        if (strList.isEmpty()) {
            return new String[]{};
        }
        String[] ret = new String[strList.size()];
        return strList.toArray(ret);
    }

    /**
     * 只转换单词的，也就是两个汉语词以上，其它的过滤掉
     *
     * @param chines
     * @return
     */
    public static String convertToSpellOnlyWord(String chines) {
        chines = chines.replaceAll(regexpSingleWord, " ").replace("\\s+", " ");
        return convertToSpell(chines);
    }

    /**
     * 汉字转换位汉语拼音，英文字符不变
     *
     * @param chines 汉字
     * @return 拼音
     */
    public static String convertToSpell(String chines) {
        StringBuilder pinyinName = new StringBuilder();
        char[] nameChar = chines.toCharArray();
        for (int i = 0; i < nameChar.length; i++) {
            if (nameChar[i] > 128) {
                try {
                    String[] pinyinArr = PinyinHelper.toHanyuPinyinStringArray(nameChar[i], defaultFormat);
                    if (pinyinArr == null || pinyinArr.length == 0) {
                        continue;
                    }
                    String pinyin = pinyinArr[0];
                    //首字母大写
                    pinyin = String.valueOf(pinyin.charAt(0)).toUpperCase() + pinyin.substring(1, pinyin.length());
                    pinyinName.append(pinyin);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } else {
                pinyinName.append(String.valueOf(nameChar[i]).toUpperCase());
            }
        }
        return pinyinName.toString();
    }

    /**
     * 汉字转换位汉语拼音，英文字符不变
     * 返回多音拼音数组
     *
     * @param chines 汉字
     * @return 拼音
     */
    public static String[] convertToSpellPolyphonic(String chines, boolean isFirstSpell) {
        if (chines == null || chines.length() == 0) return null;
        String[] postArr = convertToSpellPolyphonic(chines.substring(1), isFirstSpell);
        List<String> pList = new LinkedList<String>();
        char nameChar = chines.charAt(0);
        if (nameChar > 128) {
            try {
                String[] pinyinArr = PinyinHelper.toHanyuPinyinStringArray(nameChar, defaultFormat);
                if (pinyinArr != null && pinyinArr.length > 0) {
                    if (isFirstSpell) {
                        for (String pinyin : pinyinArr) {
                            if (pinyin.length() > 0) {
                                pList.add(String.valueOf(pinyin.charAt(0)));
                            }
                        }
                    } else {
                        Collections.addAll(pList, pinyinArr);
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            pList.add(String.valueOf(nameChar).toUpperCase());
        }
        if (postArr == null && pList.isEmpty()) {
            return null;
        }
        if (postArr != null && !pList.isEmpty()) {
            Set<String> pinyinSet = new HashSet<String>();
            for (String py : pList) {
                for (String post : postArr) {
                    pinyinSet.add(py + post);
                }
            }
            postArr = new String[pinyinSet.size()];
            pinyinSet.toArray(postArr);
        } else if (postArr == null) {
            postArr = new String[pList.size()];
            pList.toArray(postArr);
        }
        return postArr;
    }

    public static void main(String[] args) {
        String name = "刹车片";
        for (String item : convertToSpellPolyphonic(name, false)) {
            System.out.println(item);
        }
    }
}