package com.mx.privilege.util;

/**
 * @author mengxu
 * @date 2022/4/7 0:03
 */
public class StringUtil {

    public static String firstToUpper(String fieldName) {
        // 进行字母的ascii编码前移，效率要高于截取字符串进行转换的操作
        char[] cs = fieldName.toCharArray();
        cs[0] -= 32;
        return String.valueOf(cs);
    }

}
