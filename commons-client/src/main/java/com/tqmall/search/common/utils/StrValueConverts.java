package com.tqmall.search.common.utils;

import java.math.BigDecimal;

/**
 * Created by xing on 15/9/30.
 * string value transfer builder
 */
public abstract class StrValueConverts {

    @SuppressWarnings("unchecked")
    public static <T extends Comparable<T> > ComparableStrValueConvert<T> getConvert(Class<T> cls) {
        ComparableStrValueConvert ret;
        if (cls == Integer.class || cls == Integer.TYPE) {
            ret = IntStrValueConvert.INSTANCE;
        } else if (cls == String.class) {
            ret = StringStrValueConvert.INSTANCE;
        } else if (cls == Long.class || cls == Long.TYPE) {
            ret = LongStrValueConvert.INSTANCE;
        } else if (cls == Double.class || cls == Double.TYPE) {
            ret = DoubleStrValueConvert.INSTANCE;
        } else if (cls == BigDecimal.class) {
            ret = BigDecimalStrValueConvert.INSTANCE;
        } else {
            return null;
        }
        return ret;
    }

    public static <T extends Comparable<T> > ComparableStrValueConvert<T> getConvert(Class<T> cls, final T defaultValue) {
        final AbstractCmpStrValueConvert<T> convert = (AbstractCmpStrValueConvert<T>) getConvert(cls);
        if (convert == null || defaultValue == null) return convert;
        if (convert.defaultValue().equals(defaultValue)) return convert;
        return new AbstractCmpStrValueConvert<T>() {

            @Override
            protected T innerConvert(String s) {
                return convert.convert(s);
            }

            @Override
            protected T defaultValue() {
                return defaultValue;
            }
        };
    }
    /**
     * @return have error will return {@link IntStrValueConvert#defaultValue()}
     */
    public static int intConvert(String input) {
        return IntStrValueConvert.INSTANCE.convert(input);
    }

    public static int intConvert(String input, int defaultValue) {
        return convert(input, defaultValue, Integer.TYPE);
    }

    /**
     * @return have error will return {@link LongStrValueConvert#defaultValue()}
     */
    public static long longConvert(String input) {
        return LongStrValueConvert.INSTANCE.convert(input);
    }

    public static long longConvert(String input, long defaultValue) {
        return convert(input, defaultValue, Long.TYPE);
    }

    /**
     * @return have error will return {@link DoubleStrValueConvert#defaultValue()}
     */
    public static double doubleConvert(String input) {
        return DoubleStrValueConvert.INSTANCE.convert(input);
    }

    public static double doubleConvert(String input, double defaultValue) {
        return convert(input, defaultValue, Double.TYPE);
    }

    /**
     * @return can not null, have error will return {@link BigDecimalStrValueConvert#defaultValue()}
     */
    public static BigDecimal bigDecimalConvert(String input) {
        return BigDecimalStrValueConvert.INSTANCE.convert(input);
    }

    public static BigDecimal bigDecimalConvert(String input, BigDecimal defaultValue) {
        return convert(input, defaultValue, BigDecimal.class);
    }

    public static <T extends Comparable<T> > T convert(final String input, final T defaultValue, final Class<T> cls) {
        ComparableStrValueConvert<T> convert = getConvert(cls, defaultValue);
        if (convert == null) {
            throw new IllegalArgumentException("class: " + cls + " is unsupported");
        }
        return convert.convert(input);
    }

    public static abstract class AbstractCmpStrValueConvert<T extends Comparable<T>> implements ComparableStrValueConvert<T> {

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

    static class StringStrValueConvert extends AbstractCmpStrValueConvert<String> {

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

    static class IntStrValueConvert extends AbstractCmpStrValueConvert<Integer> {

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

    static class LongStrValueConvert extends AbstractCmpStrValueConvert<Long> {

        final static LongStrValueConvert INSTANCE = new LongStrValueConvert();

        @Override
        protected Long innerConvert(String s) {
            return Long.parseLong(s, 10);
        }

        @Override
        protected Long defaultValue() {
            return 0L;
        }

    }

    static class DoubleStrValueConvert extends AbstractCmpStrValueConvert<Double> {

        final static DoubleStrValueConvert INSTANCE = new DoubleStrValueConvert();

        final static Double DEFAULT_VALUE = 0.0D;

        @Override
        protected Double innerConvert(String s) {
            return Double.valueOf(s);
        }

        @Override
        protected Double defaultValue() {
            return DEFAULT_VALUE;
        }
    }

    static class BigDecimalStrValueConvert extends AbstractCmpStrValueConvert<BigDecimal> {

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
