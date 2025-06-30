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

import jmh.mbr.core.model.BenchmarkDescriptor;
import jmh.mbr.core.model.BenchmarkMethod;
import jmh.mbr.core.model.MethodAware;
import jmh.mbr.core.model.ParametrizedBenchmarkMethod;
import jmh.mbr.junit5.descriptor.AbstractBenchmarkDescriptor;
import jmh.mbr.junit5.descriptor.BenchmarkClassDescriptor;
import jmh.mbr.junit5.descriptor.BenchmarkMethodDescriptor;
import jmh.mbr.junit5.descriptor.ParametrizedBenchmarkMethodDescriptor;
import jmh.mbr.junit5.discovery.predicates.IsBenchmarkMethod;

import java.lang.reflect.Method;
import java.util.Optional;

import org.junit.platform.engine.TestDescriptor;
import org.junit.platform.engine.UniqueId;
import org.junit.platform.engine.discovery.MethodSelector;
import org.junit.platform.engine.discovery.UniqueIdSelector;
import org.junit.platform.engine.support.discovery.SelectorResolver;

/**
 * {@link SelectorResolver} for {@code Benchmark} {@link Method methods}.
 */
class BenchmarkMethodResolver implements SelectorResolver {

	private static final String SEGMENT_TYPE = "method";

	@Override
	public Resolution resolve(MethodSelector selector, Context context) {
		Method method = selector.getJavaMethod();
		if (IsBenchmarkMethod.INSTANCE.test(method)) {
			return context
					.addToParent(() -> selectClass(method.getDeclaringClass()),
							parent -> createMethodDescriptor((BenchmarkClassDescriptor) parent, method))
					.map(BenchmarkMethodResolver::toResolution).orElse(unresolved());
		}
		return unresolved();
	}

	@Override
	public Resolution resolve(UniqueIdSelector selector, Context context) {

		UniqueId uniqueId = selector.getUniqueId();
		UniqueId.Segment lastSegment = uniqueId.getLastSegment();

		if (lastSegment.getType().equals(SEGMENT_TYPE)) {
			return context //
					.addToParent(() -> selectUniqueId(uniqueId.removeLastSegment()), parent -> {
						BenchmarkClassDescriptor classDescriptor = (BenchmarkClassDescriptor) parent;
						return findBenchmarkDescriptor(classDescriptor, lastSegment) //
								.flatMap(it -> createMethodDescriptor(classDescriptor, ((MethodAware) it).getMethod()));
					}) //
					.map(BenchmarkMethodResolver::toResolution) //
					.orElse(unresolved());
		}

		return unresolved();
	}

	private static Optional<? extends BenchmarkDescriptor> findBenchmarkDescriptor(
			BenchmarkClassDescriptor classDescriptor, UniqueId.Segment lastSegment) {
		return classDescriptor.getBenchmarkClass().getChildren().stream() //
				.filter(MethodAware.class::isInstance) //
				.filter(benchmarkDescriptor -> {

					Method method = ((MethodAware) benchmarkDescriptor).getMethod();
					String id = BenchmarkMethodDescriptor.describeMethodId(method);

					return lastSegment.getValue().equals(id);
				}) //
				.findFirst();
	}

	private static Resolution toResolution(TestDescriptor descriptor) {
		if (descriptor instanceof ParametrizedBenchmarkMethodDescriptor) {
			ParametrizedBenchmarkMethodDescriptor parametrizedMethodDescriptor = (ParametrizedBenchmarkMethodDescriptor) descriptor;
			return Resolution.match(Match.exact(descriptor,
					() -> parametrizedMethodDescriptor.getParametrizedMethod().getChildren().stream() //
							.map(it -> selectUniqueId(
									descriptor.getUniqueId().append(BenchmarkFixtureResolver.SEGMENT_TYPE, it.getDisplayName()))) //
							.collect(toSet())));
		}
		return Resolution.match(Match.exact(descriptor));
	}

	private static Optional<TestDescriptor> createMethodDescriptor(BenchmarkClassDescriptor parent, Method method) {
		return parent.getBenchmarkClass().getChildren().stream() //
				.filter(MethodAware.class::isInstance)
				.filter(benchmarkDescriptor -> ((MethodAware) benchmarkDescriptor).isUnderlyingMethod(method)) //
				.findFirst() //
				.map(benchmarkDescriptor -> toMethodDescriptor(parent, benchmarkDescriptor));
	}

	private static AbstractBenchmarkDescriptor toMethodDescriptor(BenchmarkClassDescriptor parent,
			BenchmarkDescriptor benchmarkDescriptor) {

		UniqueId parentId = parent.getUniqueId();

		if (benchmarkDescriptor instanceof ParametrizedBenchmarkMethod) {
			ParametrizedBenchmarkMethod parametrized = (ParametrizedBenchmarkMethod) benchmarkDescriptor;
			UniqueId uniqueId = BenchmarkMethodDescriptor.createUniqueId(parentId, parametrized.getDescriptor());

			return new ParametrizedBenchmarkMethodDescriptor(uniqueId, parametrized);
		}

		BenchmarkMethod benchmarkMethod = (BenchmarkMethod) benchmarkDescriptor;
		UniqueId uniqueId = BenchmarkMethodDescriptor.createUniqueId(parentId, benchmarkMethod);

		return new BenchmarkMethodDescriptor(uniqueId, benchmarkMethod);
	}

}
