package com.rawchen.feishubot.util;

/**
 * @author RawChen
 * @date 2023-08-04
 */
public class StringUtil {
	public static String replaceSpecialSymbol(String str) {
		str = str.replaceAll("<", "＜");
		str = str.replaceAll(">", "＞");
		return str;
	}
}
