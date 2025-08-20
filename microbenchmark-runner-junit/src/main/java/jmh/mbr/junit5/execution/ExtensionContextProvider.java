/*
 * Copyright 2019 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v20.html
 */
package jmh.mbr.junit5.execution;

import java.io.Closeable;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

import jmh.mbr.junit5.config.MbrConfiguration;
import jmh.mbr.junit5.descriptor.AbstractBenchmarkDescriptor;
import jmh.mbr.junit5.descriptor.BenchmarkClassDescriptor;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.platform.commons.logging.Logger;
import org.junit.platform.commons.logging.LoggerFactory;
import org.junit.platform.engine.EngineExecutionListener;
import org.junit.platform.engine.TestDescriptor;

/**
 * Provides {@link ExtensionContext} for a {@link BenchmarkClassDescriptor}.
 */
class ExtensionContextProvider implements AutoCloseable {

	private static final Logger logger = LoggerFactory.getLogger(ConditionEvaluator.class);

	private final EngineExecutionListener listener;
	private final MbrConfiguration configuration;
	private final Map<AbstractBenchmarkDescriptor, ExtensionContext> contextMap = new LinkedHashMap<>();

	private ExtensionContextProvider(EngineExecutionListener listener, MbrConfiguration configuration) {
		this.listener = listener;
		this.configuration = configuration;
	}

	/**
	 * Creates a new {@link ExtensionContextProvider}.
	 *
	 * @param listener must not be {@literal null}.
	 * @param configuration must not be {@literal null}.
	 * @return the new {@link ExtensionContextProvider}.
	 */
	static ExtensionContextProvider create(EngineExecutionListener listener, MbrConfiguration configuration) {
		return new ExtensionContextProvider(listener, configuration);
	}

	ExtensionContext getExtensionContext(Optional<TestDescriptor> benchmarkClassDescriptor) {

		return benchmarkClassDescriptor.filter(BenchmarkClassDescriptor.class::isInstance)
				.map(BenchmarkClassDescriptor.class::cast)
				.map(this::getExtensionContext)
				.orElse(null);
	}

	ExtensionContext getExtensionContext(BenchmarkClassDescriptor descriptor) {
		return contextMap.computeIfAbsent(descriptor, key -> key.getExtensionContext(null, listener, configuration));
	}

	@Override
	public void close() {

		contextMap.values()
				.stream()
				.filter(Closeable.class::isInstance)
				.map(Closeable.class::cast)
				.forEach(closeable -> {
					try {
						closeable.close();
					} catch (IOException e) {
						logger.error(e, () -> String.format("Cannot close context [%s]", closeable));
					}
				});
	}
}
