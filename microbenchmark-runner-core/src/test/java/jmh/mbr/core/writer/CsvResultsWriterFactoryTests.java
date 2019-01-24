/*
 * Copyright 2016-2017 the original author or authors.
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
package jmh.mbr.core.writer;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.BeforeEach;
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

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author Dave Syer
 */
public class CsvResultsWriterFactoryTests {

	private static String TARGET_FILE = "target/result.csv";

	private CsvResultsWriterFactory factory = new CsvResultsWriterFactory();

	@BeforeEach
	public void init() {
		File target = new File(TARGET_FILE);
		if (target.exists()) {
			assertThat(target.delete()).isTrue();
		}
	}

	@Test
	void validUri() {
		assertThat(factory.forUri("csv:target/empty.csv")).isNotNull();
	}

	@Test
	void writeEmptyResults() {
		RunResult runResult = new RunResult(null, Collections.emptyList());
		String result = output(runResult);
		assertThat(new File(TARGET_FILE)).exists();
		assertThat(result).startsWith(System.lineSeparator());
		assertThat(result).contains("class, method, median, mean, range");
	}

	@Test
	void writeActualResults() {
		BenchmarkParams params = params();
		Collection<BenchmarkResult> data = Arrays.<BenchmarkResult> asList(new BenchmarkResult(null, data()));
		RunResult runResult = new RunResult(params, data);
		String result = output(runResult);
		assertThat(result).containsSubsequence("class, method, median, mean, range", "Foo, exec");
	}

	@Test
	void writeParamResult() {
		BenchmarkParams params = params("a=b");
		Collection<BenchmarkResult> data = Arrays.<BenchmarkResult> asList(new BenchmarkResult(null, data()));
		RunResult runResult = new RunResult(params, data);
		String result = output(runResult);
		// System.err.println(result);
		assertThat(result).containsSubsequence("class, method, a, median, mean, range", "Foo, exec, b");
	}

	@Test
	void writeSecondaryResult() {
		BenchmarkParams params = params("a=b");
		Collection<BenchmarkResult> data = Arrays.<BenchmarkResult> asList(new BenchmarkResult(null, data("bar")));
		RunResult runResult = new RunResult(params, data);
		String result = output(runResult);
		// System.err.println(result);
		assertThat(result).containsSubsequence("class, method, a, bar, median, mean, range", "Foo, exec, b");
	}

	@Test
	void writeMixedParamResults() {
		Collection<BenchmarkResult> data = Arrays.<BenchmarkResult> asList(new BenchmarkResult(null, data()));
		String result = output(new RunResult(params("a=b"), data), new RunResult(params("a=e", "c=d"), data));
		// System.err.println(result);
		assertThat(result).containsSubsequence("class, method, a, c, median, mean, range", "Foo, exec, b, ,", "e, d");
	}

	@Test
	void writeMixedSecondaryResults() {
		Collection<BenchmarkResult> data = Arrays.<BenchmarkResult> asList(new BenchmarkResult(null, data()));
		Collection<BenchmarkResult> seconds = Arrays
				.<BenchmarkResult> asList(new BenchmarkResult(null, data("bar", "spam")));
		String result = output(new RunResult(params(), data), new RunResult(params(), seconds));
		// System.err.println(result);
		assertThat(result).containsSubsequence("class, method, bar, spam, median, mean, range", "Foo, exec, , ",
				"Foo, exec, 1.000");
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
		return new BenchmarkParams("com.example.Foo.exec", "bar", true, 1, new int[] { 1 }, Arrays.asList("thread"), 1, 0,
				null, null, Mode.AverageTime, workload, TimeUnit.MILLISECONDS, 1, "", Collections.emptyList(), "1.8", "JDK",
				"1.8", "1.21", TimeValue.NONE);
	}

	private Collection<IterationResult> data(String... seconds) {
		IterationResult result = new IterationResult(null, null, null);
		result.addResult(new AverageTimeResult(ResultRole.PRIMARY, "foo", 15, 300000000, TimeUnit.MILLISECONDS));
		for (String label : seconds) {
			result.addResult(new AverageTimeResult(ResultRole.SECONDARY, label, 1, 1000000, TimeUnit.MILLISECONDS));
		}
		return Arrays.asList(result);
	}

	private String output(RunResult... runResult) {
		OutputStream stream = new ByteArrayOutputStream();
		OutputFormat output = OutputFormatFactory.createFormatInstance(new PrintStream(stream), VerboseMode.NORMAL);
		factory.forUri("csv:" + TARGET_FILE).write(output, Arrays.asList(runResult));
		String result = stream.toString();
		return result;
	}

}
