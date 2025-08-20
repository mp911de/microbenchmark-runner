/*
 * Copyright 2025 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v20.html
 */
package jmh.mbr.junit5.config;

import org.junit.jupiter.engine.config.DefaultJupiterConfiguration;
import org.junit.platform.engine.ConfigurationParameters;
import org.junit.platform.engine.ExecutionRequest;
import org.junit.platform.engine.reporting.OutputDirectoryProvider;
import org.junit.platform.engine.support.store.Namespace;
import org.junit.platform.engine.support.store.NamespacedHierarchicalStore;

public class DefaultMbrConfiguration extends DefaultJupiterConfiguration implements MbrConfiguration {

	private final NamespacedHierarchicalStore<Namespace> store;

	public DefaultMbrConfiguration(ExecutionRequest executionRequest) {
		this(executionRequest.getConfigurationParameters(), executionRequest.getOutputDirectoryProvider(), executionRequest.getStore());
	}

	public DefaultMbrConfiguration(ConfigurationParameters configurationParameters, OutputDirectoryProvider outputDirectoryProvider, NamespacedHierarchicalStore<Namespace> store) {
		super(configurationParameters, outputDirectoryProvider);
		this.store = store;
	}

	@Override
	public NamespacedHierarchicalStore<Namespace> getStore() {
		return store;
	}
}
