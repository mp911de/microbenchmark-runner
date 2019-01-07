/*
 * Copyright 2018 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v20.html
 */
package jmh.mbr.junit5.execution;

import static org.assertj.core.api.Assertions.*;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.regex.Pattern;

import jmh.mbr.core.model.BenchmarkClass;
import jmh.mbr.core.model.BenchmarkDescriptor;
import jmh.mbr.core.model.BenchmarkDescriptorFactory;
import jmh.mbr.core.model.BenchmarkMethod;
import jmh.mbr.junit5.descriptor.AbstractBenchmarkDescriptor;
import jmh.mbr.junit5.descriptor.BenchmarkClassDescriptor;
import jmh.mbr.junit5.descriptor.BenchmarkMethodDescriptor;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.engine.extension.ExtensionRegistry;
import org.junit.platform.engine.ConfigurationParameters;
import org.junit.platform.engine.EngineExecutionListener;
import org.junit.platform.engine.TestDescriptor;
import org.junit.platform.engine.TestExecutionResult;
import org.junit.platform.engine.UniqueId;
import org.junit.platform.engine.reporting.ReportEntry;
import org.openjdk.jmh.annotations.Benchmark;

/**
 * Unit tests for {@link JmhRunner}.
 */
public class JmhRunnerUnitTests {

	JmhRunner runner = new JmhRunner(EmptyConfigurationParameters.INSTANCE, ExtensionRegistry.createRegistryWithDefaultExtensions(EmptyConfigurationParameters.INSTANCE));

	@Test
	void shouldIncludeUnconditionalBenchmarkClass() {

		List<AbstractBenchmarkDescriptor> descriptors = Collections.singletonList(createDescriptor(SimpleBenchmarkClass.class));

		List<String> includePatterns = runner.evaluateBenchmarksToRun(descriptors, EmptyEngineExecutionListener.INSTANCE);

		assertThat(includePatterns).hasSize(1).contains(Pattern.quote(SimpleBenchmarkClass.class.getName()) + "\\." + Pattern.quote("justOne") + "$");
	}

	@Test
	void shouldIncludeUnconditionalBenchmarkMethod() {

		BenchmarkClassDescriptor descriptor = createDescriptor(SimpleBenchmarkClass.class);
		List<AbstractBenchmarkDescriptor> descriptors = Collections.singletonList((AbstractBenchmarkDescriptor) descriptor.getChildren().iterator().next());

		List<String> includePatterns = runner.evaluateBenchmarksToRun(descriptors, EmptyEngineExecutionListener.INSTANCE);

		assertThat(includePatterns).hasSize(1).contains(Pattern.quote(SimpleBenchmarkClass.class.getName()) + "\\." + Pattern.quote("justOne") + "$");
	}

	@Test
	void shouldIncludeEnabledMethod() {

		List<AbstractBenchmarkDescriptor> descriptors = Collections.singletonList(createDescriptor(ConditionalMethods.class));

		List<String> includePatterns = runner.evaluateBenchmarksToRun(descriptors, EmptyEngineExecutionListener.INSTANCE);

		assertThat(includePatterns).hasSize(1).contains(Pattern.quote(ConditionalMethods.class.getName()) + "\\." + Pattern.quote("enabled") + "$");
	}

	@Test
	void shouldNotContainDisabledBenchmarkClass() {

		List<AbstractBenchmarkDescriptor> descriptors = Collections.singletonList(createDescriptor(DisabledBenchmark.class));

		List<String> includePatterns = runner.evaluateBenchmarksToRun(descriptors, EmptyEngineExecutionListener.INSTANCE);

		assertThat(includePatterns).isEmpty();
	}

	private BenchmarkClassDescriptor createDescriptor(Class<?> javaClass) {

		BenchmarkClass benchmarkClass = BenchmarkDescriptorFactory.create(javaClass).createDescriptor();

		BenchmarkClassDescriptor descriptor = new BenchmarkClassDescriptor(UniqueId.root("root", "root"), benchmarkClass);

		for (BenchmarkDescriptor child : benchmarkClass.getChildren()) {

			if (child instanceof BenchmarkMethod) {
				BenchmarkMethod method = (BenchmarkMethod) child;

				descriptor.addChild(new BenchmarkMethodDescriptor(descriptor.getUniqueId().append("method", (method).getName()), method));
			}
		}

		return descriptor;
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

	public static class ConditionalMethods {

		@Benchmark
		@Disabled
		public void disabled() {
		}

		@Benchmark
		public void enabled() {
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
	}

	enum EmptyEngineExecutionListener implements EngineExecutionListener {

		INSTANCE;

		@Override
		public void dynamicTestRegistered(TestDescriptor testDescriptor) {

		}

		@Override
		public void executionSkipped(TestDescriptor testDescriptor, String reason) {

		}

		@Override
		public void executionStarted(TestDescriptor testDescriptor) {

		}

		@Override
		public void executionFinished(TestDescriptor testDescriptor, TestExecutionResult testExecutionResult) {

		}

		@Override
		public void reportingEntryPublished(TestDescriptor testDescriptor, ReportEntry entry) {

		}
	}
}
