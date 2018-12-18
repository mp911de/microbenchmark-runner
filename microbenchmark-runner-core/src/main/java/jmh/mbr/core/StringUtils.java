/*
 * Copyright 2018 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v20.html
 */
package jmh.mbr.core;

import java.util.Collection;
import java.util.Iterator;

/**
 * Miscellaneous {@link String} utility methods.
 */
public class StringUtils {

	/**
	 * Check whether the given {@link String} is empty.
	 *
	 * @param theString the candidate String
	 * @return {@literal true} if the string is empty.
	 */
	public static boolean isEmpty(Object theString) {
		return (theString == null || "".equals(theString));
	}

	/**
	 * Check whether the given {@link String} contains actual text.
	 *
	 * @param theString the {@link String} to check (may be {@code null})
	 * @return {@code true} if the {@link String} is not {@code null}, its length is greater than 0, and it does not
	 * contain whitespace only
	 */
	public static boolean hasText(String theString) {
		return (theString != null && !theString.isEmpty() && containsText(theString));
	}

	private static boolean containsText(CharSequence str) {

		int length = str.length();

		for (int i = 0; i < length; i++) {
			if (!Character.isWhitespace(str.charAt(i))) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Convert a {@link Collection} into a delimited {@link String} (e.g. CSV).
	 * <p>
	 * Useful for {@link #toString()} implementations.
	 *
	 * @param c the {@code Collection} to convert (potentially {@code null} or empty).
	 * @param delim the delimiter to use (typically a ",")
	 * @return the delimited {@link String}
	 */
	public static String collectionToDelimitedString(Collection<?> c, String delim) {

		if (c.isEmpty()) {
			return "";
		}

		StringBuilder sb = new StringBuilder();
		Iterator<?> it = c.iterator();
		while (it.hasNext()) {
			sb.append(it.next());
			if (it.hasNext()) {
				sb.append(delim);
			}
		}
		return sb.toString();
	}
}
