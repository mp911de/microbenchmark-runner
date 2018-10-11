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

import lombok.EqualsAndHashCode;

import java.lang.reflect.Method;
import java.util.List;

/**
 * Represents a parametrized benchmark method along with the actual {@link BenchmarkFixture fixtures}.
 * 
 * @author Mark Paluch
 */
@EqualsAndHashCode(callSuper = true)
public class ParametrizedBenchmarkMethod extends HierarchicalBenchmarkDescriptor implements MethodAware {

	ParametrizedBenchmarkMethod(BenchmarkMethod descriptor, List<BenchmarkFixture> children) {
		super(descriptor, children);
	}

	/* 
	 * (non-Javadoc)
	 * @see jmh.mbr.core.model.HierarchicalBenchmarkDescriptor#getDescriptor()
	 */
	@Override
	public BenchmarkMethod getDescriptor() {
		return (BenchmarkMethod) super.getDescriptor();
	}

	/* 
	 * (non-Javadoc)
	 * @see jmh.mbr.core.model.HierarchicalBenchmarkDescriptor#getChildren()
	 */
	@Override
	public List<BenchmarkFixture> getChildren() {
		return (List) super.getChildren();
	}

	/* 
	 * (non-Javadoc)
	 * @see jmh.mbr.core.model.MethodAware#getMethod()
	 */
	@Override
	public Method getMethod() {
		return getDescriptor().getMethod();
	}

	/* 
	 * (non-Javadoc)
	 * @see jmh.mbr.core.model.MethodAware#isUnderlyingMethod(java.lang.reflect.Method)
	 */
	@Override
	public boolean isUnderlyingMethod(Method method) {
		return getDescriptor().isUnderlyingMethod(method);
	}
}
