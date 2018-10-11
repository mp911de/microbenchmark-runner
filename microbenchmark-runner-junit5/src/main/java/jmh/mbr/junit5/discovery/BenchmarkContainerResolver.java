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
 * @author Mark Paluch
 * @see IsBenchmarkClass
 */
class BenchmarkContainerResolver implements ElementResolver {

	private static final String SEGMENT_TYPE = "class";

	BenchmarkContainerResolver() {}

	/* 
	 * (non-Javadoc)
	 * @see jmh.mbr.junit5.discovery.ElementResolver#resolveElement(java.lang.reflect.AnnotatedElement, org.junit.platform.engine.TestDescriptor)
	 */
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

	/* 
	 * (non-Javadoc)
	 * @see jmh.mbr.junit5.discovery.ElementResolver#resolveUniqueId(org.junit.platform.engine.UniqueId.Segment, org.junit.platform.engine.TestDescriptor)
	 */
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
