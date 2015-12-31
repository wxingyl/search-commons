package com.tqmall.search.common;

import org.junit.Test;
import sun.reflect.CallerSensitive;
import sun.reflect.Reflection;

/**
 * Created by xing on 15/12/31.
 * error code相关的test
 */
public class ErrorCodeTest {

    @CallerSensitive
    public static void fun() {
        Class cls = Reflection.getCallerClass();
        System.out.printf(cls.toString());
    }

    static abstract class Base {

        public void init() {
            fun();
        }
    }

    static class Full extends Base {
        @Override
        public void init() {
            super.init();
        }
    }

    @Test
    public void test() {
        Full full = new Full();
        full.init();
    }
}
