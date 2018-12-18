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

import jmh.mbr.junit5.descriptor.BenchmarkClassDescriptor;

import java.util.function.Predicate;

import org.junit.platform.engine.TestDescriptor;

/**
 * Class for applying filters to all children of a {@link TestDescriptor}.
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
