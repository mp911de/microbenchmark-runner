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
package jmh.mbr.core.model;

import lombok.Getter;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * {@link BenchmarkDescriptor} that represents a hierarchy of benchmark configurations.
 * 
 * @author Mark Paluch
 */
@Getter
public class HierarchicalBenchmarkDescriptor implements BenchmarkDescriptor {

	private final BenchmarkDescriptor descriptor;
	private final List<? extends BenchmarkDescriptor> children;

	HierarchicalBenchmarkDescriptor(BenchmarkDescriptor descriptor, List<? extends BenchmarkDescriptor> children) {
		this.descriptor = descriptor;
		this.children = Collections.unmodifiableList(children);
	}

	/**
	 * Create a {@link HierarchicalBenchmarkDescriptor} without children.
	 * 
	 * @param descriptor
	 * @return
	 */
	public static HierarchicalBenchmarkDescriptor create(BenchmarkDescriptor descriptor) {

		Objects.requireNonNull(descriptor, "BenchmarkDescriptor must not be null!");

		return new HierarchicalBenchmarkDescriptor(descriptor, Collections.emptyList());
	}

	/**
	 * Create a {@link HierarchicalBenchmarkDescriptor} with children.
	 * 
	 * @param descriptor
	 * @param children
	 * @return
	 */
	public static HierarchicalBenchmarkDescriptor create(BenchmarkDescriptor descriptor,
			Collection<BenchmarkDescriptor> children) {

		Objects.requireNonNull(descriptor, "BenchmarkDescriptor must not be null!");
		Objects.requireNonNull(children, "Children must not be null!");

		return new HierarchicalBenchmarkDescriptor(descriptor, new ArrayList<>(children));
	}
}
