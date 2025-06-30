/*
 * Copyright 2018 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v20.html
 */
package jmh.mbr.junit5.discovery;

import jmh.mbr.junit5.discovery.predicates.IsBenchmarkClass;

import org.junit.platform.engine.EngineDiscoveryRequest;
import org.junit.platform.engine.TestDescriptor;
import org.junit.platform.engine.support.discovery.EngineDiscoveryRequestResolver;

/**
 * {@link EngineDiscoveryRequestResolver}-based discovery mechanism. Resolves {@link TestDescriptor descriptors} by
 * introspecting classes, methods, and {@link org.junit.platform.engine.UniqueId}.
 */
public class DiscoverySelectorResolver {

	private static final EngineDiscoveryRequestResolver<TestDescriptor> resolver = EngineDiscoveryRequestResolver
			.builder() //
			.addClassContainerSelectorResolver(IsBenchmarkClass.INSTANCE)
			.addSelectorResolver(ctx -> new BenchmarkContainerResolver(ctx.getClassNameFilter())) //
			.addSelectorResolver(new BenchmarkMethodResolver()) //
			.addSelectorResolver(new BenchmarkFixtureResolver()) //
			.build();

	public void resolveSelectors(EngineDiscoveryRequest request, TestDescriptor engineDescriptor) {
		resolver.resolve(request, engineDescriptor);
	}

}
