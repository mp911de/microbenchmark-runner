/*
 * Copyright 2019 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * https://www.eclipse.org/legal/epl-v20.html
 */
package jmh.mbr.junit5;

import java.util.concurrent.TimeUnit;

import org.junit.platform.commons.annotation.Testable;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.Fork;
import org.openjdk.jmh.annotations.Measurement;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.annotations.Warmup;

@Microbenchmark
@State(Scope.Thread)
// @Disabled("Need to get system property aware test here")
public class ResultsWriterSmokeTests {

	double x1 = Math.PI;

	@Benchmark
	@Warmup(iterations = 1, time = 100, timeUnit = TimeUnit.MILLISECONDS)
	@Measurement(iterations = 1, time = 100, timeUnit = TimeUnit.MILLISECONDS)
	@Testable
	public double log() {
		return Math.log(x1);
	}

	@Benchmark
	@Warmup(iterations = 1, time = 100, timeUnit = TimeUnit.MILLISECONDS)
	@Measurement(iterations = 5, time = 100, timeUnit = TimeUnit.MILLISECONDS)
	@Testable
	public double log10() {
		return Math.log10(x1);
	}

	@Benchmark
	@Warmup(iterations = 1, time = 100, timeUnit = TimeUnit.MILLISECONDS)
	@Measurement(iterations = 5, time = 100, timeUnit = TimeUnit.MILLISECONDS)
	@Testable
	public double log1p() {
		return Math.log1p(x1);
	}
}
