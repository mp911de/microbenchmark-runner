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
 * 
 * @author Mark Paluch
 */
class BenchmarkFixtureResolver implements ElementResolver {

	private static final String SEGMENT_TYPE = "fixture";

	/* 
	 * (non-Javadoc)
	 * @see jmh.mbr.junit5.discovery.ElementResolver#resolveElement(java.lang.reflect.AnnotatedElement, org.junit.platform.engine.TestDescriptor)
	 */
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

	/* 
	 * (non-Javadoc)
	 * @see jmh.mbr.junit5.discovery.ElementResolver#resolveUniqueId(org.junit.platform.engine.UniqueId.Segment, org.junit.platform.engine.TestDescriptor)
	 */
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
