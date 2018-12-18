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

import jmh.mbr.junit5.PartiallyParametrizedBenchmark;
import jmh.mbr.junit5.descriptor.BenchmarkClassDescriptor;
import jmh.mbr.junit5.descriptor.BenchmarkMethodDescriptor;
import jmh.mbr.junit5.descriptor.ParametrizedBenchmarkMethodDescriptor;

import org.junit.jupiter.api.Test;
import org.junit.platform.commons.util.ClassFilter;
import org.junit.platform.engine.TestDescriptor;
import org.junit.platform.engine.UniqueId;
import org.junit.platform.engine.discovery.DiscoverySelectors;
import org.junit.platform.engine.support.descriptor.AbstractTestDescriptor;

/**
 * Unit tests for {@link JavaElementsResolver}.
 */
class JavaElementsResolverUnitTests {

	private final TestDescriptor ENGINE = new AbstractTestDescriptor(UniqueId.forEngine("foo"), "foo") {
		@Override
		public Type getType() {
			return Type.CONTAINER;
		}
	};

	private final ClassFilter FILTER = ClassFilter.of(it -> true);

	@Test
	void shouldResolveClassByPackageSelector() {

		JavaElementsResolver resolver = new JavaElementsResolver(ENGINE,
				ClassFilter.of(it -> it.equals(PartiallyParametrizedBenchmark.class)), ElementResolvers.getResolvers());

		resolver
				.resolvePackage(DiscoverySelectors.selectPackage(PartiallyParametrizedBenchmark.class.getPackage().getName()));

		assertThat(ENGINE.getChildren()).hasSize(1);

		TestDescriptor classDescriptor = ENGINE.getChildren().iterator().next();

		assertBenchmarkClass(classDescriptor);

		assertThat(classDescriptor.getChildren()).hasSize(2).hasOnlyElementsOfTypes(BenchmarkMethodDescriptor.class,
				ParametrizedBenchmarkMethodDescriptor.class);

		assertParametrizedMethod(classDescriptor);
	}

	@Test
	void shouldResolveClassByClassSelector() {

		JavaElementsResolver resolver = new JavaElementsResolver(ENGINE, FILTER, ElementResolvers.getResolvers());

		resolver.resolveClass(DiscoverySelectors.selectClass(PartiallyParametrizedBenchmark.class));

		assertThat(ENGINE.getChildren()).hasSize(1);

		TestDescriptor classDescriptor = ENGINE.getChildren().iterator().next();

		assertBenchmarkClass(classDescriptor);

		assertThat(classDescriptor.getChildren()).hasSize(2).hasOnlyElementsOfTypes(BenchmarkMethodDescriptor.class,
				ParametrizedBenchmarkMethodDescriptor.class);

		assertParametrizedMethod(classDescriptor);
	}

	@Test
	void shouldResolveBenchmarkMethodByMethodSelector() {

		JavaElementsResolver resolver = new JavaElementsResolver(ENGINE, FILTER, ElementResolvers.getResolvers());

		resolver.resolveMethod(DiscoverySelectors.selectMethod(PartiallyParametrizedBenchmark.class, "bar",
				"jmh.mbr.junit5.PartiallyParametrizedBenchmark$ParamState"));

		assertThat(ENGINE.getChildren()).hasSize(1);

		TestDescriptor classDescriptor = ENGINE.getChildren().iterator().next();

		assertBenchmarkClass(classDescriptor);

		assertThat(classDescriptor.getChildren()).hasSize(1)
				.hasOnlyElementsOfTypes(ParametrizedBenchmarkMethodDescriptor.class);

		assertParametrizedMethod(classDescriptor);
	}

	@Test
	void shouldResolveBenchmarkMethodByUniqueIdSelector() {

		JavaElementsResolver resolver = new JavaElementsResolver(ENGINE, FILTER, ElementResolvers.getResolvers());

		resolver.resolveUniqueId(DiscoverySelectors.selectUniqueId(
				"[engine:microbenchmark-engine]/[class:jmh.mbr.junit5.PartiallyParametrizedBenchmark]/[method:bar(jmh.mbr.junit5.PartiallyParametrizedBenchmark$ParamState)]/[fixture:%5Bfoo=b%5D]"));

		assertThat(ENGINE.getChildren()).hasSize(1);

		TestDescriptor classDescriptor = ENGINE.getChildren().iterator().next();

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
