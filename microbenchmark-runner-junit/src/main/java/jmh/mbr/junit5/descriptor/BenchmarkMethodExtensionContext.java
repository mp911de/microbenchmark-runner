/*
 * Copyright 2019 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v20.html
 */
package jmh.mbr.junit5.descriptor;

import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Method;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import jmh.mbr.core.model.MethodAware;
import jmh.mbr.junit5.config.MbrConfiguration;
import org.junit.jupiter.api.TestInstance.Lifecycle;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.TestInstances;
import org.junit.platform.engine.EngineExecutionListener;
import org.junit.platform.engine.TestDescriptor;
import org.junit.platform.engine.support.hierarchical.Node.ExecutionMode;

/**
 * {@link ExtensionContext} for a {@link AbstractBenchmarkDescriptor} that is {@link MethodAware}.
 */
class BenchmarkMethodExtensionContext extends AbstractExtensionContext<AbstractBenchmarkDescriptor> {

	private final MethodAware methodAware;

	BenchmarkMethodExtensionContext(ExtensionContext parent, EngineExecutionListener engineExecutionListener, MethodAware methodAware, MbrConfiguration configuration, TestDescriptor testDescriptor) {
		super(parent, engineExecutionListener, (AbstractBenchmarkDescriptor) methodAware, configuration, new LauncherStoreFacade(configuration.getStore()));
		this.methodAware = methodAware;
	}

	@Override
	public Optional<AnnotatedElement> getElement() {
		return Optional.of(methodAware.getMethod());
	}

	@Override
	public Optional<Class<?>> getTestClass() {
		return Optional.of(methodAware.getMethod().getDeclaringClass());
	}

	@Override
	public Optional<Lifecycle> getTestInstanceLifecycle() {
		return Optional.empty();
	}

	@Override
	public Optional<Object> getTestInstance() {
		return Optional.empty();
	}

	@Override
	public Optional<TestInstances> getTestInstances() {
		return Optional.empty();
	}

	@Override
	public Optional<Method> getTestMethod() {
		return Optional.of(methodAware.getMethod());
	}

	@Override
	public Optional<Throwable> getExecutionException() {
		return Optional.empty();
	}

	@Override
	protected ExecutionMode getPlatformExecutionMode() {
		return ExecutionMode.SAME_THREAD;
	}

	@Override
	public List<Class<?>> getEnclosingTestClasses() {
		return Collections.emptyList();
	}
}
