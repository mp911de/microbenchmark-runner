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

import java.lang.reflect.Method;
import java.util.List;

/**
 * Represents a parametrized benchmark method along with the actual {@link BenchmarkFixture fixtures}.
 */
public class ParametrizedBenchmarkMethod extends HierarchicalBenchmarkDescriptor implements MethodAware {

	ParametrizedBenchmarkMethod(BenchmarkMethod descriptor, List<BenchmarkFixture> children) {
		super(descriptor, children);
	}

	@Override
	public BenchmarkMethod getDescriptor() {
		return (BenchmarkMethod) super.getDescriptor();
	}

	@Override
	public List<BenchmarkFixture> getChildren() {
		return (List) super.getChildren();
	}

	@Override
	public Method getMethod() {
		return getDescriptor().getMethod();
	}

	@Override
	public boolean isUnderlyingMethod(Method method) {
		return getDescriptor().isUnderlyingMethod(method);
	}

	@Override
	public int hashCode() {
		return super.hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		return super.equals(obj);
	}
}
