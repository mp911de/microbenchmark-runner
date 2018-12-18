/*
 * Copyright 2018 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v20.html
 */
package jmh.mbr.core.model;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;

/**
 * {@link BenchmarkDescriptor} for a single {@link Class benchmark class} along its children.
 *
 * @see BenchmarkMethod
 * @see HierarchicalBenchmarkDescriptor
 */
public class BenchmarkClass extends HierarchicalBenchmarkDescriptor {

	private BenchmarkClass(ClassDescriptor descriptor, List<BenchmarkDescriptor> children) {
		super(descriptor, children);
	}

	/**
	 * Create a new {@link BenchmarkClass} given {@link Class the benchmark class} and its children.
	 *
	 * @param benchmarkClass the actual {@link Class benchmark class} to inspect.
	 * @param children child descriptors.
	 * @return the {@link BenchmarkClass} descriptor.
	 */
	public static BenchmarkClass create(Class<?> benchmarkClass, Collection<? extends BenchmarkDescriptor> children) {

		Objects.requireNonNull(benchmarkClass, "Benchmark class must not be null!");
		Objects.requireNonNull(children, "Children must not be null!");

		return new BenchmarkClass(new ClassDescriptor(benchmarkClass), new ArrayList<>(children));
	}

	public Class<?> getJavaClass() {
		return ((ClassDescriptor) getDescriptor()).benchmarkClass;
	}

	private static class ClassDescriptor implements BenchmarkDescriptor {

		private final Class<?> benchmarkClass;

		private ClassDescriptor(Class<?> benchmarkClass) {
			this.benchmarkClass = benchmarkClass;
		}
	}
}
