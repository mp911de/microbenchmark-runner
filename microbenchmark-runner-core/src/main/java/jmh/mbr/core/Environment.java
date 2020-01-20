/*
 * Copyright 2018-2020 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v20.html
 */
package jmh.mbr.core;

import java.time.Duration;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Properties;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import jmh.mbr.core.BenchmarkConfigProperties.ConfigProperty;

/**
 * Utility to obtain property values from System properties and environment variables.
 */
public abstract class Environment {

	private static final Predicate<Entry<?, ?>> CONFIG_PROPERTY_FILTER = it -> it.getKey()
			.toString().startsWith(BenchmarkConfigProperties.PREFIX);

	/**
	 * @return the {@literal os.name}.
	 */
	public static String getOsName() {
		return getProperty("os.name", "n/a");
	}

	/**
	 * @return a {@link Map} containing all configuration properties prefixed with {@link BenchmarkConfigProperties#PREFIX}.
	 */
	public static Map<String, Object> jmhConfigProperties() {

		Properties properties = new Properties();
		properties.putAll(filter(System.getenv()));
		properties.putAll(filter(System.getProperties()));

		return properties.entrySet()
				.stream()
				.collect(Collectors.toMap(e -> e.getKey().toString(), e -> e.getValue()));
	}

	/**
	 * Retrieve a property value by its {@code propertyName}. Attempts to read the property from system properties first
	 * and then (if not set through system properties), from the environment variables.
	 *
	 * @param propertyName must not be {@literal null}.
	 * @return the value or {@literal null} if not set.
	 */
	public static String getProperty(String propertyName) {

		Objects.requireNonNull(propertyName, "PropertyName must not be null!");

		return obtainPropertyValue(new ConfigProperty<>(null, propertyName));
	}

	/**
	 * Get the value of the given {@link ConfigProperty} or its {@link ConfigProperty#defaultValue() default value}.
	 *
	 * @param configProperty must not be {@literal null}.
	 * @param <T> the properties target type.
	 * @return {@literal null} when not set.
	 * @throws NullPointerException if required {@literal configProperty} argument is {@literal null}.
	 */
	static <T> T getPropertyOrDefault(ConfigProperty<T> configProperty) {

		Objects.requireNonNull(configProperty, "ConfigProperty must not be null!");

		String value = obtainPropertyValue(configProperty);

		if (StringUtils.isEmpty(value)) {
			return configProperty.defaultValue();
		}

		Class<T> targetType = configProperty.getType();

		if (targetType == Boolean.class) {
			return targetType.cast(Boolean.valueOf(value));
		}

		if (Long.class.isAssignableFrom(targetType)) {
			return targetType.cast(Long.parseLong(value));
		}

		if (Duration.class.isAssignableFrom(targetType)) {
			return targetType.cast(Duration.ofSeconds(Long.parseLong(value)));
		}

		return targetType.cast(value);
	}

	private static Map<Object, Object> filter(Map<?, ?> source) {

		return source.entrySet()
				.stream()
				.filter(CONFIG_PROPERTY_FILTER)
				.collect(Collectors.toMap(Entry::getKey, Entry::getValue));
	}

	private static <T> String obtainPropertyValue(ConfigProperty<T> configProperty) {

		for (String propertyName : configProperty.propertyNames()) {

			if (System.getProperties().containsKey(propertyName)) {
				return System.getProperty(propertyName);
			}

			if (System.getenv().containsKey(propertyName)) {
				return System.getenv(propertyName);
			}
		}

		return null;
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
		return obtainPropertyValue(new ConfigProperty<>(defaultValue, propertyName));
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

	private Environment() {
	}
}
