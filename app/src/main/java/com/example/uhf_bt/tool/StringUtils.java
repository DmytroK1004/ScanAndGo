package com.example.uhf_bt.tool;

import com.rscja.utility.StringUtility;

import java.text.SimpleDateFormat;
import java.util.Date;

public class StringUtils {

    public static String replaceUrlWithPlus(String url) {
        // 1. 处理特殊字符
        // 2. 去除后缀名带来的文件浏览器的视图凌乱(特别是图片更�?��如此类似处理，否则有的手机打�?��库，全是我们的缓存图�?
        if (url != null) {
            return url.replaceAll("http://(.)*?/", "")
                    .replaceAll("[.:/,%?&=]", "+").replaceAll("[+]+", "+");
        }
        return null;
    }

    /**
     * 验证ip是否合法
     *
     * @param text ip地址
     * @return 验证信息
     */
    public static Boolean isIP(String text) {
        if (text != null && !text.isEmpty()) {
            // 定义正则表达式
            String regex = "^((25[0-5])|(2[0-4]\\d)|(1\\d\\d)|([1-9]\\d)|\\d)(\\.((25[0-5])|(2[0-4]\\d)|(1\\d\\d)|([1-9]\\d)|\\d)){3}$";
            // 判断ip地址是否与正则表达式匹配
            if (text.matches(regex)) {
                // 返回判断信息
                return true;
            } else {
                // 返回判断信息
                return false;
            }
        }
        // 返回判断信息
        return false;
    }

    /**
     * 验证域名是否合法
     *
     * @param text 域名
     * @return 验证信息
     */
    public static Boolean isDomain(String text) {
        if (text != null && !text.isEmpty()) {
            // 定义正则表达式
            String regex = "^([a-zA-Z0-9\\.\\-]+(\\:[a-zA-Z0-9\\.&amp;%\\$\\-]+)*@)*((25[0-5]|2[0-4][0-9]|[0-1]{1}[0-9]{2}|[1-9]{1}[0-9]{1}|[1-9])\\.(25[0-5]|2[0-4][0-9]|[0-1]{1}[0-9]{2}|[1-9]{1}[0-9]{1}|[1-9]|0)\\.(25[0-5]|2[0-4][0-9]|[0-1]{1}[0-9]{2}|[1-9]{1}[0-9]{1}|[1-9]|0)\\.(25[0-5]|2[0-4][0-9]|[0-1]{1}[0-9]{2}|[1-9]{1}[0-9]{1}|[0-9])|localhost|([a-zA-Z0-9\\-]+\\.)*[a-zA-Z0-9\\-]+\\.(com|edu|gov|int|mil|net|org|biz|arpa|info|name|pro|aero|coop|museum|[a-zA-Z]{2}))$";
            // 判断域名是否与正则表达式匹配
            if (text.matches(regex)) {
                // 返回判断信息
                return true;
            } else {
                // 返回判断信息
                return false;
            }
        }
        // 返回判断信息
        return false;
    }

    public static boolean isEmpty(CharSequence cs) {

        return cs == null || cs.length() == 0;

    }

    public static boolean isNotEmpty(CharSequence cs) {

        return !StringUtils.isEmpty(cs);

    }

    public static String trim(String str) {

        return str == null ? null : str.trim();

    }

    /**
     * 字符串转整数
     *
     * @param str
     * @param defValue
     * @return
     */
    public static int toInt(String str, int defValue) {
        try {
            return Integer.parseInt(str);
        } catch (Exception e) {
        }
        return defValue;
    }

    /**
     * 对象转整数
     *
     * @param obj
     * @return 转换异常返回 0
     */
    public static int toInt(Object obj) {
        if (obj == null)
            return 0;
        return toInt(obj.toString(), 0);
    }

    /**
     * 对象转整数
     *
     * @param obj
     * @return 转换异常返回 0
     */
    public static long toLong(String obj) {
        try {
            return Long.parseLong(obj);
        } catch (Exception e) {
        }
        return 0;
    }

    /**
     * 字符转double
     *
     * @param obj
     * @return 转换异常返回 0
     */
    public static double toDouble(String obj) {
        try {
            return Double.parseDouble(obj);
        } catch (Exception e) {
        }
        return 0;
    }

    /**
     * 字符串转布尔值
     *
     * @param b
     * @return 转换异常返回 false
     */
    public static boolean toBool(String b) {
        try {
            return Boolean.parseBoolean(b);
        } catch (Exception e) {
        }
        return false;
    }

    /**
     * 判断是否为整数 INT
     *
     * @param val
     * @return
     */
    public static Boolean isInt(String val) {
        try {
            Integer.parseInt(val);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public static String getTimeFormat(String pattern, long time) {
        SimpleDateFormat formatter = new SimpleDateFormat(pattern);
        Date curDate = new Date(time);// 获取当前时间
        return formatter.format(curDate);
    }

    public static String getTimeString() {
        return getTimeFormat("yyyyMMddHHmmss", System.currentTimeMillis());
    }

    public static String getTimeFormat(long time) {
        return getTimeFormat("yyyy-MM-dd HH:mm:ss", time);
    }

    /**
     * 判断是否是十六进制  !慎用！
     *
     * @param str
     * @return
     */
    public static boolean isHexNumber(String str) {
        boolean flag = false;
        for (int i = 0; i < str.length(); i++) {
            char cc = str.charAt(i);
            if (cc == '0' || cc == '1' || cc == '2' || cc == '3' || cc == '4'
                    || cc == '5' || cc == '6' || cc == '7' || cc == '8'
                    || cc == '9' || cc == 'A' || cc == 'B' || cc == 'C'
                    || cc == 'D' || cc == 'E' || cc == 'F' || cc == 'a'
                    || cc == 'b' || cc == 'c' || cc == 'd' || cc == 'e'
                    || cc == 'f'
            ) {
                flag = true;
            }
        }
        return flag;
    }

    /**
     * 十六进制字符串转换成char数组
     *
     * @param s
     * @return
     */
    public static char[] HexStringToChars(String s) {
        char[] bytes;
        bytes = new char[s.length() / 2];

        for (int i = 0; i < bytes.length; i++) {
            bytes[i] = (char) Integer.parseInt(s.substring(2 * i, 2 * i + 2),
                    16);
        }

        return bytes;
    }

    public static boolean vailHexInput(String str) {

        if (str == null || str.length() == 0) {
            return false;
        }
        if (str.length() % 2 == 0) {
            return StringUtility.isHexNumberRex(str);
        }

        return false;
    }


    /**
     * 字符串转16进制ASCII
     * 若字符串长度为单数，则补上空格再转为ASCII码
     *
     * @param str 需要转换的字符串
     * @return 16进制ASCII字符串数据
     */
    public static String toAsciiHexString(String str) {
        if (str == null || str.isEmpty()) return "";
        int length = str.length() + str.length() % 2;
        String paddedStr = String.format("%1$-" + length + "s", str);
        StringBuilder hexString = new StringBuilder();
        for (int i = 0; i < paddedStr.length() && i < length; i++) {
            char c = paddedStr.charAt(i);
            int ascii = (int) c;
            String hex = Integer.toHexString(ascii);
            if (hex.length() == 1) {
                hexString.append("0");
            }
            hexString.append(hex);
        }
        return hexString.toString().toUpperCase();
    }


    // addSpaces为false则不自动补空格
    public static String toAsciiHexString(String str, boolean addSpaces) {
        if (str == null) return "";
        if(addSpaces) return toAsciiHexString(str);

        StringBuilder hexString = new StringBuilder();
        for (int i = 0; i < str.length(); i++) {
            char c = str.charAt(i);
            int ascii = (int) c;
            String hex = Integer.toHexString(ascii);
            if (hex.length() == 1) {
                hexString.append("0");
            }
            hexString.append(hex);
        }

        return hexString.toString().toUpperCase();
    }

    /**
     * 将ASCII 16进制字符串转换为原始数据字符串
     *
     * @param hexString ASCII 16进制字符串，每个ASCII码用2个16进制数表示，中间可能包含空格
     * @return 原始数据字符串
     */
    public static String fromAsciiHexString(String hexString) {
        return fromAsciiHexString(hexString, false);
    }

    /**
     * 将ASCII 16进制字符串转换为原始数据字符串
     *
     * @param hexString ASCII 16进制字符串，每个ASCII码用2个16进制数表示，中间可能包含空格
     * @param isFilter 是否过滤非非数字字母ASCII码，过滤后存在非数字字母ASCII则返回空字符串
     * @return 原始数据字符串
     */
    public static String fromAsciiHexString(String hexString, boolean isFilter) {
        if (hexString == null) return "";
        StringBuilder str = new StringBuilder();
        for (int i = 0; i < hexString.length(); i += 2) {
            String hex = hexString.substring(i, i + 2);
            int ascii = Integer.parseInt(hex, 16);
            if (isFilter && (ascii < 0x20 || ascii > 0x7E)) {  // 过滤乱码
                return "";
            }
            str.append((char) ascii);
        }
        return str.toString().trim();
    }

    /**
     * 将ASCII 16进制字符串转换为原始数据字符串
     *
     * @param hexString ASCII 16进制字符串，每个ASCII码用2个16进制数表示，中间可能包含空格
     * @param isFilter 是否过滤非非数字字母ASCII码，过滤后存在非数字字母ASCII则返回空字符串
     * @param deleteSpace 是否去掉前后空格和换行
     * @return 原始数据字符串
     */
    public static String fromAsciiHexString(String hexString, boolean isFilter, boolean deleteSpace) {
        if (hexString == null) return "";
        StringBuilder str = new StringBuilder();
        for (int i = 0; i < hexString.length(); i += 2) {
            String hex = hexString.substring(i, i + 2);
            int ascii = Integer.parseInt(hex, 16);
            if (isFilter && (ascii < 0x20 || ascii > 0x7E)) {  // 过滤乱码
                return "";
            }
            str.append((char) ascii);
        }
        String res = str.toString();
        if (deleteSpace)    res = res.trim();
        return res;
    }

    public static String byteArrayTolongString(byte[] var0) {
        if (var0 == null) return "";
        byte[] var1 = new byte[8];
        int var2 = 7;

        for (int var3 = var0.length - 1; var2 >= 0; --var3) {
            if (var3 >= 0) {
                var1[var2] = var0[var3];
            } else {
                var1[var2] = 0;
            }
            --var2;
        }

        long var10000 = (long) (var1[0] & 255) << 56;
        long var15 = (long) (var1[1] & 255) << 48;
        long var16 = (long) (var1[2] & 255) << 40;
        long var5 = (long) (var1[3] & 255) << 32;
        long var7 = (long) (var1[4] & 255) << 24;
        long var9 = (long) (var1[5] & 255) << 16;
        long var11 = (long) (var1[6] & 255) << 8;
        long var13 = (long) (var1[7] & 255);
        long res = var10000 | var15 | var16 | var5 | var7 | var9 | var11 | var13;
        return res == 0L ? "" : String.valueOf(res);
    }

}
