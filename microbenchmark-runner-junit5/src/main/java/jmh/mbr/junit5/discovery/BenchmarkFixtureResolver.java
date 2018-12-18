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

import jmh.mbr.core.model.ParametrizedBenchmarkMethod;
import jmh.mbr.junit5.descriptor.BenchmarkFixtureDescriptor;
import jmh.mbr.junit5.descriptor.ParametrizedBenchmarkMethodDescriptor;

import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Method;
import java.util.Collections;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.junit.platform.engine.TestDescriptor;
import org.junit.platform.engine.UniqueId;
import org.junit.platform.engine.UniqueId.Segment;

/**
 * {@link ElementResolver} for {@link jmh.mbr.core.model.BenchmarkFixture fixtures}.
 */
class BenchmarkFixtureResolver implements ElementResolver {

	private static final String SEGMENT_TYPE = "fixture";

	@Override
	public Set<TestDescriptor> resolveElement(AnnotatedElement element, TestDescriptor parent) {

		if (!(element instanceof Method)) {
			return Collections.emptySet();
		}

		if (!(parent instanceof ParametrizedBenchmarkMethodDescriptor)) {
			return Collections.emptySet();
		}

		ParametrizedBenchmarkMethodDescriptor parentDescriptor = (ParametrizedBenchmarkMethodDescriptor) parent;

		Method method = (Method) element;

		if (!parentDescriptor.isUnderlyingMethod(method)) {
			return Collections.emptySet();
		}

		return findFixtures(parent.getUniqueId(), parentDescriptor.getParametrizedMethod());
	}

	@Override
	public Optional<TestDescriptor> resolveUniqueId(Segment segment, TestDescriptor parent) {

		if (!segment.getType().equals(SEGMENT_TYPE)) {
			return Optional.empty();
		}

		if (!(parent instanceof ParametrizedBenchmarkMethodDescriptor)) {
			return Optional.empty();
		}

		ParametrizedBenchmarkMethodDescriptor parentDescriptor = (ParametrizedBenchmarkMethodDescriptor) parent;

		return findTestDescriptor(segment.getValue(), parentDescriptor.getUniqueId(),
				parentDescriptor.getParametrizedMethod());
	}

	private Set<TestDescriptor> findFixtures(UniqueId parentId, ParametrizedBenchmarkMethod descriptor) {

		return descriptor.getChildren().stream().map(it -> {

			UniqueId uniqueId = parentId.append(SEGMENT_TYPE, it.getDisplayName());
			return new BenchmarkFixtureDescriptor(uniqueId, descriptor.getDescriptor(), it);
		}).collect(Collectors.toSet());
	}

	private Optional<TestDescriptor> findTestDescriptor(String segmentValue, UniqueId parentId,
			ParametrizedBenchmarkMethod parametrizedMethod) {

		return parametrizedMethod.getChildren().stream().filter(it -> {
			return it.getDisplayName().equals(segmentValue);
		}).map(it -> {

			UniqueId uniqueId = parentId.append(SEGMENT_TYPE, it.getDisplayName());

			return (TestDescriptor) new BenchmarkFixtureDescriptor(uniqueId, parametrizedMethod.getDescriptor(), it);
		}).findFirst();
	}
}
