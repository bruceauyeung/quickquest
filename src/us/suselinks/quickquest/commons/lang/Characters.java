package us.suselinks.quickquest.commons.lang;

import org.apache.commons.lang3.CharUtils;

/**
 * notes: all of {@linkplain Character#TITLECASE_LETTER} are LATIN CAPITAL
 * LETTERS. please refer to <a
 * href="http://www.fileformat.info/info/unicode/category/Lt/list.htm"
 * >http://www.fileformat.info/info/unicode/category/Lt/list.htm</a>
 * 
 * @author <a href="mailto:bruce.oy@gmail.com">bruce.oy@gmail.com</a>
 * 
 */
public class Characters {
	public static boolean isGarbledChar(char c) {

		Character.UnicodeBlock ub = Character.UnicodeBlock.of(c);

		if (ub == Character.UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS

		|| ub == Character.UnicodeBlock.CJK_COMPATIBILITY_IDEOGRAPHS

		|| ub == Character.UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS_EXTENSION_A

		|| ub == Character.UnicodeBlock.GENERAL_PUNCTUATION

		|| ub == Character.UnicodeBlock.CJK_SYMBOLS_AND_PUNCTUATION

				|| ub == Character.UnicodeBlock.HALFWIDTH_AND_FULLWIDTH_FORMS

				// these two methods are provided since jdk1.7, so i commented
				// these lines out for jdk1.6 compatibility
				//|| Character.isAlphabetic(c) || Character.isIdeographic(c)
				|| Character.isDigit(c) || Character.isSpaceChar(c)
				|| CharUtils.isAsciiPrintable(c)) {

			return false;

		}

		return true;

	}
}
