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

import jmh.mbr.core.model.BenchmarkClass;
import jmh.mbr.core.model.BenchmarkDescriptor;
import jmh.mbr.core.model.BenchmarkMethod;
import jmh.mbr.core.model.MethodAware;
import jmh.mbr.core.model.ParametrizedBenchmarkMethod;
import jmh.mbr.junit5.descriptor.BenchmarkClassDescriptor;
import jmh.mbr.junit5.descriptor.BenchmarkMethodDescriptor;
import jmh.mbr.junit5.descriptor.ParametrizedBenchmarkMethodDescriptor;

import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Method;
import java.util.Collections;
import java.util.Optional;
import java.util.Set;

import org.junit.platform.engine.TestDescriptor;
import org.junit.platform.engine.UniqueId;
import org.junit.platform.engine.UniqueId.Segment;

/**
 * {@link ElementResolver} for {@code Benchmark} {@link Method methods}.
 */
class BenchmarkMethodResolver implements ElementResolver {

	private static final String SEGMENT_TYPE = "method";

	@Override
	public Set<TestDescriptor> resolveElement(AnnotatedElement element, TestDescriptor parent) {

		if (!(element instanceof Method)) {
			return Collections.emptySet();
		}

		if (!(parent instanceof BenchmarkClassDescriptor)) {
			return Collections.emptySet();
		}

		Method method = (Method) element;
		BenchmarkClassDescriptor classDescriptor = (BenchmarkClassDescriptor) parent;

		if (!method.getDeclaringClass().isAssignableFrom(classDescriptor.getJavaClass())) {
			return Collections.emptySet();
		}

		return findMethod(parent.getUniqueId(), classDescriptor.getBenchmarkClass(), method).map(Collections::singleton)
				.orElseGet(Collections::emptySet);
	}

	@Override
	public Optional<TestDescriptor> resolveUniqueId(Segment segment, TestDescriptor parent) {

		if (!segment.getType().equals(SEGMENT_TYPE)) {
			return Optional.empty();
		}

		if (!(parent instanceof BenchmarkClassDescriptor)) {
			return Optional.empty();
		}

		BenchmarkClassDescriptor descriptor = (BenchmarkClassDescriptor) parent;

		return findMethod(segment, parent.getUniqueId(), descriptor.getBenchmarkClass());
	}

	private Optional<TestDescriptor> findMethod(UniqueId parentId, BenchmarkClass benchmarkClass, Method method) {

		return benchmarkClass.getChildren().stream() //
				.filter(MethodAware.class::isInstance) //
				.filter(it -> ((MethodAware) it).isUnderlyingMethod(method)) //
				.map(it -> createDescriptor(parentId, it)).findFirst();
	}

	private Optional<TestDescriptor> findMethod(Segment segment, UniqueId parentId, BenchmarkClass benchmarkClass) {

		return benchmarkClass.getChildren().stream() //
				.filter(MethodAware.class::isInstance) //
				.filter(it -> {

					Method method = ((MethodAware) it).getMethod();
					String id = BenchmarkMethodDescriptor.describeMethodId(method);

					return segment.getValue().equals(id);
				}) //
				.map(it -> createDescriptor(parentId, it)) //
				.findFirst();
	}

	private TestDescriptor createDescriptor(UniqueId parentId, BenchmarkDescriptor it) {

		if (it instanceof ParametrizedBenchmarkMethod) {
			ParametrizedBenchmarkMethod parametrized = (ParametrizedBenchmarkMethod) it;
			UniqueId uniqueId = BenchmarkMethodDescriptor.createUniqueId(parentId, parametrized.getDescriptor());

			return new ParametrizedBenchmarkMethodDescriptor(uniqueId, parametrized);
		}

		BenchmarkMethod benchmarkMethod = (BenchmarkMethod) it;
		UniqueId uniqueId = BenchmarkMethodDescriptor.createUniqueId(parentId, benchmarkMethod);

		return new BenchmarkMethodDescriptor(uniqueId, benchmarkMethod);
	}

}
