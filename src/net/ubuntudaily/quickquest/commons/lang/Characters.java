package net.ubuntudaily.quickquest.commons.lang;

import org.apache.commons.lang3.CharUtils;

public class Characters {
	public static boolean isGarbledChar(char c) {

		Character.UnicodeBlock ub = Character.UnicodeBlock.of(c);

		if (ub == Character.UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS

		|| ub == Character.UnicodeBlock.CJK_COMPATIBILITY_IDEOGRAPHS

		|| ub == Character.UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS_EXTENSION_A

		|| ub == Character.UnicodeBlock.GENERAL_PUNCTUATION

		|| ub == Character.UnicodeBlock.CJK_SYMBOLS_AND_PUNCTUATION

		|| ub == Character.UnicodeBlock.HALFWIDTH_AND_FULLWIDTH_FORMS
				|| Character.isAlphabetic(c) || Character.isDigit(c)
				|| Character.isSpaceChar(c) || CharUtils.isAsciiPrintable(c)
				|| Character.isIdeographic(c)) {

			return false;

		}

		return true;

	}
}
