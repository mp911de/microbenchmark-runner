/*
 * Copyright 2018 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package jmh.mbr.core;

import java.util.Collection;
import java.util.Iterator;

/**
 * Miscellaneous {@link String} utility methods.
 * <p>
 * Mainly for internal use within the framework
 * 
 * @author Mark Paluch
 * @see org.springframework.util.StringUtils
 */
public class StringUtils {

	// ---------------------------------------------------------------------
	// General convenience methods for working with Strings
	// ---------------------------------------------------------------------

	/**
	 * Check whether the given {@code String} is empty.
	 * <p>
	 * This method accepts any Object as an argument, comparing it to {@code null} and the empty String. As a consequence,
	 * this method will never return {@code true} for a non-null non-String object.
	 * <p>
	 * The Object signature is useful for general attribute handling code that commonly deals with Strings but generally
	 * has to iterate over Objects since attributes may e.g. be primitive value objects as well.
	 * 
	 * @param str the candidate String
	 */
	public static boolean isEmpty(Object str) {
		return (str == null || "".equals(str));
	}

	/**
	 * Check that the given {@code CharSequence} is neither {@code null} nor of length 0.
	 * <p>
	 * Note: this method returns {@code true} for a {@code CharSequence} that purely consists of whitespace.
	 * <p>
	 * 
	 * <pre class="code">
	 * StringUtils.hasLength(null) = false
	 * StringUtils.hasLength("") = false
	 * StringUtils.hasLength(" ") = true
	 * StringUtils.hasLength("Hello") = true
	 * </pre>
	 * 
	 * @param str the {@code CharSequence} to check (may be {@code null})
	 * @return {@code true} if the {@code CharSequence} is not {@code null} and has length
	 * @see #hasText(String)
	 */
	public static boolean hasLength(CharSequence str) {
		return (str != null && str.length() > 0);
	}

	/**
	 * Check that the given {@code String} is neither {@code null} nor of length 0.
	 * <p>
	 * Note: this method returns {@code true} for a {@code String} that purely consists of whitespace.
	 * 
	 * @param str the {@code String} to check (may be {@code null})
	 * @return {@code true} if the {@code String} is not {@code null} and has length
	 * @see #hasLength(CharSequence)
	 * @see #hasText(String)
	 */
	public static boolean hasLength(String str) {
		return (str != null && !str.isEmpty());
	}

	/**
	 * Check whether the given {@code CharSequence} contains actual <em>text</em>.
	 * <p>
	 * More specifically, this method returns {@code true} if the {@code CharSequence} is not {@code null}, its length is
	 * greater than 0, and it contains at least one non-whitespace character.
	 * <p>
	 * 
	 * <pre class="code">
	 * StringUtils.hasText(null) = false
	 * StringUtils.hasText("") = false
	 * StringUtils.hasText(" ") = false
	 * StringUtils.hasText("12345") = true
	 * StringUtils.hasText(" 12345 ") = true
	 * </pre>
	 * 
	 * @param str the {@code CharSequence} to check (may be {@code null})
	 * @return {@code true} if the {@code CharSequence} is not {@code null}, its length is greater than 0, and it does not
	 *         contain whitespace only
	 * @see Character#isWhitespace
	 */
	public static boolean hasText(CharSequence str) {
		return (str != null && str.length() > 0 && containsText(str));
	}

	/**
	 * Check whether the given {@code String} contains actual <em>text</em>.
	 * <p>
	 * More specifically, this method returns {@code true} if the {@code String} is not {@code null}, its length is
	 * greater than 0, and it contains at least one non-whitespace character.
	 * 
	 * @param str the {@code String} to check (may be {@code null})
	 * @return {@code true} if the {@code String} is not {@code null}, its length is greater than 0, and it does not
	 *         contain whitespace only
	 * @see #hasText(CharSequence)
	 */
	public static boolean hasText(String str) {
		return (str != null && !str.isEmpty() && containsText(str));
	}

	private static boolean containsText(CharSequence str) {
		int strLen = str.length();
		for (int i = 0; i < strLen; i++) {
			if (!Character.isWhitespace(str.charAt(i))) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Convert a {@code Collection} into a delimited {@code String} (e.g. CSV).
	 * <p>
	 * Useful for {@code toString()} implementations.
	 * 
	 * @param coll the {@code Collection} to convert (potentially {@code null} or empty)
	 * @param delim the delimiter to use (typically a ",")
	 * @return the delimited {@code String}
	 */
	public static String collectionToDelimitedString(Collection<?> coll, String delim) {
		return collectionToDelimitedString(coll, delim, "", "");
	}

	/**
	 * Convert a {@code Collection} into a delimited {@code String} (e.g., CSV).
	 * <p>
	 * Useful for {@code toString()} implementations.
	 * 
	 * @param coll the {@code Collection} to convert (potentially {@code null} or empty)
	 * @return the delimited {@code String}
	 */
	public static String collectionToCommaDelimitedString(Collection<?> coll) {
		return collectionToDelimitedString(coll, ",");
	}

	/**
	 * Convert a {@link Collection} to a delimited {@code String} (e.g. CSV).
	 * <p>
	 * Useful for {@code toString()} implementations.
	 * 
	 * @param coll the {@code Collection} to convert (potentially {@code null} or empty)
	 * @param delim the delimiter to use (typically a ",")
	 * @param prefix the {@code String} to start each element with
	 * @param suffix the {@code String} to end each element with
	 * @return the delimited {@code String}
	 */
	public static String collectionToDelimitedString(Collection<?> coll, String delim, String prefix, String suffix) {

		if (coll.isEmpty()) {
			return "";
		}

		StringBuilder sb = new StringBuilder();
		Iterator<?> it = coll.iterator();
		while (it.hasNext()) {
			sb.append(prefix).append(it.next()).append(suffix);
			if (it.hasNext()) {
				sb.append(delim);
			}
		}
		return sb.toString();
	}
}
