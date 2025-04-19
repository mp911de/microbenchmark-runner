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

import jmh.mbr.core.model.BenchmarkMethod;
import jmh.mbr.core.model.MethodAware;

import java.lang.reflect.Method;

import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.engine.config.JupiterConfiguration;
import org.junit.jupiter.engine.extension.ExtensionRegistry;
import org.junit.jupiter.engine.extension.MutableExtensionRegistry;
import org.junit.platform.commons.util.ClassUtils;
import org.junit.platform.engine.EngineExecutionListener;
import org.junit.platform.engine.UniqueId;
import org.junit.platform.engine.support.descriptor.MethodSource;

/**
 * {@link org.junit.platform.engine.TestDescriptor} for a {@link BenchmarkMethod}.
 */
public class BenchmarkMethodDescriptor extends AbstractBenchmarkDescriptor implements MethodAware {

	private final BenchmarkMethod method;

	public BenchmarkMethodDescriptor(UniqueId uniqueId, BenchmarkMethod method) {
		super(uniqueId, generateDefaultDisplayName(method),
				MethodSource.from(method.getDeclaringClass(), method.getMethod()));
		this.method = method;
	}

	@Override
	public Type getType() {
		return Type.TEST;
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
	public ExtensionContext getExtensionContext(ExtensionContext parent, EngineExecutionListener engineExecutionListener, JupiterConfiguration configuration) {
		return new BenchmarkMethodExtensionContext(parent, engineExecutionListener, this, configuration, this);
	}

	@Override
	public ExtensionRegistry getExtensionRegistry(MutableExtensionRegistry parent) {
		return ExtensionUtils.populateNewExtensionRegistryFromExtendWithAnnotation(parent, getMethod());
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
