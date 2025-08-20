/*
 * Copyright 2018-2019 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v20.html
 */
package jmh.mbr.junit5.execution;

import java.util.Collections;
import java.util.Optional;
import java.util.Set;

import jmh.mbr.core.model.BenchmarkClass;
import jmh.mbr.junit5.config.DefaultMbrConfiguration;
import jmh.mbr.junit5.config.MbrConfiguration;
import jmh.mbr.junit5.descriptor.BenchmarkClassDescriptor;
import jmh.mbr.junit5.execution.JmhRunnerUnitTests.EmptyOutputDirectoryProvider;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ConditionEvaluationResult;
import org.junit.jupiter.api.extension.ExecutionCondition;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.engine.extension.ExtensionRegistry;
import org.junit.jupiter.engine.extension.MutableExtensionRegistry;
import org.junit.platform.engine.ConfigurationParameters;
import org.junit.platform.engine.UniqueId;
import org.junit.platform.engine.support.store.NamespacedHierarchicalStore;
import org.openjdk.jmh.annotations.Benchmark;

import static org.assertj.core.api.Assertions.*;

/**
 * Unit tests for {@link ConditionEvaluator}.
 */
public class ConditionEvaluatorUnitTests {

	MbrConfiguration configuration = new DefaultMbrConfiguration(JmhRunnerUnitTests.EmptyConfigurationParameters.INSTANCE, EmptyOutputDirectoryProvider.INSTANCE, new NamespacedHierarchicalStore(null).newChild());

	@Test
	void shouldRunWithoutCondition() {

		ExtensionRegistry registry = MutableExtensionRegistry
				.createRegistryWithDefaultExtensions(configuration);

		BenchmarkClassDescriptor descriptor = createDescriptor(SimpleBenchmarkClass.class);

		ConditionEvaluator evaluator = new ConditionEvaluator();
		ConditionEvaluationResult result = evaluator.evaluate(registry, descriptor
				.getExtensionContext(null, null, configuration));

		assertThat(result.isDisabled()).isFalse();
	}

	@Test
	void shouldSkipDisabledBenchmarkClass() {

		ExtensionRegistry registry = MutableExtensionRegistry
				.createRegistryWithDefaultExtensions(configuration);

		BenchmarkClassDescriptor descriptor = createDescriptor(DisabledBenchmark.class);

		ConditionEvaluator evaluator = new ConditionEvaluator();
		ConditionEvaluationResult result = evaluator.evaluate(registry, descriptor
				.getExtensionContext(null, null, configuration));

		assertThat(result.isDisabled()).isTrue();
	}

	@Test
	void shouldSkipDisabledThroughExtensionClass() {

		MutableExtensionRegistry parentRegistry = MutableExtensionRegistry
				.createRegistryWithDefaultExtensions(configuration);

		BenchmarkClassDescriptor descriptor = createDescriptor(CustomExtensionBenchmark.class);
		ExtensionRegistry registry = descriptor.getExtensionRegistry(parentRegistry);

		ConditionEvaluator evaluator = new ConditionEvaluator();
		ConditionEvaluationResult result = evaluator.evaluate(registry, descriptor
				.getExtensionContext(null, null, configuration));

		assertThat(result.isDisabled()).isTrue();
		assertThat(result.getReason()).contains("always disabled");
	}

	private BenchmarkClassDescriptor createDescriptor(Class<?> javaClass) {
		BenchmarkClass benchmarkClass = BenchmarkClass
				.create(javaClass, Collections.emptyList());
		return new BenchmarkClassDescriptor(UniqueId
				.root("root", "root"), benchmarkClass);
	}


	public static class SimpleBenchmarkClass {

		@Benchmark
		public void justOne() {
		}
	}

	@Disabled
	public static class DisabledBenchmark {

		@Benchmark
		public void justOne() {
		}
	}

	@ExtendWith(NeverRunExtension.class)
	public static class CustomExtensionBenchmark {

		@Benchmark
		public void justOne() {
		}
	}

	enum EmptyConfigurationParameters implements ConfigurationParameters {
		INSTANCE;

		@Override
		public Optional<String> get(String key) {
			return Optional.empty();
		}

		@Override
		public Optional<Boolean> getBoolean(String key) {
			return Optional.empty();
		}


		@Override
		public int size() {
			return 0;
		}

		@Override
		public Set<String> keySet() {
			return Collections.emptySet();
		}
	}

	static class NeverRunExtension implements ExecutionCondition {

		@Override
		public ConditionEvaluationResult evaluateExecutionCondition(ExtensionContext context) {
			return ConditionEvaluationResult.disabled("always disabled");
		}
	}
}
