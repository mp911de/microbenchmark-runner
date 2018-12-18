/*
 * Copyright 2018 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v20.html
 */
package jmh.mbr.junit5;

import jmh.mbr.junit5.discovery.DiscoverySelectorResolver;

import java.util.Optional;

import jmh.mbr.junit5.execution.JmhRunner;
import org.junit.platform.engine.EngineDiscoveryRequest;
import org.junit.platform.engine.ExecutionRequest;
import org.junit.platform.engine.TestDescriptor;
import org.junit.platform.engine.TestEngine;
import org.junit.platform.engine.UniqueId;

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

		MicrobenchmarkEngineDescriptor engineDescriptor = new MicrobenchmarkEngineDescriptor(uniqueId);
		new DiscoverySelectorResolver().resolveSelectors(discoveryRequest, engineDescriptor);
		return engineDescriptor;
	}

	@Override
	public void execute(ExecutionRequest request) {
		new JmhRunner().execute(request.getRootTestDescriptor(), request.getEngineExecutionListener());
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
