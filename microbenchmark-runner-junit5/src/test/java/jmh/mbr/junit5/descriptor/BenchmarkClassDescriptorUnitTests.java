/*
 * Copyright 2018 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v20.html
 */
package jmh.mbr.junit5.descriptor;

import static org.assertj.core.api.Assertions.*;

import java.util.Collections;

import jmh.mbr.core.model.BenchmarkClass;
import jmh.mbr.junit5.ParametrizedBenchmark;
import org.junit.jupiter.api.Test;
import org.junit.platform.engine.UniqueId;
import org.junit.platform.launcher.PostDiscoveryFilter;
import org.junit.platform.launcher.TagFilter;

/**
 * Unit tests for {@link BenchmarkClassDescriptor}.
 */
class BenchmarkClassDescriptorUnitTests {

	@Test
	void shouldApplyTagFilter() {

		PostDiscoveryFilter includeFoo = TagFilter.includeTags("foo");
		PostDiscoveryFilter includeBar = TagFilter.includeTags("bar");

		BenchmarkClass benchmarkClass = BenchmarkClass.create(ParametrizedBenchmark.class, Collections.emptyList());
		BenchmarkClassDescriptor descriptor = new BenchmarkClassDescriptor(UniqueId.root("root", "root"), benchmarkClass);

		assertThat(includeFoo.apply(descriptor).included()).isTrue();
		assertThat(includeBar.apply(descriptor).included()).isFalse();
	}
}
