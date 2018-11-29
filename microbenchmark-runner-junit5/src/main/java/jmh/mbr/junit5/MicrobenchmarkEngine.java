/*
 * Copyright 2018 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
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
 *
 * @author Mark Paluch
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
