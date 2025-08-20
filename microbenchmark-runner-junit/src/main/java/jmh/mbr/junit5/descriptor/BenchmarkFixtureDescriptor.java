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

import java.util.Set;

import jmh.mbr.core.model.BenchmarkFixture;
import jmh.mbr.core.model.BenchmarkMethod;
import jmh.mbr.junit5.config.MbrConfiguration;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.engine.extension.ExtensionRegistry;
import org.junit.jupiter.engine.extension.MutableExtensionRegistry;
import org.junit.platform.engine.EngineExecutionListener;
import org.junit.platform.engine.TestTag;
import org.junit.platform.engine.UniqueId;
import org.junit.platform.engine.support.descriptor.MethodSource;

/**
 * {@link org.junit.platform.engine.TestDescriptor} for a {@link BenchmarkFixture}.
 */
public class BenchmarkFixtureDescriptor extends AbstractBenchmarkDescriptor {

	private final BenchmarkFixture fixture;
	private final Set<TestTag> tags;

	public BenchmarkFixtureDescriptor(UniqueId uniqueId, BenchmarkMethod method, BenchmarkFixture fixture) {
		super(uniqueId, fixture.getDisplayName(), MethodSource.from(method.getMethod()));
		this.fixture = fixture;
		this.tags = DescriptorUtils.getTags(method.getDeclaringClass());
	}

	@Override
	public Set<TestTag> getTags() {
		return tags;
	}

	@Override
	public Type getType() {
		return Type.TEST;
	}

	@Override
	public ExtensionContext getExtensionContext(ExtensionContext parent, EngineExecutionListener engineExecutionListener, MbrConfiguration configuration) {
		throw new UnsupportedOperationException();
	}

	@Override
	public ExtensionRegistry getExtensionRegistry(MutableExtensionRegistry parent) {
		throw new UnsupportedOperationException();
	}

	public BenchmarkFixture getFixture() {
		return fixture;
	}
}
