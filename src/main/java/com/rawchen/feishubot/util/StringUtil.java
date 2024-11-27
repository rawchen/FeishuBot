package com.rawchen.feishubot.util;

import cn.hutool.core.util.StrUtil;
import lombok.extern.slf4j.Slf4j;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author RawChen
 * @date 2023-08-04
 */
@Slf4j
public class StringUtil {
	public static String replaceSpecialSymbol(String str) {

		if (StrUtil.isEmpty(str)) {
			return "";
		}

		Pattern pattern = Pattern.compile("!\\[([^\\]]*)]\\(([^\\)]*)\\)");
		Matcher matcher = pattern.matcher(str);
		while (matcher.find()) {
//			str = str.replaceAll("!\\[([^\\]]*)]\\(([^\\)]*)\\)", "```![Image](http://sample.com/a.jpg)```");
			str = str.replaceAll("!\\[([^\\]]*)]\\(([^\\)]*)\\)", "![Image](img_v3_02h1_6e6d0296-1c9a-4058-9a2c-a167e2cbd56g)");
//			String url = matcher.group(2);
//			String description = matcher.group(1);
//			System.out.println("URL: " + url);
//			System.out.println("Description: " + description);
		}

//		str = str.replace(">", "&#62;");
//		str = str.replace("<", "&#60;");
//		str = str.replace("~", "&sim;");
//		str = str.replace("-", "&#45;");
//		str = str.replace("!", "&#33;");
//		str = str.replace("*", "&#42;");
//		str = str.replace("/", "&#47;");
//		str = str.replace("\\", "&#92;");
//		str = str.replace("[", "&#91;");
//		str = str.replace("]", "&#93;");
//		str = str.replace("(", "&#40;");
//		str = str.replace(")", "&#41;");
//		str = str.replace("#", "&#35;");
//		str = str.replace(":", "&#58;");
//		str = str.replace("+", "&#43;");
//		str = str.replace("\"", "&#34;");
//		str = str.replace("'", "&#39;");
//		str = str.replace("`", "&#96;");
//		str = str.replace("$", "&#36;");
//		str = str.replace("_", "&#95;");
//		str = str.replace("-", "&#45;");

		return str;
	}

	public static String escape(String input) {
		StringBuilder sb = new StringBuilder();

		for (int i = 0; i < input.length(); i++) {
			char c = input.charAt(i);

			switch (c) {
				case '\n':
					sb.append("\\n");
					break;
				case '\t':
					sb.append("\\t");
					break;
				case '\r':
					sb.append("\\r");
					break;
				case '\f':
					sb.append("\\f");
					break;
				case '\b':
					sb.append("\\b");
					break;
				case '\\':
					sb.append("\\\\");
					break;
				case '\'':
					sb.append("\\'");
					break;
				case '\"':
					sb.append("\\\"");
					break;
				default:
					sb.append(c);
			}
		}

		return sb.toString();
	}
}
