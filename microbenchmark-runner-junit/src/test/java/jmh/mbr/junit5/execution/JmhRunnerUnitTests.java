/*
 * Copyright 2018-2020 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v20.html
 */
package jmh.mbr.junit5.execution;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.regex.Pattern;

import jmh.mbr.core.model.BenchmarkClass;
import jmh.mbr.core.model.BenchmarkDescriptor;
import jmh.mbr.core.model.BenchmarkDescriptorFactory;
import jmh.mbr.core.model.BenchmarkMethod;
import jmh.mbr.core.model.BenchmarkResults;
import jmh.mbr.junit5.JmhRunnerStub;
import jmh.mbr.junit5.config.DefaultMbrConfiguration;
import jmh.mbr.junit5.config.MbrConfiguration;
import jmh.mbr.junit5.descriptor.AbstractBenchmarkDescriptor;
import jmh.mbr.junit5.descriptor.BenchmarkClassDescriptor;
import jmh.mbr.junit5.descriptor.BenchmarkMethodDescriptor;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.engine.extension.MutableExtensionRegistry;
import org.junit.platform.engine.ConfigurationParameters;
import org.junit.platform.engine.EngineExecutionListener;
import org.junit.platform.engine.TestDescriptor;
import org.junit.platform.engine.TestExecutionResult;
import org.junit.platform.engine.UniqueId;
import org.junit.platform.engine.reporting.OutputDirectoryProvider;
import org.junit.platform.engine.reporting.ReportEntry;
import org.junit.platform.engine.support.store.NamespacedHierarchicalStore;
import org.openjdk.jmh.annotations.Benchmark;

import static org.assertj.core.api.Assertions.*;

/**
 * Unit tests for {@link JmhRunner}.
 */
public class JmhRunnerUnitTests {

	JmhRunnerStub runner;

	{
		MbrConfiguration configuration = new DefaultMbrConfiguration(EmptyConfigurationParameters.INSTANCE, EmptyOutputDirectoryProvider.INSTANCE, new NamespacedHierarchicalStore(null).newChild());
		runner = new JmhRunnerStub(configuration, MutableExtensionRegistry
				.createRegistryWithDefaultExtensions(configuration)) {
		};
	}

	@Test
	void shouldIncludeUnconditionalBenchmarkClass() {

		List<AbstractBenchmarkDescriptor> descriptors = Collections
				.singletonList(createDescriptor(SimpleBenchmarkClass.class));

		List<String> includePatterns = runner
				.evaluateBenchmarksToRun(descriptors, EmptyEngineExecutionListener.INSTANCE);

		assertThat(includePatterns).hasSize(1).contains(Pattern
				.quote(SimpleBenchmarkClass.class.getName()
						.replace('$', '.')) + "\\." + Pattern
				.quote("justOne") + "$");
	}

	@Test
	void shouldIncludeUnconditionalBenchmarkMethod() {

		BenchmarkClassDescriptor descriptor = createDescriptor(SimpleBenchmarkClass.class);
		List<AbstractBenchmarkDescriptor> descriptors = Collections
				.singletonList((AbstractBenchmarkDescriptor) descriptor.getChildren()
						.iterator().next());

		List<String> includePatterns = runner
				.evaluateBenchmarksToRun(descriptors, EmptyEngineExecutionListener.INSTANCE);

		assertThat(includePatterns).hasSize(1).contains(Pattern
				.quote(SimpleBenchmarkClass.class.getName()
						.replace('$', '.')) + "\\." + Pattern
				.quote("justOne") + "$");
	}

	@Test
	void shouldIncludeEnabledMethod() {

		List<AbstractBenchmarkDescriptor> descriptors = Collections
				.singletonList(createDescriptor(ConditionalMethods.class));

		List<String> includePatterns = runner
				.evaluateBenchmarksToRun(descriptors, EmptyEngineExecutionListener.INSTANCE);

		assertThat(includePatterns).hasSize(1).contains(Pattern
				.quote(ConditionalMethods.class.getName()
						.replace('$', '.')) + "\\." + Pattern
				.quote("enabled") + "$");
	}

	@Test
	void shouldNotContainDisabledBenchmarkClass() {

		List<AbstractBenchmarkDescriptor> descriptors = Collections
				.singletonList(createDescriptor(DisabledBenchmark.class));

		List<String> includePatterns = runner
				.evaluateBenchmarksToRun(descriptors, EmptyEngineExecutionListener.INSTANCE);

		assertThat(includePatterns).isEmpty();
	}

	@Test
	public void shouldCaptureConfigurationParameters() {

		CapturingConfigurationParameters parameters = new CapturingConfigurationParameters(Collections
				.singletonMap("jmh.mbr.project", "my beloved one!"));

		MbrConfiguration configuration = new DefaultMbrConfiguration(parameters, EmptyOutputDirectoryProvider.INSTANCE, new NamespacedHierarchicalStore(null).newChild());
		JmhRunnerStub runner = new JmhRunnerStub(configuration, MutableExtensionRegistry
				.createRegistryWithDefaultExtensions(configuration)) {
		};

		runner.onRunReturnEmptyResult();
		runner.execute(SimpleBenchmarkClass.class);

		BenchmarkResults results = runner.getResult();

		parameters.verifyKeyRequested("jmh.mbr.project", "jmh.mbr.project.version");
		assertThat(results.getMetaData().getProject()).isEqualTo("my beloved one!");
	}


	private BenchmarkClassDescriptor createDescriptor(Class<?> javaClass) {

		BenchmarkClass benchmarkClass = BenchmarkDescriptorFactory.create(javaClass)
				.createDescriptor();

		BenchmarkClassDescriptor descriptor = new BenchmarkClassDescriptor(UniqueId
				.root("root", "root"), benchmarkClass);

		for (BenchmarkDescriptor child : benchmarkClass.getChildren()) {

			if (child instanceof BenchmarkMethod) {
				BenchmarkMethod method = (BenchmarkMethod) child;

				descriptor.addChild(new BenchmarkMethodDescriptor(descriptor.getUniqueId()
						.append("method", (method).getName()), method));
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

	static class CapturingConfigurationParameters implements ConfigurationParameters {

		List<String> capturedKeys = new ArrayList<>();
		private Function<String, String> callback;

		public CapturingConfigurationParameters(Map<String, String> map) {
			this(map::get);
		}

		public CapturingConfigurationParameters(Function<String, String> callback) {
			this.callback = callback;
		}

		@Override
		public Optional<String> get(String key) {
			capturedKeys.add(key);
			return Optional.ofNullable(callback.apply(key));
		}

		@Override
		public Set<String> keySet() {
			return new LinkedHashSet<>(capturedKeys);
		}

		@Override
		public Optional<Boolean> getBoolean(String key) {
			return get(key).map(Boolean::parseBoolean);
		}

		@Override
		public int size() {
			return 1;
		}

		public void verifyKeyRequested(String... keys) {

			for (String key : keys) {
				assertThat(capturedKeys.contains(key))
						.withFailMessage("Expected key '%s' to be requested at least once.", key)
						.isTrue();
			}
		}

		public void verifyKeyRequested(String key, long times) {

			long count = capturedKeys.stream().filter(key::equals).count();
			assertThat(count)
					.withFailMessage("Expected key '%s' to be requested exactly %s times. But was %s.", key, times, count)
					.isEqualTo(times);
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

	enum EmptyOutputDirectoryProvider implements OutputDirectoryProvider {
		INSTANCE;

		@Override
		public Path getRootDirectory() {
			return null;
		}

		@Override
		public Path createOutputDirectory(TestDescriptor testDescriptor) throws IOException {
			return null;
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
