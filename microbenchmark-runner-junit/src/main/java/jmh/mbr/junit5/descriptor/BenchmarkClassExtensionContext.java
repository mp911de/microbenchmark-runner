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

import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Method;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import jmh.mbr.junit5.config.MbrConfiguration;
import org.junit.jupiter.api.TestInstance.Lifecycle;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.TestInstances;
import org.junit.platform.engine.EngineExecutionListener;
import org.junit.platform.engine.support.hierarchical.Node.ExecutionMode;

/**
 * {@link ExtensionContext} for a {@link BenchmarkClassDescriptor}.
 */
class BenchmarkClassExtensionContext extends AbstractExtensionContext<BenchmarkClassDescriptor> {

	BenchmarkClassExtensionContext(ExtensionContext parent, EngineExecutionListener engineExecutionListener, BenchmarkClassDescriptor testDescriptor, MbrConfiguration configuration) {
		super(parent, engineExecutionListener, testDescriptor, configuration, new LauncherStoreFacade(configuration.getStore()));
	}

	@Override
	public Optional<AnnotatedElement> getElement() {
		return Optional.of(getBenchmarkDescriptor().getJavaClass());
	}

	@Override
	public Optional<Class<?>> getTestClass() {
		return Optional.of(getBenchmarkDescriptor().getJavaClass());
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
		return Optional.empty();
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
