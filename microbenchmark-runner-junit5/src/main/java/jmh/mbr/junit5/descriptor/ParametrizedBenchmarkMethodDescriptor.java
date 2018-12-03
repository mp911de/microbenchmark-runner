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

import java.lang.reflect.Method;

import jmh.mbr.core.model.BenchmarkMethod;
import jmh.mbr.core.model.MethodAware;
import jmh.mbr.core.model.ParametrizedBenchmarkMethod;
import org.junit.platform.commons.util.ClassUtils;
import org.junit.platform.engine.UniqueId;
import org.junit.platform.engine.support.descriptor.MethodSource;

/**
 * {@link org.junit.platform.engine.TestDescriptor} for a parametrized {@link BenchmarkMethod} with fixtures.
 *
 * @author Mark Paluch
 */
public class ParametrizedBenchmarkMethodDescriptor extends AbstractBenchmarkDescriptor implements MethodAware {

	private final BenchmarkMethod method;
	private final ParametrizedBenchmarkMethod parametrizedMethod;

	public ParametrizedBenchmarkMethodDescriptor(UniqueId uniqueId, ParametrizedBenchmarkMethod parametrizedMethod) {
		this(uniqueId, parametrizedMethod.getDescriptor(), parametrizedMethod);
	}

	private ParametrizedBenchmarkMethodDescriptor(UniqueId uniqueId, BenchmarkMethod benchmarkMethod,
												  ParametrizedBenchmarkMethod parametrizedMethod) {
		super(uniqueId, generateDefaultDisplayName(benchmarkMethod.getMethod()),
				MethodSource.from(benchmarkMethod.getDeclaringClass(), benchmarkMethod.getMethod()));
		this.method = benchmarkMethod;
		this.parametrizedMethod = parametrizedMethod;
	}

	/*
	 * (non-Javadoc)
	 * @see org.junit.platform.engine.TestDescriptor#getType()
	 */
	@Override
	public Type getType() {
		return Type.CONTAINER;
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

	public ParametrizedBenchmarkMethod getParametrizedMethod() {
		return parametrizedMethod;
	}

	public BenchmarkMethod getBenchmarkMethod() {
		return method;
	}

	private static String generateDefaultDisplayName(Method testMethod) {
		return String.format("%s(%s)", testMethod.getName(),
				ClassUtils.nullSafeToString(Class::getSimpleName, testMethod.getParameterTypes()));
	}
}
