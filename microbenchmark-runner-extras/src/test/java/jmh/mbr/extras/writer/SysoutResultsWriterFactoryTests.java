/*
 * Copyright 2019 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v20.html
 */
package jmh.mbr.extras.writer;

import static org.assertj.core.api.Assertions.*;

import java.io.ByteArrayOutputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.concurrent.TimeUnit;

import jmh.mbr.core.ResultsWriter;
import jmh.mbr.core.model.BenchmarkResults;
import jmh.mbr.core.model.BenchmarkResults.MetaData;
import org.junit.jupiter.api.Test;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.infra.BenchmarkParams;
import org.openjdk.jmh.results.AverageTimeResult;
import org.openjdk.jmh.results.BenchmarkResult;
import org.openjdk.jmh.results.IterationResult;
import org.openjdk.jmh.results.ResultRole;
import org.openjdk.jmh.results.RunResult;
import org.openjdk.jmh.runner.WorkloadParams;
import org.openjdk.jmh.runner.format.OutputFormat;
import org.openjdk.jmh.runner.format.OutputFormatFactory;
import org.openjdk.jmh.runner.options.TimeValue;
import org.openjdk.jmh.runner.options.VerboseMode;
import org.openjdk.jmh.util.ScoreFormatter;

class SysoutResultsWriterFactoryTests {

	private SysoutCsvResultsWriterFactory factory = new SysoutCsvResultsWriterFactory();

	@Test
	void emptyUri() {
		ResultsWriter writer = factory.forUri(null);
		assertThat(writer).isNotNull();
	}

	@Test
	void sysoutEnablesStdOut() {
		ResultsWriter writer = factory.forUri("sysout");
		assertThat(writer).isNotNull();
	}

	@Test
	void noneDisablesStdOut() {
		ResultsWriter writer = factory.forUri("none");
		assertThat(writer).isNull();
	}

	@Test
	void writeActualResults() {
		BenchmarkParams params = params();
		Collection<BenchmarkResult> data = Collections
				.singletonList(new BenchmarkResult(null, data()));
		RunResult runResult = new RunResult(params, data);
		String result = output(runResult);
		assertThat(result)
				.containsSubsequence("class, method, median, mean, range", "Foo, exec");
	}

	@Test
	void writeParamResult() {
		BenchmarkParams params = params("a=b");
		Collection<BenchmarkResult> data = Collections
				.singletonList(new BenchmarkResult(null, data()));
		RunResult runResult = new RunResult(params, data);
		String result = output(runResult);
		assertThat(result)
				.containsSubsequence("class, method, a, median, mean, range", "Foo, exec, b");
	}

	@Test
	void writeSecondaryResult() {
		BenchmarkParams params = params("a=b");
		Collection<BenchmarkResult> data = Collections
				.singletonList(new BenchmarkResult(null, data("bar")));
		RunResult runResult = new RunResult(params, data);
		String result = output(runResult);
		assertThat(result)
				.containsSubsequence("class, method, a, bar, median, mean, range", "Foo, exec, b");
	}

	@Test
	void writeMixedParamResults() {
		Collection<BenchmarkResult> data = Collections
				.singletonList(new BenchmarkResult(null, data()));
		String result = output(new RunResult(params("a=b"), data), new RunResult(params("a=e", "c=d"), data));
		assertThat(result)
				.containsSubsequence("class, method, a, c, median, mean, range", "Foo, exec, b, ,", "e, d");
	}

	@Test
	void writeMixedSecondaryResults() {
		Collection<BenchmarkResult> data = Collections
				.singletonList(new BenchmarkResult(null, data()));
		Collection<BenchmarkResult> seconds = Collections
				.singletonList(new BenchmarkResult(null, data("bar", "spam")));
		String result = output(new RunResult(params(), data), new RunResult(params(), seconds));
		assertThat(result)
				.containsSubsequence("class, method, bar, spam, median, mean, range", "Foo, exec, , ",
						"Foo, exec, " + ScoreFormatter.format(1.000));
	}

	private BenchmarkParams params(String... workloads) {
		WorkloadParams workload = new WorkloadParams();
		for (int i = 0; i < workloads.length; i++) {
			String pair = workloads[i];
			String key = pair;
			String value = "";
			if (pair.contains("=")) {
				key = pair.split("=")[0];
				value = pair.split("=")[1].trim();
				workload.put(key, value, i);
			}
		}
		return new BenchmarkParams("com.example.Foo.exec", "bar", true, 1, new int[]{1}, Collections
				.singletonList("thread"), 1, 0,
				null, null, Mode.AverageTime, workload, TimeUnit.MILLISECONDS, 1, "", Collections
				.emptyList(), "1.8", "JDK",
				"1.8", "1.21", TimeValue.NONE);
	}

	private static Collection<IterationResult> data(String... seconds) {
		IterationResult result = new IterationResult(null, null, null);
		result.addResult(new AverageTimeResult(ResultRole.PRIMARY, "foo", 15, 300000000, TimeUnit.MILLISECONDS));
		for (String label : seconds) {
			result.addResult(new AverageTimeResult(ResultRole.SECONDARY, label, 1, 1000000, TimeUnit.MILLISECONDS));
		}
		return Collections.singletonList(result);
	}

	private String output(RunResult... runResult) {
		OutputStream stream = new ByteArrayOutputStream();
		OutputFormat output = OutputFormatFactory
				.createFormatInstance(new PrintStream(stream), VerboseMode.NORMAL);
		factory.forUri("sysout").write(output, new BenchmarkResults(MetaData.none(), Arrays.asList(runResult)));
		return stream.toString();
	}
}
