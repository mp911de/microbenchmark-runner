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

import java.util.Objects;

/**
 * Utility to obtain property values from System properties and environment variables.
 */
public class Environment {

	/**
	 * Retrieve a property value by its {@code propertyName}. Attempts to read the property from system properties first
	 * and then (if not set through system properties), from the environment variables.
	 *
	 * @param propertyName must not be {@literal null}.
	 * @return the value or {@literal null} if not set.
	 */
	public static String getProperty(String propertyName) {

		Objects.requireNonNull(propertyName, "PropertyName must not be null!");

		String value = System.getProperty(propertyName);

		if (StringUtils.isEmpty(value)) {
			return System.getenv(propertyName);
		}
		return value;
	}

	/**
	 * Retrieve a property value by its {@code propertyName}. Attempts to read the property from system properties first
	 * and then (if not set through system properties), from the environment variables. Falls back to {@code defaultValue}
	 * if the property is not set.
	 *
	 * @param propertyName must not be {@literal null}.
	 * @param defaultValue must not be {@literal null}.
	 * @return the value or {@literal null} if not set.
	 */
	public static String getProperty(String propertyName, String defaultValue) {

		Objects.requireNonNull(propertyName, "PropertyName must not be null!");

		String value = getProperty(propertyName);
		if (StringUtils.isEmpty(value)) {
			return defaultValue;
		}

		return value;
	}

	/**
	 * Check whether the property is configured (i.e. the property value is not empty and not {@literal null}).
	 *
	 * @param propertyName must not be {@literal null}.
	 * @return {@literal true} if the environment contains a value for {@code propertyName}.
	 */
	public static boolean containsProperty(String propertyName) {
		return !StringUtils.isEmpty(getProperty(propertyName));
	}
}
