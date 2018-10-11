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
package jmh.mbr.junit5.descriptor;

import jmh.mbr.core.model.BenchmarkMethod;
import jmh.mbr.core.model.MethodAware;

import java.lang.reflect.Method;

import org.junit.platform.commons.util.ClassUtils;
import org.junit.platform.engine.UniqueId;
import org.junit.platform.engine.support.descriptor.AbstractTestDescriptor;
import org.junit.platform.engine.support.descriptor.MethodSource;

/**
 * {@link org.junit.platform.engine.TestDescriptor} for a {@link BenchmarkMethod}.
 * 
 * @author Mark Paluch
 */
public class BenchmarkMethodDescriptor extends AbstractTestDescriptor implements MethodAware {

	private final BenchmarkMethod method;

	public BenchmarkMethodDescriptor(UniqueId uniqueId, BenchmarkMethod benchmarkMethod) {
		super(uniqueId, generateDefaultDisplayName(benchmarkMethod),
				MethodSource.from(benchmarkMethod.getDeclaringClass(), benchmarkMethod.getMethod()));
		this.method = benchmarkMethod;
	}

	/* 
	 * (non-Javadoc)
	 * @see org.junit.platform.engine.TestDescriptor#getType()
	 */
	@Override
	public Type getType() {
		return Type.TEST;
	}

	/* 
	 * (non-Javadoc)
	 * @see org.junit.platform.engine.TestDescriptor#getLegacyReportingName()
	 */
	@Override
	public String getLegacyReportingName() {
		return generateDefaultDisplayName(method.getMethod());
	}

	/* 
	 * (non-Javadoc)
	 * @see jmh.mbr.core.model.MethodAware#getMethod()
	 */
	@Override
	public Method getMethod() {
		return method.getMethod();
	}

	/* 
	 * (non-Javadoc)
	 * @see jmh.mbr.core.model.MethodAware#isUnderlyingMethod(java.lang.reflect.Method)
	 */
	@Override
	public boolean isUnderlyingMethod(Method method) {
		return this.method.getMethod().equals(method);
	}

	public BenchmarkMethod getBenchmarkMethod() {
		return method;
	}

	public static UniqueId createUniqueId(UniqueId uniqueId, BenchmarkMethod benchmarkMethod) {
		return uniqueId.append("method", describeMethodId(benchmarkMethod.getMethod()));
	}

	public static String describeMethodId(Method method) {
		return String.format("%s(%s)", method.getName(), ClassUtils.nullSafeToString(method.getParameterTypes()));
	}

	private static String generateDefaultDisplayName(BenchmarkMethod benchmarkMethod) {
		return generateDefaultDisplayName(benchmarkMethod.getMethod());
	}

	private static String generateDefaultDisplayName(Method method) {
		return String.format("%s(%s)", method.getName(),
				ClassUtils.nullSafeToString(Class::getSimpleName, method.getParameterTypes()));
	}
}
