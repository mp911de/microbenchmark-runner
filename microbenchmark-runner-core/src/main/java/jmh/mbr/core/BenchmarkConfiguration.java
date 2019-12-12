/*
 * Copyright 2019 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * https://www.eclipse.org/legal/epl-v20.html
 */
package jmh.mbr.core;

import java.text.SimpleDateFormat;
import java.time.Duration;
import java.util.Date;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author Christoph Strobl
 */
public interface BenchmarkConfiguration {

	/**
	 * Read {@code benchmarksEnabled} property from {@link jmh.mbr.core.Environment}.
	 *
	 * @return true if not set.
	 */
	default boolean isEnabled() {
		return true;
	}

	/**
	 * @return
	 */
	String getMode();

	static BenchmarkConfiguration defaultOptions() {
		return new EnvironmentBenchmarkConfiguration();
	}

	default String publishUri() {
		return null;
	}

	/**
	 * Read {@code warmupIterations} property from {@link jmh.mbr.core.Environment}.
	 *
	 * @return -1 if not set.
	 */
	int getWarmupIterations();

	int getWarmupBatchSize();

	String getWarmupMode();

	/**
	 * Read {@code measurementIterations} property from {@link jmh.mbr.core.Environment}.
	 *
	 * @return -1 if not set.
	 */
	int getMeasurementIterations();

	int getMeasurementBatchSize();

	Duration getTimeout();

	/**
	 * Read {@code forks} property from {@link jmh.mbr.core.Environment}.
	 *
	 * @return -1 if not set.
	 */
	int getForksCount();

	/**
	 * Read {@code benchmarkReportDir} property from {@link jmh.mbr.core.Environment}.
	 *
	 * @return {@literal null} if not set.
	 */
	default String getReportDirectory() {
		return null;
	}

	/**
	 * Read {@code measurementTime} property from {@link jmh.mbr.core.Environment}.
	 *
	 * @return {@link Duration#ZERO} if not set.
	 */
	Duration getMeasurementTime();

	/**
	 * Read {@code warmupTime} property from {@link jmh.mbr.core.Environment}.
	 *
	 * @return {@link Duration#ZERO} if not set.
	 */
	Duration getWarmupTime();

	/**
	 * Returns the report file name for {@link Class class under benchmark}.
	 *
	 * @param jmhTestClass class under benchmark.
	 * @return the report file name such as {@code project.version_yyyy-MM-dd_ClassName.json} eg.
	 * {@literal 1.11.0.BUILD-SNAPSHOT_2017-03-07_MappingMongoConverterBenchmark.json}
	 */
	default String reportFilename(Class<?> jmhTestClass) {

		StringBuilder sb = new StringBuilder();
		sb.append(new SimpleDateFormat("yyyy-MM-dd").format(new Date()));
		sb.append("_");
		sb.append(jmhTestClass.getSimpleName());
		sb.append(".json");
		return sb.toString();
	}

	Map<String, Object> asMap();

	class EnvironmentBenchmarkConfiguration implements BenchmarkConfiguration {

		@Override
		public String getMode() {
			return Environment.getPropertyOrDefault(BenchmarkConfigProperties.MODE);
		}

		/**
		 * Read {@code benchmarksEnabled} property from {@link jmh.mbr.core.Environment}.
		 *
		 * @return true if not set.
		 */
		public boolean isEnabled() {
			return Environment.getPropertyOrDefault(BenchmarkConfigProperties.ENABLED);
		}

		/**
		 * Read {@code warmupIterations} property from {@link jmh.mbr.core.Environment}.
		 *
		 * @return -1 if not set.
		 */
		public int getWarmupIterations() {
			return Environment.getPropertyOrDefault(BenchmarkConfigProperties.WARMUP_ITERATIONS);
		}

		@Override
		public int getWarmupBatchSize() {
			return Environment.getPropertyOrDefault(BenchmarkConfigProperties.WARMUP_BATCH_SIZE);
		}

		@Override
		public String getWarmupMode() {
			return Environment.getPropertyOrDefault(BenchmarkConfigProperties.WARMUP_MODE);
		}

		/**
		 * Read {@code measurementIterations} property from {@link jmh.mbr.core.Environment}.
		 *
		 * @return -1 if not set.
		 */
		public int getMeasurementIterations() {
			return Environment.getPropertyOrDefault(BenchmarkConfigProperties.MEASUREMENT_ITERATIONS);
		}

		@Override
		public int getMeasurementBatchSize() {
			return Environment.getPropertyOrDefault(BenchmarkConfigProperties.MEASUREMENT_ITERATIONS);
		}

		@Override
		public Duration getTimeout() {
			return Environment.getPropertyOrDefault(BenchmarkConfigProperties.TIMEOUT);
		}

		/**
		 * Read {@code forks} property from {@link jmh.mbr.core.Environment}.
		 *
		 * @return -1 if not set.
		 */
		public int getForksCount() {
			return Environment.getPropertyOrDefault(BenchmarkConfigProperties.FORKS);
		}

		/**
		 * Read {@code benchmarkReportDir} property from {@link jmh.mbr.core.Environment}.
		 *
		 * @return {@literal null} if not set.
		 */
		public String getReportDirectory() {
			return Environment.getPropertyOrDefault(BenchmarkConfigProperties.BENCHMARK_REPORT_DIR);
		}

		/**
		 * Read {@code measurementTime} property from {@link jmh.mbr.core.Environment}.
		 *
		 * @return {@link Duration#ZERO} if not set.
		 */
		public Duration getMeasurementTime() {
			return Environment.getPropertyOrDefault(BenchmarkConfigProperties.MEASUREMENT_TIME);
		}

		/**
		 * Read {@code warmupTime} property from {@link jmh.mbr.core.Environment}.
		 *
		 * @return {@link Duration#ZERO} if not set.
		 */
		public Duration getWarmupTime() {
			return Environment.getPropertyOrDefault(BenchmarkConfigProperties.WARMUP_TIME);
		}

		@Override
		public String publishUri() {
			return Environment.getPropertyOrDefault(BenchmarkConfigProperties.PUBLISH_URI);
		}

		@Override
		public Map<String, Object> asMap() {

			return Environment.jmhConfigProperties().entrySet()
					.stream()
					.collect(Collectors.toMap(e -> e.getKey().toString(), e -> e.getValue()));
		}

		/**
		 * Returns the report file name for {@link Class class under benchmark}.
		 *
		 * @param jmhTestClass class under benchmark.
		 * @return the report file name such as {@code project.version_yyyy-MM-dd_ClassName.json} eg.
		 * {@literal 1.11.0.BUILD-SNAPSHOT_2017-03-07_MappingMongoConverterBenchmark.json}
		 */
		public String reportFilename(Class<?> jmhTestClass) {

			StringBuilder sb = new StringBuilder();

			if (Environment.containsProperty("project.version")) {

				sb.append(Environment.getProperty("project.version"));
				sb.append("_");
			}

			sb.append(new SimpleDateFormat("yyyy-MM-dd").format(new Date()));
			sb.append("_");
			sb.append(jmhTestClass.getSimpleName());
			sb.append(".json");
			return sb.toString();
		}
	}
}
