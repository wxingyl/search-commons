package com.tqmall.search.util;

import com.google.common.collect.ImmutableSet;
import org.lionsoul.jcseg.filter.ENSCFilter;

import java.util.Set;

/**
 * 过滤字符串
 * Created by wcong on 14-5-22.
 */
public class Filter {

    private static Set<Character> charSet = ImmutableSet.of('.', '/', '-', '+', '*');

    public static  String filterString(String old){
        StringBuilder sb = new StringBuilder();
        for (char a : old.toCharArray()) {
            if (FullHalfConverter.isFullChar(a) ) {
                a = FullHalfConverter.toHalf(a);
            }
            if (Character.getType(a) == Character.OTHER_LETTER
                    || ENSCFilter.getEnCharType(a)==  ENSCFilter.EN_NUMERIC
                    || ENSCFilter.isLowerCaseLetter(a)
                    || charSet.contains(a)
            ) {
                sb.append(a);
            } else if ( ENSCFilter.isUpperCaseLetter(a)  ) {
                sb.append(((char) ENSCFilter.toLowerCase(a)));
            }
        }
        return sb.toString();
    }

    public static boolean isLexDenyStr(String str) {
        for (char a : str.toCharArray()) {
            if (FullHalfConverter.isFullChar(a) ) {
                a = FullHalfConverter.toHalf(a);
            }
            if (JcsegFactory.escapeSet.contains(String.valueOf(a))) {
                return true;
            }
        }
        return false;
    }

}
