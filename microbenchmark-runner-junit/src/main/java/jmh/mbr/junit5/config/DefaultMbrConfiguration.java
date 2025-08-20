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
import org.junit.platform.engine.support.discovery.DiscoveryIssueReporter;
import org.junit.platform.engine.support.store.Namespace;
import org.junit.platform.engine.support.store.NamespacedHierarchicalStore;

public class DefaultMbrConfiguration extends DefaultJupiterConfiguration implements MbrConfiguration, MbrDiscoveryConfiguration {

	private final ConfigurationParameters configurationParameters;
	private final OutputDirectoryProvider outputDirectoryProvider;
	private final DiscoveryIssueReporter issueReporter;
	private final NamespacedHierarchicalStore<Namespace> store;

	public DefaultMbrConfiguration(ExecutionRequest executionRequest) {
		this(executionRequest, DiscoveryIssueReporter.consuming(haha -> {
		}));
	}

	public DefaultMbrConfiguration(ExecutionRequest executionRequest, DiscoveryIssueReporter issueReporter) {
		this(executionRequest.getConfigurationParameters(), executionRequest.getOutputDirectoryProvider(), issueReporter, executionRequest.getStore());
	}

	public DefaultMbrConfiguration(ConfigurationParameters configurationParameters, OutputDirectoryProvider outputDirectoryProvider, DiscoveryIssueReporter issueReporter) {
		this(configurationParameters, outputDirectoryProvider, issueReporter, new NamespacedHierarchicalStore<>(null));
	}

	public DefaultMbrConfiguration(ConfigurationParameters configurationParameters, OutputDirectoryProvider outputDirectoryProvider, DiscoveryIssueReporter issueReporter, NamespacedHierarchicalStore<Namespace> store) {
		super(configurationParameters, outputDirectoryProvider, issueReporter);
		this.configurationParameters = configurationParameters;
		this.outputDirectoryProvider = outputDirectoryProvider;
		this.issueReporter = issueReporter;
		this.store = store;
	}

	@Override
	public MbrConfiguration withStore(NamespacedHierarchicalStore<Namespace> store) {
		return new DefaultMbrConfiguration(configurationParameters, outputDirectoryProvider, issueReporter, store);
	}

	@Override
	public NamespacedHierarchicalStore<Namespace> getStore() {
		return store;
	}
}
