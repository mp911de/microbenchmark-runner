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
package jmh.mbr.junit5.discovery.predicates;

import static org.assertj.core.api.Assertions.*;

import java.util.function.Predicate;

import org.junit.jupiter.api.Test;
import org.openjdk.jmh.annotations.Benchmark;

/**
 * Unit tests for {@link IsBenchmarkClass}.
 * 
 * @author Mark Paluch
 */
public class IsBenchmarkClassUnitTests {

	@Test
	void shouldDiscoverBenchmark() {

		assertThat((Predicate<Class<?>>) IsBenchmarkClass.INSTANCE).accepts(WithBenchmark.class);
		assertThat((Predicate<Class<?>>) IsBenchmarkClass.INSTANCE).rejects(NoBenchmark.class);
	}

	static class NoBenchmark {}

	public static class WithBenchmark {

		@Benchmark
		public void benchmarkMethod() {

		}
	}
}
