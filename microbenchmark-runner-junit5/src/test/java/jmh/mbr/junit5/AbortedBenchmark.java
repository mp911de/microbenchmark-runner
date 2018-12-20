/*
 * Copyright 2018 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v20.html
 */
package jmh.mbr.junit5;

import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.BeforeAll;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.Fork;
import org.openjdk.jmh.annotations.Measurement;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.annotations.Warmup;

@Warmup(iterations = 1, time = 1)
@Measurement(iterations = 1, time = 1)
@Fork(value = 1, warmups = 1)
@State(Scope.Benchmark)
@Microbenchmark
public class AbortedBenchmark {

	@BeforeAll
	public static void init() {
		Assumptions.assumeTrue(false, "Assumption failed");
	}

	@Benchmark
	public void foo() {}
}
