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
import jmh.mbr.core.model.BenchmarkDescriptorFactory;
import jmh.mbr.junit5.descriptor.BenchmarkClassDescriptor;
import jmh.mbr.junit5.discovery.predicates.IsBenchmarkClass;

import java.lang.reflect.AnnotatedElement;
import java.util.Collections;
import java.util.Optional;
import java.util.Set;

import org.junit.platform.commons.util.ReflectionUtils;
import org.junit.platform.engine.TestDescriptor;
import org.junit.platform.engine.UniqueId;

/**
 * {@link ElementResolver} for test containers. Containers are based on {@link Class classes} that contain
 * {@code Benchmark} methods.
 *
 * @see IsBenchmarkClass
 */
class BenchmarkContainerResolver implements ElementResolver {

	private static final String SEGMENT_TYPE = "class";

	BenchmarkContainerResolver() {}

	@Override
	public Set<TestDescriptor> resolveElement(AnnotatedElement element, TestDescriptor parent) {

		if (!(element instanceof Class)) {
			return Collections.emptySet();
		}

		Class<?> clazz = (Class<?>) element;
		if (!isPotentialCandidate(clazz)) {
			return Collections.emptySet();
		}

		UniqueId uniqueId = createUniqueId(clazz, parent);
		return Collections.singleton(resolveClass(clazz, uniqueId));
	}

	@Override
	public Optional<TestDescriptor> resolveUniqueId(UniqueId.Segment segment, TestDescriptor parent) {

		if (!segment.getType().equals(SEGMENT_TYPE)) {
			return Optional.empty();
		}

		if (!requiredParentType().isInstance(parent)) {
			return Optional.empty();
		}

		String className = getClassName(parent, segment.getValue());

		Optional<Class<?>> optionalContainerClass = ReflectionUtils.loadClass(className);
		if (!optionalContainerClass.isPresent()) {
			return Optional.empty();
		}

		Class<?> containerClass = optionalContainerClass.get();
		if (!isPotentialCandidate(containerClass)) {
			return Optional.empty();
		}

		UniqueId uniqueId = createUniqueId(containerClass, parent);
		return Optional.of(resolveClass(containerClass, uniqueId));
	}

	private Class<? extends TestDescriptor> requiredParentType() {
		return TestDescriptor.class;
	}

	private String getClassName(TestDescriptor parent, String segmentValue) {
		return segmentValue;
	}

	private boolean isPotentialCandidate(Class<?> element) {
		return IsBenchmarkClass.INSTANCE.test(element);
	}

	private UniqueId createUniqueId(Class<?> benchmarkClass, TestDescriptor parent) {
		return parent.getUniqueId().append(SEGMENT_TYPE, benchmarkClass.getName());
	}

	private TestDescriptor resolveClass(Class<?> benchmarkClass, UniqueId uniqueId) {

		BenchmarkClass descriptor = BenchmarkDescriptorFactory.create(benchmarkClass).createDescriptor();

		return new BenchmarkClassDescriptor(uniqueId, descriptor);
	}
}
