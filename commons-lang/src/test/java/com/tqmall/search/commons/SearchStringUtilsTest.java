package com.tqmall.search.commons;

import com.tqmall.search.commons.param.Param;
import com.tqmall.search.commons.utils.SearchStringUtils;
import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created by xing on 16/1/22.
 * {@link SearchStringUtils}测试类
 */
public class SearchStringUtilsTest {

    @Test
    public void joinTest() {
        List<Integer> list = new ArrayList<>();
        Collections.addAll(list, 1, 2, 1);

        System.out.println("list: " + list);
        String str = SearchStringUtils.join(list, Param.SEPARATOR_CHAR);
        Assert.assertEquals(str, "1,2,1");

        list.add(null);
        System.out.println("list: " + list);
        str = SearchStringUtils.join(list, Param.SEPARATOR_CHAR);
        Assert.assertEquals(str, "1,2,1,");

        list.add(0, null);
        System.out.println("list: " + list);
        str = SearchStringUtils.join(list, Param.SEPARATOR_CHAR);
        Assert.assertEquals(str, ",1,2,1,");

        list.set(2, null);
        System.out.println("list: " + list);
        str = SearchStringUtils.join(list, Param.SEPARATOR_CHAR);
        Assert.assertEquals(str, ",1,,1,");

        Integer[] array = new Integer[5];
        array[1] = 1;
        array[2] = 2;
        array[3] = 1;

        str = SearchStringUtils.join(array, Param.SEPARATOR_CHAR);
        Assert.assertEquals(str, ",1,2,1,");

        array[2] = null;
        str = SearchStringUtils.join(array, Param.SEPARATOR_CHAR);
        Assert.assertEquals(str, ",1,,1,");

        array[0] = 6;
        str = SearchStringUtils.join(array, Param.SEPARATOR_CHAR);
        Assert.assertEquals(str, "6,1,,1,");

        array[4] = 6;
        str = SearchStringUtils.join(array, Param.SEPARATOR_CHAR);
        Assert.assertEquals(str, "6,1,,1,6");
    }

    /**
     * StringUtils.split(null, *)         = null
     * StringUtils.split("", *)           = []
     * StringUtils.split("a.b.c", '.')    = ["a", "b", "c"]
     * StringUtils.split("a..b.c", '.')   = ["a", "b", "c"]
     * StringUtils.split("a:b:c", '.')    = ["a:b:c"]
     * StringUtils.split("a b c", ' ')    = ["a", "b", "c"]
     */
    @Test
    public void splitTest() {
        String str = null;
        String[] array = SearchStringUtils.split(str, Param.SEPARATOR_CHAR);
        Assert.assertArrayEquals(null, array);

        str = "";
        array = SearchStringUtils.split(str, Param.SEPARATOR_CHAR);
        Assert.assertArrayEquals(SearchStringUtils.EMPTY_STRING_ARRAY, array);

        str = "a,b,c";
        array = SearchStringUtils.split(str, Param.SEPARATOR_CHAR);
        String[] correctArray = new String[]{"a", "b", "c"};
        Assert.assertArrayEquals(correctArray, array);

        str = "a,,b,c";
        array = SearchStringUtils.split(str, Param.SEPARATOR_CHAR);
        Assert.assertArrayEquals(correctArray, array);

        str = "a:b:c";
        array = SearchStringUtils.split(str, Param.SEPARATOR_CHAR);
        Assert.assertArrayEquals(new String[]{"a:b:c"}, array);

        str = "a b c";
        array = SearchStringUtils.split(str, ' ');
        Assert.assertArrayEquals(correctArray, array);

        str = "a b  c";
        array = SearchStringUtils.split(str, ' ');
        Assert.assertArrayEquals(correctArray, array);
    }

    @Test
    public void stringArrayTrimTest() {
        for (String s : SearchStringUtils.split(" xing ,wang ", ',')) {
            System.out.println("s: " + s + ", length: " + s.length());
        }
        for (String s : SearchStringUtils.splitTrim(" xing ,wang ", ',')) {
            Assert.assertTrue(s.length() == 4);
        }
    }
}
