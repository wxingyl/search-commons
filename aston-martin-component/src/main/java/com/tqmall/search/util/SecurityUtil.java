package com.tqmall.search.util;

import org.apache.commons.codec.binary.Base64;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Created by chenyongfu on 14-11-20.
 */
public class SecurityUtil {

    private final static Logger logger = LoggerFactory.getLogger(SecurityUtil.class);
    /**
     * MD5 加密
     */
    public static String MD5(String str) {
        MessageDigest messageDigest = null;

        try {
            messageDigest = MessageDigest.getInstance("MD5");
            messageDigest.reset();
            messageDigest.update(str.getBytes("UTF-8"));
        } catch (NoSuchAlgorithmException e) {
            logger.error("NoSuchAlgorithmException caught!",e);
            return null;
        } catch (UnsupportedEncodingException e) {
            logger.error("MD5加密失败",e);
            return null;
        }

        byte[] byteArray = messageDigest.digest();
        StringBuffer md5StrBuff = new StringBuffer();

        for (int i = 0; i < byteArray.length; i++) {
            if (Integer.toHexString(0xFF & byteArray[i]).length() == 1)
                md5StrBuff.append("0").append(Integer.toHexString(0xFF & byteArray[i]));
            else
                md5StrBuff.append(Integer.toHexString(0xFF & byteArray[i]));
        }
        return md5StrBuff.toString();
    }

    /**
     * 编码
     *
     * @param origin
     * @return
     */
    public static String base64Encode(String origin) {
        if (origin == null) {
            throw new RuntimeException("origin could  not be null or empty!");
        }
        Base64 base64 = new Base64();
        byte[] res = base64.encode(origin.getBytes(Charset.forName("utf-8")));
        return new String(res, Charset.forName("utf-8"));
    }

    /**
     * 解码
     *
     * @param encodeStr
     * @return
     */
    public static String base64Decode(String encodeStr) {
        if (encodeStr == null) {
            throw new RuntimeException("encodeStr could  not be null or empty!");
        }
        Base64 base64 = new Base64();
        byte[] res = base64.decode(encodeStr.getBytes(Charset.forName("utf-8")));
        return new String(res, Charset.forName("utf-8"));
    }
}
