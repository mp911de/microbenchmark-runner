/*
 * Copyright 2018 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v20.html
 */
package jmh.mbr.junit;

import jmh.mbr.junit.config.MbrDiscoveryConfiguration;
import org.junit.platform.engine.UniqueId;
import org.junit.platform.engine.support.descriptor.EngineDescriptor;

/**
 * {@link EngineDescriptor} for Microbenchmark Runner.
 */
class MicrobenchmarkEngineDescriptor extends EngineDescriptor {

	private final MbrDiscoveryConfiguration configuration;

	MicrobenchmarkEngineDescriptor(UniqueId uniqueId, MbrDiscoveryConfiguration configuration) {
		super(uniqueId, "Microbenchmark Runner");
		this.configuration = configuration;
	}

	public MbrDiscoveryConfiguration getConfiguration() {
		return configuration;
	}
}
