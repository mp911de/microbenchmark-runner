/*
 * Copyright 2018 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v20.html
 */
package jmh.mbr.junit5.discovery;

import static org.assertj.core.api.Assertions.*;
import static org.junit.platform.engine.discovery.ClassNameFilter.*;
import static org.junit.platform.engine.discovery.DiscoverySelectors.*;

import jmh.mbr.junit5.MicrobenchmarkEngine;
import jmh.mbr.junit5.PartiallyParametrizedBenchmark;
import jmh.mbr.junit5.descriptor.BenchmarkClassDescriptor;
import jmh.mbr.junit5.descriptor.BenchmarkMethodDescriptor;
import jmh.mbr.junit5.descriptor.ParametrizedBenchmarkMethodDescriptor;

import java.util.regex.Pattern;

import org.junit.jupiter.api.Test;
import org.junit.platform.engine.Filter;
import org.junit.platform.engine.TestDescriptor;
import org.junit.platform.testkit.engine.EngineDiscoveryResults;
import org.junit.platform.testkit.engine.EngineTestKit;

/**
 * Tests for {@link DiscoverySelectorResolver}.
 */
class DiscoverySelectorResolverTests {

	@Test
	void shouldResolveClassByPackageSelector() {

		EngineDiscoveryResults results = EngineTestKit.engine(new MicrobenchmarkEngine())
				.selectors(selectPackage(PartiallyParametrizedBenchmark.class.getPackage().getName()))
				.filters((Filter<?>) includeClassNamePatterns(Pattern.quote(PartiallyParametrizedBenchmark.class.getName())))
				.discover();

		TestDescriptor engineDescriptor = results.getEngineDescriptor();

		assertThat(engineDescriptor.getChildren()).hasSize(1);

		TestDescriptor classDescriptor = engineDescriptor.getChildren().iterator().next();

		assertBenchmarkClass(classDescriptor);

		assertThat(classDescriptor.getChildren()).hasSize(2).hasOnlyElementsOfTypes(BenchmarkMethodDescriptor.class,
				ParametrizedBenchmarkMethodDescriptor.class);

		assertParametrizedMethod(classDescriptor);
	}

	@Test
	void shouldResolveClassByClassSelector() {

		EngineDiscoveryResults results = EngineTestKit.engine(new MicrobenchmarkEngine())
				.selectors(selectClass(PartiallyParametrizedBenchmark.class)).discover();

		TestDescriptor engineDescriptor = results.getEngineDescriptor();

		assertThat(engineDescriptor.getChildren()).hasSize(1);

		TestDescriptor classDescriptor = engineDescriptor.getChildren().iterator().next();

		assertBenchmarkClass(classDescriptor);

		assertThat(classDescriptor.getChildren()).hasSize(2).hasOnlyElementsOfTypes(BenchmarkMethodDescriptor.class,
				ParametrizedBenchmarkMethodDescriptor.class);

		assertParametrizedMethod(classDescriptor);
	}

	@Test
	void shouldResolveBenchmarkMethodByMethodSelector() {

		EngineDiscoveryResults results = EngineTestKit.engine(new MicrobenchmarkEngine())
				.selectors(selectMethod(PartiallyParametrizedBenchmark.class, "bar",
						"jmh.mbr.junit5.PartiallyParametrizedBenchmark$ParamState"))
				.discover();

		TestDescriptor engineDescriptor = results.getEngineDescriptor();

		assertThat(engineDescriptor.getChildren()).hasSize(1);

		TestDescriptor classDescriptor = engineDescriptor.getChildren().iterator().next();

		assertBenchmarkClass(classDescriptor);

		assertThat(classDescriptor.getChildren()).hasSize(1)
				.hasOnlyElementsOfTypes(ParametrizedBenchmarkMethodDescriptor.class);

		assertParametrizedMethod(classDescriptor);
	}

	@Test
	void shouldResolveBenchmarkMethodByUniqueIdSelector() {

		EngineDiscoveryResults results = EngineTestKit.engine(new MicrobenchmarkEngine()).selectors(selectUniqueId(
				"[engine:microbenchmark-engine]/[class:jmh.mbr.junit5.PartiallyParametrizedBenchmark]/[method:bar(jmh.mbr.junit5.PartiallyParametrizedBenchmark$ParamState)]/[fixture:%5Bfoo=b%5D]"))
				.discover();

		TestDescriptor engineDescriptor = results.getEngineDescriptor();

		assertThat(engineDescriptor.getChildren()).hasSize(1);

		TestDescriptor classDescriptor = engineDescriptor.getChildren().iterator().next();

		assertBenchmarkClass(classDescriptor);

		assertThat(classDescriptor.getChildren()).hasSize(1)
				.hasOnlyElementsOfTypes(ParametrizedBenchmarkMethodDescriptor.class);

		TestDescriptor parametrizedDescriptor = classDescriptor.getChildren().stream()
				.filter(ParametrizedBenchmarkMethodDescriptor.class::isInstance).findFirst().get();

		assertThat(parametrizedDescriptor.getChildren()).hasSize(1);
	}

	private void assertBenchmarkClass(TestDescriptor classDescriptor) {
		assertThat(classDescriptor).isInstanceOf(BenchmarkClassDescriptor.class);
		assertThat(classDescriptor.getSource()).isNotEmpty();
		assertThat(classDescriptor.getDisplayName()).isEqualTo(PartiallyParametrizedBenchmark.class.getName());
	}

	private void assertParametrizedMethod(TestDescriptor classDescriptor) {

		TestDescriptor parametrizedDescriptor = classDescriptor.getChildren().stream()
				.filter(ParametrizedBenchmarkMethodDescriptor.class::isInstance).findFirst().get();

		assertThat(parametrizedDescriptor.getChildren()).hasSize(2);
	}
}
