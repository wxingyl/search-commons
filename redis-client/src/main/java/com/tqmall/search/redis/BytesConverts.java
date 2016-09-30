package com.tqmall.search.redis;

import com.tqmall.search.commons.lang.SmallDateFormat;
import com.tqmall.search.commons.lang.StrValueConvert;
import com.tqmall.search.commons.utils.CommonsUtils;
import com.tqmall.search.commons.utils.JsonUtils;
import com.tqmall.search.commons.utils.StrValueConverts;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * date 16/4/16 下午4:12
 * {@link BytesConvert}工具类
 *
 * @author 尚辰
 */
public final class BytesConverts {

    private static final Logger log = LoggerFactory.getLogger(BytesConverts.class);

    public static final byte[] EMPTY_BYTES = new byte[0];

    /**
     * 设置键key的过期时间，单位时秒
     */
    public static final byte[] EX_BYTES = "EX".getBytes(StandardCharsets.UTF_8);

    /**
     * 设置键key的过期时间，单位时毫秒
     */
    public static final byte[] PX_BYTES = "PX".getBytes(StandardCharsets.UTF_8);

    /**
     * 只有键key不存在的时候才会设置key的值
     */
    public static final byte[] NX_BYTES = "NX".getBytes(StandardCharsets.UTF_8);

    /**
     * 只有键key存在的时候才会设置key的值
     */
    public static final byte[] XX_BYTES = "XX".getBytes(StandardCharsets.UTF_8);

    /**
     * 读取key但是没有命中
     */
    public static final byte[] NIL_BYTES = "nil".getBytes(StandardCharsets.UTF_8);

    private BytesConverts() {
    }

    @SuppressWarnings({"rawstypes", "unchecked"})
    public static <T> BytesConvert<T> basicConvert(Class<T> cls) {
        if (cls.isAssignableFrom(Date.class)) {
            return (BytesConvert<T>) DateBytesConvert.DEFAULT_INSTANCE;
        } else {
            return BasicBytesConvert.valueOf(cls);
        }
    }

    public static BytesConvert<Date> dateConvert(SmallDateFormat dateFormat) {
        return new DateBytesConvert(dateFormat);
    }

    @SuppressWarnings({"rawstypes", "unchecked"})
    public static <T> BytesConvert<T> serializedBeanConvert() {
        return (BytesConvert<T>) SerializedBeanBytesConvert.INSTANCE;
    }

    @SuppressWarnings({"rawstypes", "unchecked"})
    public static <T> BytesConvert<T> jsonBeanConvert() {
        return (BytesConvert<T>) JsonBeanBytesConvert.INSTANCE;
    }

    /**
     * 基本的数据类型, 即可以通过{@link StrValueConverts#getBasicConvert(Class)}获取的类型
     *
     * @param <T> 数据类型
     */
    static class BasicBytesConvert<T> implements BytesConvert<T> {

        static final ConcurrentMap<Class, BasicBytesConvert> CACHE = new ConcurrentHashMap<>();

        private final StrValueConvert<T> convert;

        BasicBytesConvert(Class<T> cls) throws IllegalArgumentException {
            this.convert = StrValueConverts.getBasicConvert(cls);
        }

        @SuppressWarnings({"rawstypes", "unchecked"})
        static <T> BasicBytesConvert<T> valueOf(Class<T> cls) {
            BasicBytesConvert c;
            if ((c = CACHE.get(cls)) == null) {
                c = new BasicBytesConvert<>(cls);
                CACHE.putIfAbsent(cls, c);
            }
            return c;
        }

        @Override
        public byte[] toBytes(T obj) {
            return obj.toString().getBytes(StandardCharsets.UTF_8);
        }

        @Override
        public T initBytes(byte[] bytes, Class<T> cls) {
            return convert.convert(new String(bytes, StandardCharsets.UTF_8));
        }
    }

    static class DateBytesConvert implements BytesConvert<Date> {

        static final DateBytesConvert DEFAULT_INSTANCE = new DateBytesConvert(CommonsUtils.dateFormat());

        private final SmallDateFormat dateFormat;

        DateBytesConvert(SmallDateFormat dateFormat) {
            this.dateFormat = dateFormat;
        }

        @Override
        public byte[] toBytes(Date obj) {
            return dateFormat.format(obj).getBytes(StandardCharsets.UTF_8);
        }

        @Override
        public Date initBytes(byte[] bytes, Class<Date> cls) {
            return dateFormat.convert(new String(bytes, StandardCharsets.UTF_8));
        }
    }

    /**
     * 自定义的bean的字节转换, 通过序列化/反序列化搞定
     *
     * @param <T> 必须implement {@link Serializable}
     */
    static class SerializedBeanBytesConvert<T> implements BytesConvert<T> {

        static final SerializedBeanBytesConvert INSTANCE = new SerializedBeanBytesConvert();

        @Override
        public byte[] toBytes(T obj) {
            ByteArrayOutputStream bytesOut = new ByteArrayOutputStream(256);
            try (ObjectOutputStream out = new ObjectOutputStream(bytesOut)) {
                out.writeObject(obj);
                return bytesOut.toByteArray();
            } catch (IOException e) {
                log.error("SerializedBeanBytesConvert write obj to byte array have exception", e);
                return null;
            }
        }

        @Override
        public T initBytes(byte[] bytes, Class<T> cls) {
            try (ObjectInputStream in = new ObjectInputStream(new ByteArrayInputStream(bytes))) {
                return cls.cast(in.readObject());
            } catch (IOException | ClassNotFoundException e) {
                log.error("SerializedBeanBytesConvert init obj from byte array have exception", e);
                return null;
            }
        }
    }

    static class JsonBeanBytesConvert<T> implements BytesConvert<T> {

        static final JsonBeanBytesConvert INSTANCE = new JsonBeanBytesConvert();

        @Override
        public byte[] toBytes(T obj) {
            String str = JsonUtils.toJsonStr(obj);
            return str == null ? EMPTY_BYTES : str.getBytes(StandardCharsets.UTF_8);
        }

        @Override
        public T initBytes(byte[] bytes, Class<T> cls) {
            return JsonUtils.parseObject(new String(bytes, StandardCharsets.UTF_8), cls);
        }
    }

}
