package com.rawchen.feishubot.util;

/**
 * @author RawChen
 * @date 2023-08-04
 */
public class StringUtil {
	public static String replaceSpecialSymbol(String str) {
		str = str.replaceAll(">", 		"&#62;");
		str = str.replaceAll("<", 		"&#60;");
		str = str.replaceAll("~", 		"&sim;");
		str = str.replaceAll("-", 		"&#45;");
		str = str.replaceAll("!", 		"&#33;");
		str = str.replaceAll("\\*", 	"&#42;");
		str = str.replaceAll("/", 		"&#47;");
		str = str.replaceAll("\\\\",	"&#92;");
		str = str.replaceAll("\\[", 	"&#91;");
		str = str.replaceAll("]", 		"&#93;");
		str = str.replaceAll("\\(", 	"&#40;");
		str = str.replaceAll("\\)", 	"&#41;");
		str = str.replaceAll("#", 		"&#35;");
		str = str.replaceAll(":", 		"&#58;");
		str = str.replaceAll("\\+", 	"&#43;");
		str = str.replaceAll("\"", 		"&#34;");
		str = str.replaceAll("'", 		"&#39;");
		str = str.replaceAll("`", 		"&#96;");
		str = str.replaceAll("\\$", 	"&#36;");
		str = str.replaceAll("_", 		"&#95;");
		return str;
	}
}
