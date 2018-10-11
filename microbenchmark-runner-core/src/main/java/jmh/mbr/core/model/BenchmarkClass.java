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

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;

/**
 * {@link BenchmarkDescriptor} for a single {@link Class benchmark class} along its children.
 * 
 * @author Mark Paluch
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
	 * @param benchmarkClass
	 * @param children
	 * @return
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
