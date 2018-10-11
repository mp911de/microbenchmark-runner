/*
 * Copyright 2018 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
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
 * 
 * @author Mark Paluch
 */
class BenchmarkMethodResolver implements ElementResolver {

	private static final String SEGMENT_TYPE = "method";

	/* 
	 * (non-Javadoc)
	 * @see jmh.mbr.junit5.discovery.ElementResolver#resolveElement(java.lang.reflect.AnnotatedElement, org.junit.platform.engine.TestDescriptor)
	 */
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

	/* 
	 * (non-Javadoc)
	 * @see jmh.mbr.junit5.discovery.ElementResolver#resolveUniqueId(org.junit.platform.engine.UniqueId.Segment, org.junit.platform.engine.TestDescriptor)
	 */
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
