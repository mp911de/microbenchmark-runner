/*
 * Copyright 2018-2019 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v20.html
 */
package jmh.mbr.junit5;

import java.util.Optional;

import jmh.mbr.junit5.config.DefaultMbrConfiguration;
import jmh.mbr.junit5.config.MbrDiscoveryConfiguration;
import jmh.mbr.junit5.discovery.DiscoverySelectorResolver;
import jmh.mbr.junit5.execution.JmhRunner;
import org.junit.jupiter.engine.extension.MutableExtensionRegistry;
import org.junit.platform.engine.EngineDiscoveryRequest;
import org.junit.platform.engine.ExecutionRequest;
import org.junit.platform.engine.TestDescriptor;
import org.junit.platform.engine.TestEngine;
import org.junit.platform.engine.UniqueId;
import org.junit.platform.engine.support.discovery.DiscoveryIssueReporter;

/**
 * Microbenchmark Runner Engine.
 */
public class MicrobenchmarkEngine implements TestEngine {

	public static final String ENGINE_ID = "microbenchmark-engine";

	@Override
	public String getId() {
		return ENGINE_ID;
	}

	@Override
	public TestDescriptor discover(EngineDiscoveryRequest discoveryRequest, UniqueId uniqueId) {

		DiscoveryIssueReporter issueReporter = DiscoveryIssueReporter.deduplicating(
				DiscoveryIssueReporter.forwarding(discoveryRequest.getDiscoveryListener(), uniqueId));

		MbrDiscoveryConfiguration configuration = new DefaultMbrConfiguration(discoveryRequest.getConfigurationParameters(), discoveryRequest.getOutputDirectoryProvider(), issueReporter);

		MicrobenchmarkEngineDescriptor engineDescriptor = new MicrobenchmarkEngineDescriptor(uniqueId, configuration);
		new DiscoverySelectorResolver()
				.resolveSelectors(discoveryRequest, engineDescriptor);
		return engineDescriptor;
	}

	@Override
	public void execute(ExecutionRequest request) {

		MicrobenchmarkEngineDescriptor rootTestDescriptor = (MicrobenchmarkEngineDescriptor) request.getRootTestDescriptor();

		MutableExtensionRegistry extensionRegistry = MutableExtensionRegistry
				.createRegistryWithDefaultExtensions(rootTestDescriptor.getConfiguration());

		new JmhRunner(rootTestDescriptor.getConfiguration()
				.withStore(request.getStore()), extensionRegistry)
				.execute(rootTestDescriptor, request
						.getEngineExecutionListener());
	}

	@Override
	public Optional<String> getGroupId() {
		return Optional.of("com.github.mp911de.microbenchmark-runner");
	}

	@Override
	public Optional<String> getArtifactId() {
		return Optional.of("microbenchmark-runner-junit5");
	}
}
