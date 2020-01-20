/*
 * Copyright 2020 the original author or authors.
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
import java.util.stream.Collectors;

/**
 * {@link BenchmarkConfiguration} based on the {@link Environment}.
 *
 * @see Environment
 */
enum EnvironmentBenchmarkConfiguration implements BenchmarkConfiguration {

	INSTANCE;

	@Override
	public String getMode() {
		return Environment.getPropertyOrDefault(BenchmarkConfigProperties.MODE);
	}

	/**
	 * Read {@code benchmarksEnabled} property from {@link Environment}.
	 *
	 * @return true if not set.
	 */
	public boolean isEnabled() {
		return Environment.getPropertyOrDefault(BenchmarkConfigProperties.ENABLED);
	}

	/**
	 * Read {@code warmupIterations} property from {@link Environment}.
	 *
	 * @return -1 if not set.
	 */
	public int getWarmupIterations() {
		return Environment
				.getPropertyOrDefault(BenchmarkConfigProperties.WARMUP_ITERATIONS);
	}

	@Override
	public int getWarmupBatchSize() {
		return Environment
				.getPropertyOrDefault(BenchmarkConfigProperties.WARMUP_BATCH_SIZE);
	}

	@Override
	public String getWarmupMode() {
		return Environment
				.getPropertyOrDefault(BenchmarkConfigProperties.WARMUP_MODE);
	}

	/**
	 * Read {@code measurementIterations} property from {@link Environment}.
	 *
	 * @return -1 if not set.
	 */
	public int getMeasurementIterations() {
		return Environment
				.getPropertyOrDefault(BenchmarkConfigProperties.MEASUREMENT_ITERATIONS);
	}

	@Override
	public int getMeasurementBatchSize() {
		return Environment
				.getPropertyOrDefault(BenchmarkConfigProperties.MEASUREMENT_ITERATIONS);
	}

	@Override
	public Duration getTimeout() {
		return Environment.getPropertyOrDefault(BenchmarkConfigProperties.TIMEOUT);
	}

	/**
	 * Read {@code forks} property from {@link Environment}.
	 *
	 * @return -1 if not set.
	 */
	public int getForksCount() {
		return Environment.getPropertyOrDefault(BenchmarkConfigProperties.FORKS);
	}

	/**
	 * Read {@code benchmarkReportDir} property from {@link Environment}.
	 *
	 * @return {@literal null} if not set.
	 */
	public String getReportDirectory() {
		return Environment
				.getPropertyOrDefault(BenchmarkConfigProperties.BENCHMARK_REPORT_DIR);
	}

	/**
	 * Read {@code measurementTime} property from {@link Environment}.
	 *
	 * @return {@link Duration#ZERO} if not set.
	 */
	public Duration getMeasurementTime() {
		return Environment
				.getPropertyOrDefault(BenchmarkConfigProperties.MEASUREMENT_TIME);
	}

	/**
	 * Read {@code warmupTime} property from {@link Environment}.
	 *
	 * @return {@link Duration#ZERO} if not set.
	 */
	public Duration getWarmupTime() {
		return Environment
				.getPropertyOrDefault(BenchmarkConfigProperties.WARMUP_TIME);
	}

	@Override
	public String publishUri() {
		return Environment
				.getPropertyOrDefault(BenchmarkConfigProperties.PUBLISH_URI);
	}

	@Override
	public Map<String, Object> asMap() {

		return Environment.jmhConfigProperties().entrySet()
				.stream()
				.collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
	}
}
