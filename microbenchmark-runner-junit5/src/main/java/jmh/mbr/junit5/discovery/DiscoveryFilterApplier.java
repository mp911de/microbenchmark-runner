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

import jmh.mbr.junit5.descriptor.BenchmarkClassDescriptor;

import java.util.function.Predicate;

import org.junit.platform.engine.TestDescriptor;

/**
 * Class for applying filters to all children of a {@link TestDescriptor}.
 * 
 * @author Mark Paluch
 */
class DiscoveryFilterApplier {

	void applyClassNamePredicate(Predicate<String> classNamePredicate, TestDescriptor engineDescriptor) {
		TestDescriptor.Visitor filteringVisitor = descriptor -> {
			if (descriptor instanceof BenchmarkClassDescriptor
					&& !includeClass((BenchmarkClassDescriptor) descriptor, classNamePredicate)) {
				descriptor.removeFromHierarchy();
			}
		};
		engineDescriptor.accept(filteringVisitor);
	}

	private boolean includeClass(BenchmarkClassDescriptor classTestDescriptor, Predicate<String> classNamePredicate) {
		return classNamePredicate.test(classTestDescriptor.getJavaClass().getName());
	}
}
