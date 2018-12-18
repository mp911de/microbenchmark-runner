/*
 * Copyright 2018 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v20.html
 */
package jmh.mbr.junit5.discovery.predicates;

import static org.assertj.core.api.Assertions.*;

import java.util.function.Predicate;

import org.junit.jupiter.api.Test;
import org.openjdk.jmh.annotations.Benchmark;

/**
 * Unit tests for {@link IsBenchmarkClass}.
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
