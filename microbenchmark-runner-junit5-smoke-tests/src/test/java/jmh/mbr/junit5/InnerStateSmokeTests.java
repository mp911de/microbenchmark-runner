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
import org.openjdk.jmh.annotations.AuxCounters;
import org.openjdk.jmh.annotations.AuxCounters.Type;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.Measurement;
import org.openjdk.jmh.annotations.Param;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.annotations.Warmup;

@Warmup(iterations = 1, time = 100, timeUnit = TimeUnit.MILLISECONDS)
@Measurement(iterations = 1, time = 100, timeUnit = TimeUnit.MILLISECONDS)
@Microbenchmark
public class InnerStateSmokeTests {

	@Benchmark
	@Testable
	public double log1p(MainState state) {
		return Math.log1p(state.getValue());
	}

	@State(Scope.Thread)
	@AuxCounters(Type.EVENTS)
	public static class MainState {

		public static enum Sample {
			one, two;
		}

		@Param
		private Sample sample = Sample.one;

		public double getValue() {
			return sample == Sample.one ? Math.PI : Math.E;
		}

	}
}
