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
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * {@link BenchmarkDescriptor} that represents a hierarchy of benchmark configurations.
 */
public class HierarchicalBenchmarkDescriptor implements BenchmarkDescriptor {

	private final BenchmarkDescriptor descriptor;
	private final List<? extends BenchmarkDescriptor> children;

	HierarchicalBenchmarkDescriptor(BenchmarkDescriptor descriptor, List<? extends BenchmarkDescriptor> children) {

		Objects.requireNonNull(descriptor, "BenchmarkDescriptor must not be null!");
		Objects.requireNonNull(children, "Children must not be null!");

		this.descriptor = descriptor;
		this.children = Collections.unmodifiableList(children);
	}

	/**
	 * Create a {@link HierarchicalBenchmarkDescriptor} without children.
	 *
	 * @param descriptor the {@link BenchmarkDescriptor} that hosts benchmark.
	 * @return the {@link HierarchicalBenchmarkDescriptor} for {@link BenchmarkDescriptor}.
	 */
	public static HierarchicalBenchmarkDescriptor create(BenchmarkDescriptor descriptor) {

		Objects.requireNonNull(descriptor, "BenchmarkDescriptor must not be null!");

		return new HierarchicalBenchmarkDescriptor(descriptor, Collections.emptyList());
	}

	/**
	 * Create a {@link HierarchicalBenchmarkDescriptor} with children.
	 *
	 * @param descriptor the {@link BenchmarkDescriptor} that hosts benchmark.
	 * @param children benchmark children.
	 * @return the {@link HierarchicalBenchmarkDescriptor} for {@link BenchmarkDescriptor}.
	 */
	public static HierarchicalBenchmarkDescriptor create(BenchmarkDescriptor descriptor,
			Collection<BenchmarkDescriptor> children) {

		Objects.requireNonNull(descriptor, "BenchmarkDescriptor must not be null!");
		Objects.requireNonNull(children, "Children must not be null!");

		return new HierarchicalBenchmarkDescriptor(descriptor, new ArrayList<>(children));
	}

	public BenchmarkDescriptor getDescriptor() {
		return descriptor;
	}

	public List<? extends BenchmarkDescriptor> getChildren() {
		return children;
	}
}
