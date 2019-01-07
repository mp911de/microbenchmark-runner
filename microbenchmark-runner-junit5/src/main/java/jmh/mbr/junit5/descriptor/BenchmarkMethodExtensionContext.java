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
import java.util.Optional;

import jmh.mbr.core.model.MethodAware;
import org.junit.jupiter.api.TestInstance.Lifecycle;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.platform.engine.ConfigurationParameters;
import org.junit.platform.engine.EngineExecutionListener;

/**
 * {@link ExtensionContext} for a {@link AbstractBenchmarkDescriptor} that is {@link MethodAware}.
 */
class BenchmarkMethodExtensionContext extends AbstractExtensionContext<AbstractBenchmarkDescriptor> {

	private MethodAware methodAware;

	BenchmarkMethodExtensionContext(ExtensionContext parent, EngineExecutionListener engineExecutionListener, MethodAware methodAware, ConfigurationParameters configurationParameters) {
		super(parent, engineExecutionListener, (AbstractBenchmarkDescriptor) methodAware, configurationParameters);
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
	public Optional<Method> getTestMethod() {
		return Optional.of(methodAware.getMethod());
	}

	@Override
	public Optional<Throwable> getExecutionException() {
		return Optional.empty();
	}
}
