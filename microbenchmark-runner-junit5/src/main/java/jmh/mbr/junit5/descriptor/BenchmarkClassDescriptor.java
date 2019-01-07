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

import jmh.mbr.core.model.BenchmarkClass;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.engine.extension.ExtensionRegistry;
import org.junit.platform.engine.ConfigurationParameters;
import org.junit.platform.engine.EngineExecutionListener;
import org.junit.platform.engine.UniqueId;
import org.junit.platform.engine.support.descriptor.ClassSource;

/**
 * {@link org.junit.platform.engine.TestDescriptor} for a {@link BenchmarkClass}.
 */
public class BenchmarkClassDescriptor extends AbstractBenchmarkDescriptor {

	private final BenchmarkClass benchmarkClass;

	public BenchmarkClassDescriptor(UniqueId uniqueId, BenchmarkClass benchmarkClass) {

		super(uniqueId, benchmarkClass.getJavaClass().getName(), ClassSource.from(benchmarkClass.getJavaClass()));
		this.benchmarkClass = benchmarkClass;
	}

	@Override
	public Type getType() {
		return Type.CONTAINER;
	}

	public BenchmarkClass getBenchmarkClass() {
		return benchmarkClass;
	}

	public Class<?> getJavaClass() {
		return benchmarkClass.getJavaClass();
	}

	@Override
	public ExtensionContext getExtensionContext(ExtensionContext parent, EngineExecutionListener engineExecutionListener, ConfigurationParameters configurationParameters) {
		return new BenchmarkClassExtensionContext(parent, engineExecutionListener, this, configurationParameters);
	}

	@Override
	public ExtensionRegistry getExtensionRegistry(ExtensionRegistry parent) {
		return ExtensionUtils.populateNewExtensionRegistryFromExtendWithAnnotation(parent, getJavaClass());
	}
}
