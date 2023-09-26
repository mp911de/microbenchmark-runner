/*
 * Copyright 2015-2019 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v20.html
 */
package jmh.mbr.junit5.descriptor;

import static java.util.stream.Collectors.*;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;

import org.junit.jupiter.api.extension.ExecutableInvoker;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.ExtensionContext.Store.CloseableResource;
import org.junit.jupiter.api.parallel.ExecutionMode;
import org.junit.jupiter.engine.config.JupiterConfiguration;
import org.junit.jupiter.engine.execution.NamespaceAwareStore;
import org.junit.platform.commons.JUnitException;
import org.junit.platform.commons.util.Preconditions;
import org.junit.platform.engine.EngineExecutionListener;
import org.junit.platform.engine.TestDescriptor;
import org.junit.platform.engine.TestTag;
import org.junit.platform.engine.reporting.ReportEntry;
import org.junit.platform.engine.support.hierarchical.Node;
import org.junit.platform.engine.support.store.NamespacedHierarchicalStore;

/**
 * Abstract base class for {@link ExtensionContext}. This class implements {@link AutoCloseable} to close {@link java.io.Closeable} {@link ExtensionValuesStore value stores}.
 *
 * @param <T>
 */
abstract class AbstractExtensionContext<T extends TestDescriptor> implements ExtensionContext, AutoCloseable {

	private static final NamespacedHierarchicalStore.CloseAction<Namespace> CLOSE_RESOURCES = (__, ___, value) -> {
		if (value instanceof CloseableResource) {
			((CloseableResource) value).close();
		}
	};

	private final ExtensionContext parent;
	private final EngineExecutionListener engineExecutionListener;
	private final T benchmarkDescriptor;
	private final Set<String> tags;
	private final JupiterConfiguration configuration;
	private final NamespacedHierarchicalStore<Namespace> valuesStore;

	AbstractExtensionContext(ExtensionContext parent, EngineExecutionListener engineExecutionListener, T benchmarkDescriptor,
							 JupiterConfiguration configuration) {

		Preconditions.notNull(benchmarkDescriptor, "BenchmarkDescriptor must not be null");
		Preconditions.notNull(configuration, "JupiterConfiguration must not be null");

		this.parent = parent;
		this.engineExecutionListener = engineExecutionListener;
		this.benchmarkDescriptor = benchmarkDescriptor;
		this.configuration = configuration;
		this.valuesStore = createStore(parent);

		// @formatter:off
		this.tags = benchmarkDescriptor.getTags().stream()
				.map(TestTag::getName)
				.collect(collectingAndThen(toCollection(LinkedHashSet::new), Collections::unmodifiableSet));
		// @formatter:on
	}

	private NamespacedHierarchicalStore<Namespace> createStore(ExtensionContext parent) {
		NamespacedHierarchicalStore<Namespace> parentStore = null;
		if (parent != null) {
			parentStore = ((AbstractExtensionContext<?>) parent).valuesStore;
		}
		return new NamespacedHierarchicalStore<>(parentStore, CLOSE_RESOURCES);
	}

	@Override
	public void close() {
		this.valuesStore.close();
	}

	@Override
	public String getUniqueId() {
		return getBenchmarkDescriptor().getUniqueId().toString();
	}

	@Override
	public String getDisplayName() {
		return getBenchmarkDescriptor().getDisplayName();
	}

	@Override
	public void publishReportEntry(Map<String, String> values) {
		this.engineExecutionListener.reportingEntryPublished(getBenchmarkDescriptor(), ReportEntry.from(values));
	}

	@Override
	public Optional<ExtensionContext> getParent() {
		return Optional.ofNullable(this.parent);
	}

	@Override
	public ExtensionContext getRoot() {
		if (this.parent != null) {
			return this.parent.getRoot();
		}
		return this;
	}

	T getBenchmarkDescriptor() {
		return this.benchmarkDescriptor;
	}

	@Override
	public Store getStore(Namespace namespace) {
		Preconditions.notNull(namespace, "Namespace must not be null");
		return new NamespaceAwareStore(this.valuesStore, namespace);
	}

	@Override
	public Set<String> getTags() {
		// return modifiable copy
		return new LinkedHashSet<>(this.tags);
	}

	@Override
	public Optional<String> getConfigurationParameter(String key) {
		return this.configuration.getRawConfigurationParameter(key);
	}

	@Override
	public <V> Optional<V> getConfigurationParameter(String key, Function<String, V> transformer) {
		return this.configuration.getRawConfigurationParameter(key, transformer);
	}

	@Override
	public ExecutionMode getExecutionMode() {
		return toJupiterExecutionMode(getPlatformExecutionMode());
	}

	@Override
	public ExecutableInvoker getExecutableInvoker() {
		return new ExecutableInvoker() {
			@Override
			public Object invoke(Method method, Object o) {
				throw new UnsupportedOperationException();
			}

			@Override
			public <T> T invoke(Constructor<T> constructor, Object o) {
				throw new UnsupportedOperationException();
			}
		};
	}

	protected abstract Node.ExecutionMode getPlatformExecutionMode();

	private ExecutionMode toJupiterExecutionMode(Node.ExecutionMode mode) {
		switch (mode) {
			case CONCURRENT:
				return ExecutionMode.CONCURRENT;
			case SAME_THREAD:
				return ExecutionMode.SAME_THREAD;
		}
		throw new JUnitException("Unknown ExecutionMode: " + mode);
	}
}
