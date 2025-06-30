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

import static java.util.stream.Collectors.*;
import static org.junit.platform.engine.discovery.DiscoverySelectors.*;
import static org.junit.platform.engine.support.discovery.SelectorResolver.Resolution.*;

import jmh.mbr.core.model.BenchmarkClass;
import jmh.mbr.core.model.BenchmarkDescriptorFactory;
import jmh.mbr.junit5.descriptor.BenchmarkClassDescriptor;
import jmh.mbr.junit5.discovery.predicates.IsBenchmarkClass;
import jmh.mbr.junit5.discovery.predicates.IsBenchmarkMethod;

import java.util.Optional;
import java.util.function.Predicate;

import org.junit.platform.commons.support.HierarchyTraversalMode;
import org.junit.platform.commons.support.ReflectionSupport;
import org.junit.platform.engine.TestDescriptor;
import org.junit.platform.engine.UniqueId;
import org.junit.platform.engine.discovery.ClassSelector;
import org.junit.platform.engine.discovery.UniqueIdSelector;
import org.junit.platform.engine.support.discovery.SelectorResolver;

/**
 * {@link SelectorResolver} for test containers. Containers are based on {@link Class classes} that contain
 * {@code Benchmark} methods.
 *
 * @see IsBenchmarkClass
 */
class BenchmarkContainerResolver implements SelectorResolver {

	private static final String SEGMENT_TYPE = "class";

	private final Predicate<String> classNameFilter;

	BenchmarkContainerResolver(Predicate<String> classNameFilter) {
		this.classNameFilter = classNameFilter;
	}

	@Override
	public Resolution resolve(ClassSelector selector, Context context) {
		if (classNameFilter.test(selector.getClassName())) {
			return resolveClass(selector.getJavaClass(), context);
		}
		return Resolution.unresolved();
	}

	@Override
	public Resolution resolve(UniqueIdSelector selector, Context context) {
		UniqueId uniqueId = selector.getUniqueId();
		UniqueId.Segment lastSegment = uniqueId.getLastSegment();

		if (lastSegment.getType().equals(SEGMENT_TYPE)) {
			return ReflectionSupport.tryToLoadClass(lastSegment.getValue()).toOptional()
					.map(testClass -> resolveClass(testClass, context)).orElse(unresolved());
		}

		return unresolved();
	}

	private Resolution resolveClass(Class<?> testClass, Context context) {
		if (IsBenchmarkClass.INSTANCE.test(testClass)) {
			return context.addToParent(parent -> createClassDescriptor(testClass, parent))
					.map(descriptor -> Resolution.match(Match.exact(descriptor,
							() -> ReflectionSupport
									.streamMethods(testClass, IsBenchmarkMethod.INSTANCE, HierarchyTraversalMode.TOP_DOWN)
									.map(m -> selectMethod(testClass, m)).collect(toSet())))) //
					.orElse(unresolved());
		}
		return unresolved();
	}

	private static Optional<BenchmarkClassDescriptor> createClassDescriptor(Class<?> testClass, TestDescriptor parent) {
		UniqueId uniqueId = parent.getUniqueId().append(BenchmarkContainerResolver.SEGMENT_TYPE, testClass.getName());
		BenchmarkClass descriptor = BenchmarkDescriptorFactory.create(testClass).createDescriptor();
		return Optional.of(new BenchmarkClassDescriptor(uniqueId, descriptor));
	}

}
