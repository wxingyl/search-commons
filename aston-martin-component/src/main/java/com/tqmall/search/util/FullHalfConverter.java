package com.tqmall.search.util;

/**
 * Created by yongfu.chen on 14-7-3.
 */
public class FullHalfConverter {

    public static Boolean isHalfString(String QJstr) {
        String Tstr = "";
        byte[] b = null;
        for (int i = 0; i < QJstr.length(); i++) {
            Tstr = QJstr.substring(i, i + 1);
            if (Tstr.equals("　")) { // 全角空格
                return false;
            }
            try {
                b = Tstr.getBytes("unicode");  // 得到 unicode 字节数据
                if (b[2] == -1) {  // 表示全角
                    return false;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

        } // end for.
        return true;
    }

    /**
     * 全角转半角的 转换函数
     *
     * @param QJstr
     * @return String
     * @Methods Name full2HalfChange
     * @Create In 2012-8-24 By v-jiangwei
     */
    public static String fullToHalf(String QJstr) {
        StringBuffer outStrBuf = new StringBuffer("");
        String Tstr = "";
        byte[] b = null;
        for (int i = 0; i < QJstr.length(); i++) {
            Tstr = QJstr.substring(i, i + 1);
            // 全角空格转换成半角空格
            if (Tstr.equals("　")) {
                outStrBuf.append(" ");
                continue;
            }
            try {
                b = Tstr.getBytes("unicode");  // 得到 unicode 字节数据
                if (b[2] == -1) {  // 表示全角
                    b[3] = (byte) (b[3] + 32);
                    b[2] = 0;
                    outStrBuf.append(new String(b, "unicode"));
                } else {
                    outStrBuf.append(Tstr);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

        } // end for.
        return outStrBuf.toString();


    }

    /**
     * 半角转全角
     *
     * @param QJstr
     * @return String
     * @Methods Name half2Fullchange
     * @Create In 2012-8-24 By v-jiangwei
     */
    public static String halfToFull(String QJstr) {
        StringBuffer outStrBuf = new StringBuffer("");
        String Tstr = "";
        byte[] b = null;
        for (int i = 0; i < QJstr.length(); i++) {
            Tstr = QJstr.substring(i, i + 1);
            if (Tstr.equals(" ")) {  // 半角空格
                outStrBuf.append(Tstr);
                continue;
            }
            try {
                b = Tstr.getBytes("unicode");
                if (b[2] == 0) {  // 半角
                    b[3] = (byte) (b[3] - 32);
                    b[2] = -1;
                    outStrBuf.append(new String(b, "unicode"));
                } else {
                    outStrBuf.append(Tstr);
                }
                return outStrBuf.toString();
            } catch (Exception e) {
                e.printStackTrace();
            }

        }
        return outStrBuf.toString();
    }

    /**
     * 判断全角字符
     *
     * @param input
     * @return
     */
    public static Boolean isFullChar(char input) {
        return ((input == '\u3000') || (input > '\uFF00' && input < '\uFF5F'));
    }

    /**
     * 全角字符转半角
     *
     * @param input String.
     * @return 半角字符
     */
    public static char toHalf(char input) {

        if (input == '\u3000') {
            input = ' ';
        } else if (input > '\uFF00' && input < '\uFF5F') {
            input = (char) (input - 65248);
        }
        return input;
    }
}
