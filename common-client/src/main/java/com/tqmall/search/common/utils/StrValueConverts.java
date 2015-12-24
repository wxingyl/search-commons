package com.tqmall.search.common.utils;

import java.math.BigDecimal;

/**
 * Created by xing on 15/9/30.
 * string value transfer builder
 */
public abstract class StrValueConverts {

    @SuppressWarnings("unchecked")
    public static <T> StrValueConvert<T> getConvert(Class<T> cls) {
        if (cls == Integer.class || cls == Integer.TYPE) {
            return (StrValueConvert<T>) IntStrValueConvert.INSTANCE;
        } else if (cls == String.class) {
            return (StrValueConvert<T>) StringStrValueConvert.INSTANCE;
        } else if (cls == Long.class || cls == Long.TYPE) {
            return (StrValueConvert<T>) LongStrValueConvert.INSTANCE;
        } else if (cls == Double.class || cls == Double.TYPE) {
            return (StrValueConvert<T>) DoubleStrValueConvert.INSTANCE;
        } else if (cls == BigDecimal.class) {
            return (StrValueConvert<T>) BigDecimalStrValueConvert.INSTANCE;
        } else {
            return null;
        }
    }

    /**
     * @return can not null, have error will return {@link IntStrValueConvert#defaultValue()}
     */
    public static Integer intConvert(String input) {
        return IntStrValueConvert.INSTANCE.convert(input);
    }

    /**
     * @return can not null, have error will return {@link LongStrValueConvert#defaultValue()}
     */
    public static Long longConvert(String input) {
        return LongStrValueConvert.INSTANCE.convert(input);
    }

    /**
     * @return can not null, have error will return {@link DoubleStrValueConvert#defaultValue()}
     */
    public static Double doubleConvert(String input) {
        return DoubleStrValueConvert.INSTANCE.convert(input);
    }

    /**
     * @return can not null, have error will return {@link BigDecimalStrValueConvert#defaultValue()}
     */
    public static BigDecimal bigDecimalConvert(String input) {
        return BigDecimalStrValueConvert.INSTANCE.convert(input);
    }

    static abstract class AbstractStrValueConvert<T extends Comparable<T>> implements StrValueConvert<T> {

        protected abstract T innerConvert(String s);

        protected abstract T defaultValue();

        @Override
        public int compare(T o1, T o2) {
            return o1.compareTo(o2);
        }

        @Override
        final public T convert(String s) {
            if (s == null || s.isEmpty()) return defaultValue();
            try {
                return innerConvert(s);
            } catch (NumberFormatException e) {
                return defaultValue();
            }
        }
    }

    static class StringStrValueConvert extends AbstractStrValueConvert<String> {

        final static StringStrValueConvert INSTANCE = new StringStrValueConvert();

        @Override
        protected String innerConvert(String s) {
            return s;
        }

        @Override
        protected String defaultValue() {
            return null;
        }
    }

    static class IntStrValueConvert extends AbstractStrValueConvert<Integer> {

        final static IntStrValueConvert INSTANCE = new IntStrValueConvert();

        @Override
        protected Integer innerConvert(String s) {
            return Integer.parseInt(s, 10);
        }

        @Override
        protected Integer defaultValue() {
            return 0;
        }

    }

    static class LongStrValueConvert extends AbstractStrValueConvert<Long> {

        final static LongStrValueConvert INSTANCE = new LongStrValueConvert();

        @Override
        protected Long innerConvert(String s) {
            return Long.parseLong(s, 10);
        }

        @Override
        protected Long defaultValue() {
            return 0l;
        }

    }

    static class DoubleStrValueConvert extends AbstractStrValueConvert<Double> {

        final static DoubleStrValueConvert INSTANCE = new DoubleStrValueConvert();

        final static Double DEFAULT_VALUE = 0.0d;

        @Override
        protected Double innerConvert(String s) {
            return Double.valueOf(s);
        }

        @Override
        protected Double defaultValue() {
            return DEFAULT_VALUE;
        }
    }

    static class BigDecimalStrValueConvert extends AbstractStrValueConvert<BigDecimal> {

        final static BigDecimalStrValueConvert INSTANCE = new BigDecimalStrValueConvert();

        @Override
        protected BigDecimal innerConvert(String s) {
            return new BigDecimal(s);
        }

        @Override
        protected BigDecimal defaultValue() {
            return BigDecimal.ZERO;
        }
    }
}
