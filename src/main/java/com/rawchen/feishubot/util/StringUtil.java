package com.rawchen.feishubot.util;

/**
 * @author RawChen
 * @date 2023-08-04
 */
public class StringUtil {
	public static String replaceSpecialSymbol(String str) {
		str = str.replaceAll(">", "&#62;");
		str = str.replaceAll("<", "&#60;");
		str = str.replaceAll("~", "&sim;");
		str = str.replaceAll("-", "&sim;");
		str = str.replaceAll("!", "&sim;");
		str = str.replaceAll("\\*", "&sim;");
		str = str.replaceAll("/", "&sim;");
		str = str.replaceAll("\\\\", "&sim;");
		str = str.replaceAll("\\[", "&sim;");
		str = str.replaceAll("]", "&sim;");
		str = str.replaceAll("\\(", "&sim;");
		str = str.replaceAll("\\)", "&sim;");
		str = str.replaceAll("#", "&sim;");
		str = str.replaceAll(":", "&sim;");
		str = str.replaceAll("\\+", "&sim;");
		str = str.replaceAll("\"", "&sim;");
		str = str.replaceAll("'", "&sim;");
		str = str.replaceAll("`", "&sim;");
		str = str.replaceAll("\\$", "&sim;");
		str = str.replaceAll("_", "&sim;");
		return str;
	}
}
