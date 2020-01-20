/*
 * Copyright 2019-2020 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * https://www.eclipse.org/legal/epl-v20.html
 */
package jmh.mbr.junit5.execution;

import java.time.Duration;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;

import jmh.mbr.core.BenchmarkConfigProperties;
import jmh.mbr.core.BenchmarkConfigProperties.ConfigProperty;
import jmh.mbr.core.BenchmarkConfiguration;
import jmh.mbr.core.Environment;
import org.junit.platform.engine.ConfigurationParameters;

/**
 * {@link BenchmarkConfiguration} obtained from {@link ConfigurationParameters}.
 */
class ConfigurationParameterBenchmarkConfiguration implements BenchmarkConfiguration {

	private final ConfigurationParameters configurationParameters;

	public ConfigurationParameterBenchmarkConfiguration(ConfigurationParameters configurationParameters) {
		this.configurationParameters = configurationParameters;
	}

	@Override
	public boolean isEnabled() {
		return getConfigParameterOrDefault(BenchmarkConfigProperties.ENABLED, Boolean::parseBoolean);
	}

	@Override
	public int getWarmupIterations() {
		return getConfigParameterOrDefault(BenchmarkConfigProperties.WARMUP_ITERATIONS, Integer::parseInt);
	}

	@Override
	public Duration getWarmupTime() {
		return getConfigParameterOrDefault(BenchmarkConfigProperties.WARMUP_TIME, it -> Duration
				.ofSeconds(Long.parseLong(it)));
	}

	@Override
	public Map<String, Object> asMap() {

		Map<String, Object> properties = Environment.jmhConfigProperties();
		BenchmarkConfigProperties.iterator().forEachRemaining(it -> {

			Object value = getConfigParameterOrDefault((ConfigProperty<Object>) it, t -> t);
			if (value != null) {
				properties.put(it.propertyName(), value);
			}
		});
		return properties;
	}

	@Override
	public int getWarmupBatchSize() {
		return getConfigParameterOrDefault(BenchmarkConfigProperties.WARMUP_BATCH_SIZE, Integer::parseInt);
	}

	@Override
	public String getWarmupMode() {
		return getConfigParameterOrDefault(BenchmarkConfigProperties.WARMUP_MODE, it -> it);
	}

	@Override
	public int getMeasurementIterations() {
		return getConfigParameterOrDefault(BenchmarkConfigProperties.MEASUREMENT_ITERATIONS, Integer::parseInt);
	}

	@Override
	public int getMeasurementBatchSize() {
		return getConfigParameterOrDefault(BenchmarkConfigProperties.MEASUREMENT_BATCH_SIZE, Integer::parseInt);
	}

	@Override
	public Duration getMeasurementTime() {
		return getConfigParameterOrDefault(BenchmarkConfigProperties.MEASUREMENT_TIME, it -> Duration
				.ofSeconds(Long.parseLong(it)));
	}

	@Override
	public String getMode() {
		return getConfigParameterOrDefault(BenchmarkConfigProperties.MODE, it -> it);
	}

	@Override
	public Duration getTimeout() {
		return getConfigParameterOrDefault(BenchmarkConfigProperties.TIMEOUT, it -> Duration
				.ofSeconds(Long.parseLong(it)));
	}

	@Override
	public int getForksCount() {
		return getConfigParameterOrDefault(BenchmarkConfigProperties.FORKS, Integer::parseInt);
	}

	@Override
	public String publishUri() {
		return getConfigParameterOrDefault(BenchmarkConfigProperties.PUBLISH_URI, it -> it);
	}

	private <T> T getConfigParameterOrDefault(ConfigProperty<T> property, Function<String, T> mapFunction) {

		for (String propertyName : property.propertyNames()) {

			Optional<String> configValue = configurationParameters.get(propertyName);
			if (configValue.isPresent()) {
				return configValue.map(mapFunction).get();
			}
		}

		return property.defaultValue();
	}
}
