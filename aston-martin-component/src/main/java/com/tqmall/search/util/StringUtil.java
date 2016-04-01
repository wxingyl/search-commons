package com.tqmall.search.util;

import com.google.common.collect.Lists;
import com.tqmall.search.common.utils.StrValueConvert;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.SimpleDateFormat;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 字符串处理公用类
 * Created by wcong on 14-9-24.
 */
public class StringUtil {

    protected static Logger log = LoggerFactory.getLogger(StringUtil.class);

    private final static String DATE_FORMAT_LONG = "yyyy-MM-dd HH:mm:ss";

    private final static String DATE_FORMAT_SHORT = "yyyy-MM-dd";

    /**
     * 驼峰转下划线
     * 默认第一个小写
     *
     * @param old 驼峰字符串
     * @return String
     */
    public static String camelToUnderline(String old) {
        Pattern p = Pattern.compile("[A-Z]");
        if (old == null || old.equals("")) {
            return "";
        }
        StringBuilder builder = new StringBuilder(old);
        Matcher mc = p.matcher(old);
        int i = 0;
        while (mc.find()) {
            builder.replace(mc.start() + i, mc.end() + i, "_" + mc.group().toLowerCase());
            i++;
        }
        return builder.toString();
    }

    /**
     * 下划线转驼峰
     *
     * @param old 下划线字符串
     * @return String 驼峰字符串
     */
    public static String underlineToCamel(String old) {
        if (old == null || old.equals("")) {
            return "";
        }
        StringBuilder builder = new StringBuilder();
        boolean isConvert = false;
        for (char c : old.toCharArray()) {
            if (c == '_') {
                isConvert = true;
                continue;
            }
            if (isConvert) {
                builder.append(Character.toUpperCase(c));
                isConvert = false;
            } else {
                builder.append(c);
            }
        }
        return builder.toString();
    }

    public static String makeGetMethod(String field) {
        return "get" + field.substring(0, 1).toUpperCase() + field.substring(1);
    }

    public static String makeSetMethod(String field) {
        return "set" + field.substring(0, 1).toUpperCase() + field.substring(1);
    }

    /**
     * 目前数判断 �
     *
     * @param str 要判断的字符串
     * @return 是否是乱码
     */
    public static boolean isMessyCode(String str) {
        if (str.contains("�")) {
            return true;
        } else {
            return false;
        }
    }

    public static String makeDateStringWithoutT(String oldString) {
        if (oldString == null) {
            return null;
        }
        return oldString.replace("T", " ").split("\\.")[0];
    }

    public static String firstCharUp(String oldString) {
        if (oldString == null) {
            return null;
        }
        oldString = oldString.trim();
        if (oldString.length() == 0) {
            return oldString;
        }
        return oldString.substring(0, 1).toUpperCase() + oldString.substring(1);
    }

    public static boolean isInteger(String str) {
        try {
            Integer.parseInt(str);
        } catch (Exception e) {
            return false;
        }
        return true;
    }

    public static boolean isDouble(String str) {
        try {
            Double.parseDouble(str);
        } catch (Exception e) {
            return false;
        }
        return true;
    }

    public static boolean isBoolean(String str) {
        return "true".equals(str) || "false".equals(str);
    }

    /**
     * yyyy-MM-dd HH:mm:ss
     *
     * @param str
     * @return
     */
    public static Date toDateTime(String str) {
        Date d = null;
        SimpleDateFormat df = new SimpleDateFormat(DATE_FORMAT_LONG);
        try {
            d = df.parse(str);
        } catch (Exception e) {
        }
        return d;
    }

    public static boolean isDate(String str) {
        try {
            new SimpleDateFormat(DATE_FORMAT_SHORT).parse(str);
        } catch (Exception e) {
            return false;
        }
        return true;
    }

    public static boolean isDateTime(String str) {
        SimpleDateFormat df = new SimpleDateFormat(DATE_FORMAT_LONG);
        try {
            df.parse(str);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public static boolean isDateOnly(String str) {
        if (isDate(str)) {
            SimpleDateFormat df = new SimpleDateFormat(DATE_FORMAT_LONG);
            try {
                df.parse(str);
            } catch (Exception e) {
                return true;
            }
        }
        return false;
    }

    public static String formatDate(String str) {
        if (isDateOnly(str)) {
            return formatDate(str, true);
        }
        return formatDate(str, false);
    }

    public static String formatDate(String str, boolean onlyDate) {
        SimpleDateFormat df = new SimpleDateFormat(DATE_FORMAT_LONG);
        if (onlyDate) {
            df = new SimpleDateFormat(DATE_FORMAT_SHORT);
        }
        Date date = null;
        try {
            date = df.parse(str);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return df.format(date);
    }

    public static Object formatValue(String str) {
        if (str == null) {
            return null;
        }
        if (isDate(str)) {
            return formatDate(str);
        }
        if (isInteger(str)) {
            return Integer.parseInt(str);
        }
        if (isDouble(str)) {
            return Double.parseDouble(str);
        }
        if (isBoolean(str)) {
            return Boolean.parseBoolean(str);
        }
        return str;
    }

    /**
     * default is
     */
    public static <T> List<T> splitToList(String str, char ch, StrValueConvert<T> convert) {
        String[] array = StringUtils.split(str, ch);
        if (array == null || array.length == 0) return null;
        List<T> list = Lists.newArrayListWithExpectedSize(array.length);
        for (String s : array) {
            list.add(convert.convert(s));
        }
        return list;
    }

    /**
     * 把一个字符串转换成后缀数组，用于在elasticsearch中使用prefixFilter查询<br/>
     * 如果需要转换成拼音首字母，请使用
     *
     * @param str
     * @return 后缀数组
     * @See PinYinUtils.convertToFirstSpellArr
     */
    public static String[] convertToSuffixArr(String str) {
        if (str == null || str.trim().length() == 0) {
            return new String[]{};
        }
        String q = str.trim();
        List<String> strList = new LinkedList<String>();
        for (int i = 0; i < q.length(); i++) {
            String item = q.substring(i);
            if (item != null && item.length() > 0) {
                strList.add(item);
            }
        }
        if (strList.isEmpty()) {
            return new String[]{};
        }
        String[] ret = new String[strList.size()];
        return strList.toArray(ret);
    }

    /**
     * 修复一些词——“暂停词”，比如空格等等会导致搜索出错的词，需要在建索引和查询时同时使用
     *
     * @param source
     * @return
     */
    public static String fixPauseWords(String source) {
        return fixPauseWords(source, null, null);
    }

    /**
     * 修复一些词——“暂停词”，比如空格等等会导致搜索出错的词，需要在建索引和查询时同时使用
     *
     * @param source
     * @param regexp  正则
     * @param replace
     * @return
     */
    public static String fixPauseWords(String source, String regexp, String replace) {
        if (source == null || source.length() == 0) {
            return source;
        }
        if (regexp == null) {
            regexp = "\\s";
        }
        if (replace == null) {
            replace = "";
        }
        return source.replaceAll(regexp, replace);
    }

    public static String fixNull(String str) {
        if (str == null)
            return "";
        else
            return str;
    }

    /**
     * 把集合转换成数组，结果是字符串。String.valueOf();
     *
     * @param collection
     * @return
     */
    public static String[] collectionToArray(Collection collection) {
        if (collection == null || collection.isEmpty()) {
            return new String[0];
        }
        Object[] objects = collection.toArray();
        String[] strings = new String[objects.length];
        int i = 0;
        for (Object o : objects) {
            strings[i] = String.valueOf(o);
            i++;
        }
        return strings;
    }

    /**
     * come from org.springframework.util.StringUtils
     *
     * @return 与jdk自带的不同的是，如果最后一个字符是分割的，会返回一个空串，而不是没有
     */
    public static String[] split(String toSplit, String delimiter) {
        if (hasLength(toSplit) && hasLength(delimiter)) {
            int offset = toSplit.indexOf(delimiter);
            if (offset < 0) {
                return null;
            } else {
                String beforeDelimiter = toSplit.substring(0, offset);
                String afterDelimiter = toSplit.substring(offset + delimiter.length());
                return new String[]{beforeDelimiter, afterDelimiter};
            }
        } else {
            return null;
        }
    }

    /**
     * come from org.springframework.util.StringUtils
     */
    public static boolean hasLength(CharSequence str) {
        return str != null && str.length() > 0;
    }

    /**
     * 字符串去重,按空白分开,把重复的字符串去掉
     *
     * @param str
     * @return
     */
    public static String uniqueString(String str) {
        Set<String> strs = new HashSet<>();
        char[] chars = str.toCharArray();
        StringBuilder builder = new StringBuilder();
        for (char c : chars) {
            if (Character.isSpaceChar(c)) {
                if (builder.length() > 0) {
                    strs.add(builder.toString());
                    builder.setLength(0);
                }
            } else {
                builder.append(c);
            }
        }
        if (builder.length() > 0) {
            strs.add(builder.toString());
            builder.setLength(0);
        }
        strs.remove("");
        return ListUtil.implode(strs, " ");
    }
}
