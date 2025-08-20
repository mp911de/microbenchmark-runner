/*
 * Copyright 2018-2019 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v20.html
 */
package jmh.mbr.junit5.descriptor;

import java.lang.reflect.Method;

import jmh.mbr.core.model.BenchmarkMethod;
import jmh.mbr.core.model.MethodAware;
import jmh.mbr.core.model.ParametrizedBenchmarkMethod;
import jmh.mbr.junit5.config.MbrConfiguration;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.engine.extension.ExtensionRegistry;
import org.junit.jupiter.engine.extension.MutableExtensionRegistry;
import org.junit.platform.commons.util.ClassUtils;
import org.junit.platform.engine.EngineExecutionListener;
import org.junit.platform.engine.UniqueId;
import org.junit.platform.engine.support.descriptor.MethodSource;

/**
 * {@link org.junit.platform.engine.TestDescriptor} for a parametrized {@link BenchmarkMethod} with fixtures.
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
				MethodSource.from(benchmarkMethod.getDeclaringClass(), benchmarkMethod
						.getMethod()));
		this.method = benchmarkMethod;
		this.parametrizedMethod = parametrizedMethod;
	}

	@Override
	public Type getType() {
		return Type.CONTAINER;
	}

	@Override
	public String getLegacyReportingName() {
		return generateDefaultDisplayName(method.getMethod());
	}

	@Override
	public Method getMethod() {
		return method.getMethod();
	}

	@Override
	public boolean isUnderlyingMethod(Method method) {
		return this.method.getMethod().equals(method);
	}

	@Override
	public ExtensionContext getExtensionContext(ExtensionContext parent, EngineExecutionListener engineExecutionListener, MbrConfiguration configuration) {
		return new BenchmarkMethodExtensionContext(parent, engineExecutionListener, this, configuration, this);
	}

	@Override
	public ExtensionRegistry getExtensionRegistry(MutableExtensionRegistry parent) {
		return ExtensionUtils
				.populateNewExtensionRegistryFromExtendWithAnnotation(parent, getMethod());
	}

	public ParametrizedBenchmarkMethod getParametrizedMethod() {
		return parametrizedMethod;
	}

	public BenchmarkMethod getBenchmarkMethod() {
		return method;
	}

	private static String generateDefaultDisplayName(Method testMethod) {
		return String.format("%s(%s)", testMethod.getName(),
				ClassUtils.nullSafeToString(Class::getSimpleName, testMethod
						.getParameterTypes()));
	}
}
