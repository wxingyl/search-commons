package com.tqmall.search.common;

import com.tqmall.search.common.utils.ClientConst;
import com.tqmall.search.common.utils.ClientStringUtils;
import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;

/**
 * Created by xing on 16/1/22.
 * {@link com.tqmall.search.common.utils.ClientStringUtils}测试类
 */
public class ClientStringUtilsTest {

    @Test
    public void joinTest() {
        ArrayList<Integer> list = new ArrayList<>();
        list.add(1);
        list.add(2);
        list.add(1);

        System.out.println("list: " + list);
        String str = ClientStringUtils.join(list, ClientConst.SEPARATOR_CHAR);
        Assert.assertEquals(str, "1,2,1");

        list.add(null);
        System.out.println("list: " + list);
        str = ClientStringUtils.join(list, ClientConst.SEPARATOR_CHAR);
        Assert.assertEquals(str, "1,2,1,");

        list.add(0, null);
        System.out.println("list: " + list);
        str = ClientStringUtils.join(list, ClientConst.SEPARATOR_CHAR);
        Assert.assertEquals(str, ",1,2,1,");

        list.set(2, null);
        System.out.println("list: " + list);
        str = ClientStringUtils.join(list, ClientConst.SEPARATOR_CHAR);
        Assert.assertEquals(str, ",1,,1,");

        Integer[] array = new Integer[5];
        array[1] = 1;
        array[2] = 2;
        array[3] = 1;

        str = ClientStringUtils.join(array, ClientConst.SEPARATOR_CHAR);
        Assert.assertEquals(str, ",1,2,1,");

        array[2] = null;
        str = ClientStringUtils.join(array, ClientConst.SEPARATOR_CHAR);
        Assert.assertEquals(str, ",1,,1,");

        array[0] = 6;
        str = ClientStringUtils.join(array, ClientConst.SEPARATOR_CHAR);
        Assert.assertEquals(str, "6,1,,1,");

        array[4] = 6;
        str = ClientStringUtils.join(array, ClientConst.SEPARATOR_CHAR);
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
        String[] array = ClientStringUtils.split(str, ClientConst.SEPARATOR_CHAR);
        Assert.assertArrayEquals(null, array);

        str = "";
        array = ClientStringUtils.split(str, ClientConst.SEPARATOR_CHAR);
        Assert.assertArrayEquals(ClientStringUtils.EMPTY_STRING_ARRAY, array);

        str = "a,b,c";
        array = ClientStringUtils.split(str, ClientConst.SEPARATOR_CHAR);
        String[] correctArray = new String[]{"a", "b", "c"};
        Assert.assertArrayEquals(correctArray, array);

        str = "a,,b,c";
        array = ClientStringUtils.split(str, ClientConst.SEPARATOR_CHAR);
        Assert.assertArrayEquals(correctArray, array);

        str = "a:b:c";
        array = ClientStringUtils.split(str, ClientConst.SEPARATOR_CHAR);
        Assert.assertArrayEquals(new String[]{"a:b:c"}, array);

        str = "a b c";
        array = ClientStringUtils.split(str, ' ');
        Assert.assertArrayEquals(correctArray, array);

        str = "a b  c";
        array = ClientStringUtils.split(str, ' ');
        Assert.assertArrayEquals(correctArray, array);
    }

}
