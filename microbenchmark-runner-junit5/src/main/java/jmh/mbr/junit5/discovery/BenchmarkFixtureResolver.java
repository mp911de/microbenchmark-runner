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

import static org.junit.platform.engine.discovery.DiscoverySelectors.*;
import static org.junit.platform.engine.support.discovery.SelectorResolver.Resolution.*;

import jmh.mbr.core.model.BenchmarkFixture;
import jmh.mbr.core.model.BenchmarkMethod;
import jmh.mbr.core.model.ParametrizedBenchmarkMethod;
import jmh.mbr.junit5.descriptor.AbstractBenchmarkDescriptor;
import jmh.mbr.junit5.descriptor.BenchmarkFixtureDescriptor;
import jmh.mbr.junit5.descriptor.ParametrizedBenchmarkMethodDescriptor;

import java.util.Optional;

import org.junit.platform.engine.TestDescriptor;
import org.junit.platform.engine.UniqueId;
import org.junit.platform.engine.discovery.UniqueIdSelector;
import org.junit.platform.engine.support.discovery.SelectorResolver;

/**
 * {@link SelectorResolver} for {@link jmh.mbr.core.model.BenchmarkFixture fixtures}.
 */
class BenchmarkFixtureResolver implements SelectorResolver {

	static final String SEGMENT_TYPE = "fixture";

	@Override
	public Resolution resolve(UniqueIdSelector selector, Context context) {

		UniqueId uniqueId = selector.getUniqueId();
		UniqueId.Segment lastSegment = uniqueId.getLastSegment();

		if (lastSegment.getType().equals(BenchmarkFixtureResolver.SEGMENT_TYPE)) {
			return context
					.<AbstractBenchmarkDescriptor> addToParent(() -> selectUniqueId(uniqueId.removeLastSegment()), parent -> {
						ParametrizedBenchmarkMethodDescriptor methodDescriptor = (ParametrizedBenchmarkMethodDescriptor) parent;
						ParametrizedBenchmarkMethod parametrizedMethod = methodDescriptor.getParametrizedMethod();
						return parametrizedMethod.getChildren().stream() //
								.filter(fixture -> fixture.getDisplayName().equals(lastSegment.getValue())) //
								.findFirst() //
								.flatMap(fixture -> Optional
										.of(createFixtureDescriptor(parent, parametrizedMethod.getDescriptor(), fixture)));
					}).map(descriptor -> Resolution.match(Match.exact(descriptor))).orElse(unresolved());
		}

		return unresolved();
	}

	private BenchmarkFixtureDescriptor createFixtureDescriptor(TestDescriptor parent, BenchmarkMethod method,
			BenchmarkFixture fixture) {
		UniqueId uniqueId = parent.getUniqueId().append(BenchmarkFixtureResolver.SEGMENT_TYPE, fixture.getDisplayName());
		return new BenchmarkFixtureDescriptor(uniqueId, method, fixture);
	}
}
