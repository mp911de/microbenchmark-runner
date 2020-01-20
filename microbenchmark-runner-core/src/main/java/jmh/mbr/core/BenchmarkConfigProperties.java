/*
 * Copyright 2019-2020 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * https://www.eclipse.org/legal/epl-v20.html
 */
package jmh.mbr.core;

import java.time.Duration;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

/**
 * Collection of {@link ConfigProperty configuration properties}.
 */
public interface BenchmarkConfigProperties {

	String PREFIX = "jmh.mbr.";

	ConfigProperty<Boolean> ENABLED = new ConfigProperty<>(true, PREFIX + "enabled");

	ConfigProperty<String> PROJECT = new ConfigProperty<>(null, PREFIX + "project");
	ConfigProperty<String> VERSION = new ConfigProperty<>(null, PREFIX + "project.version");
	ConfigProperty<String> PUBLISH_URI = new ConfigProperty<>(null, PREFIX + "report.publishTo");
	ConfigProperty<String> BENCHMARK_REPORT_DIR = new ConfigProperty<>(null, PREFIX + "report.dir");

	ConfigProperty<Integer> WARMUP_ITERATIONS = new ConfigProperty<>(-1, PREFIX + "warmup.iterations", "wi");
	ConfigProperty<Integer> WARMUP_BATCH_SIZE = new ConfigProperty<>(-1, PREFIX + "warmup.batchSize", "wbs");
	ConfigProperty<Duration> WARMUP_TIME = new ConfigProperty<>(Duration.ZERO, PREFIX + "warmup.time", "w");
	ConfigProperty<String> WARMUP_MODE = new ConfigProperty<>(null, PREFIX + "warmup.mode", "wm");

	ConfigProperty<Integer> MEASUREMENT_ITERATIONS = new ConfigProperty<>(-1, PREFIX + "measurement.iterations", "i");
	ConfigProperty<Integer> MEASUREMENT_BATCH_SIZE = new ConfigProperty<>(-1, PREFIX + "measurement.batchSize", "bs");
	ConfigProperty<Duration> MEASUREMENT_TIME = new ConfigProperty<>(Duration.ZERO, PREFIX + "measurement.time", "r");

	ConfigProperty<String> MODE = new ConfigProperty<>(null, PREFIX + "mode", "bm");
	ConfigProperty<Duration> TIMEOUT = new ConfigProperty<>(Duration.ZERO, PREFIX + "timeout", "to");

	ConfigProperty<Integer> FORKS = new ConfigProperty<>(-1, PREFIX + "forks", "f");

	/**
	 * Return a {@link Iterator} over all {@link ConfigProperty properties}.
	 *
	 * @return a {@link List} over all {@link ConfigProperty properties}.
	 */
	static Iterator<ConfigProperty<?>> iterator() {
		return asList().iterator();
	}

	/**
	 * Return a {@link List} of all {@link ConfigProperty properties}.
	 *
	 * @return a {@link List} of all {@link ConfigProperty properties}.
	 */
	static List<ConfigProperty<?>> asList() {
		return Arrays
				.asList(ENABLED, PROJECT, VERSION, PUBLISH_URI, BENCHMARK_REPORT_DIR, WARMUP_ITERATIONS, WARMUP_BATCH_SIZE, WARMUP_TIME, WARMUP_MODE, MEASUREMENT_ITERATIONS, MEASUREMENT_TIME, MEASUREMENT_BATCH_SIZE, MODE, TIMEOUT, FORKS);
	}

	class ConfigProperty<T> {

		private final String[] properties;
		private final T defaultValue;

		ConfigProperty(T defaultValue, String... properties) {

			this.properties = properties;
			this.defaultValue = defaultValue;
		}

		public String propertyName() {
			return propertyNames()[0];
		}

		public String[] propertyNames() {
			return properties;
		}

		public T defaultValue() {
			return defaultValue;
		}

		@SuppressWarnings("unchecked")
		Class<T> getType() {
			return defaultValue != null ? (Class<T>) defaultValue
					.getClass() : (Class<T>) Object.class;
		}
	}
}
