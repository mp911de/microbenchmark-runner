/*
 * Copyright 2019 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v20.html
 */
package jmh.mbr.core;

import java.io.IOException;
import java.util.Collection;
import java.util.Collections;

import org.junit.jupiter.api.Test;
import org.openjdk.jmh.infra.BenchmarkParams;
import org.openjdk.jmh.infra.IterationParams;
import org.openjdk.jmh.results.BenchmarkResult;
import org.openjdk.jmh.results.IterationResult;
import org.openjdk.jmh.results.RunResult;
import org.openjdk.jmh.runner.format.OutputFormat;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for {@link JmhSupport}.
 */
class JmhSupportUnitTests {

	@Test
	void shouldConsiderCompositeResultWriterUri() {

		TestResultsWriterFactory.REGISTRY.put("foo", FooResultWriter::new);
		TestResultsWriterFactory.REGISTRY.put("bar", BarResultWriter::new);

		System.setProperty("publishTo", "foo,bar,none");

		try {
			JmhSupport support = new JmhSupport();
			RunResult runResult = new RunResult(null, Collections.emptyList());
			support.publishResults(SilentOutputFormat.INSTANCE, Collections.singleton(runResult));
		} finally {
			System.clearProperty("publishTo");
			TestResultsWriterFactory.REGISTRY.remove("foo");
			TestResultsWriterFactory.REGISTRY.remove("bar");
		}

		assertThat(FooResultWriter.written).isTrue();
		assertThat(BarResultWriter.written).isTrue();
	}

	@Test
	void shouldBeAbleToWriteToEmptyUri() {

		TestResultsWriterFactory.REGISTRY.put("", FooResultWriter::new);

		try {
			JmhSupport support = new JmhSupport();
			RunResult runResult = new RunResult(null, Collections.emptyList());
			support.publishResults(SilentOutputFormat.INSTANCE, Collections.singleton(runResult));
		} finally {
			TestResultsWriterFactory.REGISTRY.remove("");
		}

		assertThat(FooResultWriter.written).isTrue();
	}

	static class FooResultWriter implements ResultsWriter {

		static boolean written = false;

		@Override
		public void write(OutputFormat output, Collection<RunResult> results) {
			written = true;
		}
	}

	static class BarResultWriter implements ResultsWriter {

		static boolean written = false;

		@Override
		public void write(OutputFormat output, Collection<RunResult> results) {
			written = true;
		}
	}

	enum SilentOutputFormat implements OutputFormat {

		INSTANCE;

		@Override
		public void iteration(BenchmarkParams benchParams, IterationParams params, int iteration) {

		}

		@Override
		public void iterationResult(BenchmarkParams benchParams, IterationParams params, int iteration,
				IterationResult data) {

		}

		@Override
		public void startBenchmark(BenchmarkParams benchParams) {

		}

		@Override
		public void endBenchmark(BenchmarkResult result) {

		}

		@Override
		public void startRun() {

		}

		@Override
		public void endRun(Collection<RunResult> result) {

		}

		@Override
		public void print(String s) {

		}

		@Override
		public void println(String s) {

		}

		@Override
		public void flush() {

		}

		@Override
		public void close() {

		}

		@Override
		public void verbosePrintln(String s) {

		}

		@Override
		public void write(int b) {

		}

		@Override
		public void write(byte[] b) throws IOException {

		}
	}
}
