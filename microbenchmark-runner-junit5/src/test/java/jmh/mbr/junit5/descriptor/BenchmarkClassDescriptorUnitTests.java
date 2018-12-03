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
 *
 * @author Mark Paluch
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
