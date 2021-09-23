package com.spring.learn.util;

/**
 *
 * @author zhuquanwen
 * @vesion 1.0
 * @date 2021/9/23 20:41
 * @since jdk1.8
 */
public class StringUtil {
    private StringUtil() {}

    public static String firstLower(String simpleName) {
        return simpleName.substring(0, 1).toLowerCase() +
                simpleName.substring(1);
    }
}
