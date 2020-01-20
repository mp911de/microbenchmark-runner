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
import java.util.Map;

import org.openjdk.jmh.annotations.Mode;

/**
 * Configuration properties to run a JMH benchmark.
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
	 * @return measurement mode, see {@link Mode}.
	 */
	String getMode();

	static BenchmarkConfiguration defaultOptions() {
		return EnvironmentBenchmarkConfiguration.INSTANCE;
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

	/**
	 * @return warmup measurement mode, see {@link Mode}.
	 */
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
	 * Return all properties as {@link Map} using the configuration property name as key.
	 *
	 * @return the configuration property map.
	 */
	Map<String, Object> asMap();
}
