package com.eBrother.util;


import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Pattern;

public class StringUtil {
	public static boolean isNumeric(String str) {
		return _isValid(str, "[0-9]+");
	}
	private static boolean _isValid(String str, String regex) {
		if (null == str) return false;
		// regex
		// 숫자 : [0-9]+
		// 소문자와 숫자 : [a-z0-9]+
		// 영문과 숫자 : [a-zA-Z0-9]+
		return Pattern.matches(regex, str);
	}

	public static String toCsvFormatLine(String str, String delim) {
//		String[] cols = str.split(delim);
		String[] cols = explode(str, delim, true);
		return toCsvFormatByStringArr(cols);
	}

	static public int getIntNumber(String str) throws Exception {
			
		return Integer.parseInt ( str );
		
	}

	
	public static String[] explode(String str, String delim, boolean isTrim) {
		int delimLength = delim.length();

		List<String> list = new LinkedList<String>();
		int sidx = 0;
		int eidx = 0;
		while (true) {
			eidx = str.indexOf(delim, sidx);
			String col = _substring(str, sidx, eidx, isTrim);
			list.add(col);

			if (-1 == eidx) break;

			sidx = eidx + delimLength;
		}

		return list.toArray(new String[list.size()]);
	}

	private static String _substring(String str, int sidx, int eidx, boolean isTrim) {
		String retval;
		if (eidx < 0) retval = str.substring(sidx);
		else retval = str.substring(sidx, eidx);

		if (isTrim) return retval.trim();
		else return retval;
	}

	private static String _escaping(String str) {
		if (null == str) return "";

		return str
			.replaceAll("\r", "")
			.replaceAll("\n", " ")
			.replaceAll("\\\\", "\\\\\\\\")
			.replaceAll("\"", "\\\\\"");
	}

	public static String toCsvFormatByStringArr(String[] strarr) {
		StringBuilder strbuf = new StringBuilder();
		int colLen = 0;
		for (String col : strarr) {
			if (null != col) colLen =  col.length();
			strbuf.append(',');
			if (null == col) {
				strbuf.append("");
			} else if (0 == col.indexOf('"') && (colLen - 1) == col.lastIndexOf('"')) { // 앞뒤로 "가 있으면...
				String newstr = col.substring(1, colLen - 1); // 앞뒤 "를 제외한 문자열
				if (0 == newstr.length()) {
					strbuf.append("");
				} else {
					strbuf
						.append('"')
						.append(_escaping(newstr))
						.append('"')
					;
				}
			} else if (StringUtil.isNumeric(col)) {
				strbuf.append(col);
			} else {
				strbuf
					.append('"')
					.append(_escaping(col))
					.append('"')
				;
			}
		}
		return strbuf.substring(1);
	}

	public static String changeFileExt(String fileName, String newExt) {
		int idx = fileName.lastIndexOf('.');
		if (0 > idx) return fileName;

		return String.format("%s.%s", fileName.substring(0, idx), newExt);
	}

	public static String limitLengthString(String str, int len) {
		if (null == str) return "";

		if (len > str.length()) return str;

		return str.substring(0, len);
	}

	public static String toStringExceptionStackTrace(Throwable ex) {
		if (null == ex) return "";

		StringWriter errors = new StringWriter();
        ex.printStackTrace(new PrintWriter(errors));

        return errors.toString();
	}
}

