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
package jmh.mbr.core;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Date;

import org.openjdk.jmh.results.RunResult;
import org.openjdk.jmh.results.format.ResultFormatType;
import org.openjdk.jmh.runner.Defaults;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.format.OutputFormat;
import org.openjdk.jmh.runner.format.OutputFormatFactory;
import org.openjdk.jmh.runner.options.ChainedOptionsBuilder;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;
import org.openjdk.jmh.runner.options.TimeValue;
import org.openjdk.jmh.util.UnCloseablePrintStream;
import org.openjdk.jmh.util.Utils;

/**
 * @author Christoph Strobl
 * @author Mark Paluch
 */
public class JmhSupport {


	/**
	 * Collect all options for the {@link Runner}.
	 *
	 * @return never {@literal null}.
	 * @throws Exception
	 */
	public ChainedOptionsBuilder options(Class<?> jmhTestClass) throws Exception {

		ChainedOptionsBuilder optionsBuilder = options();
		return report(optionsBuilder, jmhTestClass);
	}

	/**
	 * Collect all options for the {@link Runner}.
	 *
	 * @return never {@literal null}.
	 * @throws Exception
	 */
	public ChainedOptionsBuilder options() {

		ChainedOptionsBuilder optionsBuilder = new OptionsBuilder();

		optionsBuilder = warmup(optionsBuilder);
		optionsBuilder = measure(optionsBuilder);
		optionsBuilder = forks(optionsBuilder);

		return optionsBuilder;
	}

	/**
	 * Read {@code warmupIterations} property from {@link org.springframework.core.env.Environment}.
	 *
	 * @return -1 if not set.
	 */
	public int getWarmupIterations() {
		return Integer.parseInt(Environment.getProperty("warmupIterations", "-1"));
	}

	/**
	 * Read {@code measurementIterations} property from {@link org.springframework.core.env.Environment}.
	 *
	 * @return -1 if not set.
	 */
	public int getMeasurementIterations() {
		return Integer.parseInt(Environment.getProperty("measurementIterations", "-1"));

	}

	/**
	 * Read {@code forks} property from {@link org.springframework.core.env.Environment}.
	 *
	 * @return -1 if not set.
	 */
	public int getForksCount() {
		return Integer.parseInt(Environment.getProperty("forks", "-1"));
	}

	/**
	 * Read {@code benchmarkReportDir} property from {@link org.springframework.core.env.Environment}.
	 *
	 * @return {@literal null} if not set.
	 */
	public String getReportDirectory() {
		return Environment.getProperty("benchmarkReportDir");
	}

	/**
	 * Read {@code measurementTime} property from {@link org.springframework.core.env.Environment}.
	 *
	 * @return -1 if not set.
	 */
	public long getMeasurementTime() {
		return Long.parseLong(Environment.getProperty("measurementTime", "-1"));
	}

	/**
	 * Read {@code warmupTime} property from {@link org.springframework.core.env.Environment}.
	 *
	 * @return -1 if not set.
	 */
	public long getWarmupTime() {
		return Integer.parseInt(Environment.getProperty("warmupTime", "-1"));
	}

	/**
	 * {@code project.version_yyyy-MM-dd_ClassName.json} eg.
	 * {@literal 1.11.0.BUILD-SNAPSHOT_2017-03-07_MappingMongoConverterBenchmark.json}
	 *
	 * @return
	 */
	public String reportFilename(Class<?> jmhTestClass) {

		StringBuilder sb = new StringBuilder();

		if (Environment.containsProperty("project.version")) {

			sb.append(Environment.getProperty("project.version"));
			sb.append("_");
		}

		sb.append(new SimpleDateFormat("yyyy-MM-dd").format(new Date()));
		sb.append("_");
		sb.append(jmhTestClass.getSimpleName());
		sb.append(".json");
		return sb.toString();
	}

	/**
	 * Apply measurement options to {@link ChainedOptionsBuilder}.
	 *
	 * @param optionsBuilder must not be {@literal null}.
	 * @return {@link ChainedOptionsBuilder} with options applied.
	 * @see #getMeasurementIterations()
	 * @see #getMeasurementTime()
	 */
	private ChainedOptionsBuilder measure(ChainedOptionsBuilder optionsBuilder) {

		int measurementIterations = getMeasurementIterations();
		long measurementTime = getMeasurementTime();

		if (measurementIterations > 0) {
			optionsBuilder = optionsBuilder.measurementIterations(measurementIterations);
		}

		if (measurementTime > 0) {
			optionsBuilder = optionsBuilder.measurementTime(TimeValue.seconds(measurementTime));
		}

		return optionsBuilder;
	}

	/**
	 * Apply warmup options to {@link ChainedOptionsBuilder}.
	 *
	 * @param optionsBuilder must not be {@literal null}.
	 * @return {@link ChainedOptionsBuilder} with options applied.
	 * @see #getWarmupIterations()
	 * @see #getWarmupTime()
	 */
	private ChainedOptionsBuilder warmup(ChainedOptionsBuilder optionsBuilder) {

		int warmupIterations = getWarmupIterations();
		long warmupTime = getWarmupTime();

		if (warmupIterations > 0) {
			optionsBuilder = optionsBuilder.warmupIterations(warmupIterations);
		}

		if (warmupTime > 0) {
			optionsBuilder = optionsBuilder.warmupTime(TimeValue.seconds(warmupTime));
		}

		return optionsBuilder;
	}

	/**
	 * Apply forks option to {@link ChainedOptionsBuilder}.
	 *
	 * @param optionsBuilder must not be {@literal null}.
	 * @return {@link ChainedOptionsBuilder} with options applied.
	 * @see #getForksCount()
	 */
	private ChainedOptionsBuilder forks(ChainedOptionsBuilder optionsBuilder) {

		int forks = getForksCount();

		if (forks <= 0) {
			return optionsBuilder;
		}

		return optionsBuilder.forks(forks);
	}

	/**
	 * Apply report option to {@link ChainedOptionsBuilder}.
	 *
	 * @param optionsBuilder must not be {@literal null}.
	 * @return {@link ChainedOptionsBuilder} with options applied.
	 * @throws IOException if report file cannot be created.
	 * @see #getReportDirectory()
	 */
	private ChainedOptionsBuilder report(ChainedOptionsBuilder optionsBuilder, Class<?> jmhTestClass) throws IOException {

		String reportDir = getReportDirectory();

		if (!StringUtils.hasText(reportDir)) {
			return optionsBuilder;
		}

		String reportFilePath = reportDir + (reportDir.endsWith(File.separator) ? "" : File.separator)
				+ reportFilename(jmhTestClass);
		File file = getFile(reportFilePath);

		if (file.exists()) {
			file.delete();
		} else {

			file.getParentFile().mkdirs();
			file.createNewFile();
		}

		optionsBuilder.resultFormat(ResultFormatType.JSON);
		optionsBuilder.result(reportFilePath);

		return optionsBuilder;
	}

	/**
	 * Resolve the given resource location to a {@code java.io.File}, i.e. to a file in the file system.
	 * 
	 * @param resourceLocation
	 * @return
	 */
	private File getFile(String resourceLocation) {
		return new File(URI.create(resourceLocation));
	}

	/**
	 * Publish results to an external system.
	 *
	 * @param results must not be {@literal null}.
	 */
	public void publishResults(Collection<RunResult> results) {

		if (results.isEmpty() || !Environment.containsProperty("publishTo")) {
			return;
		}

		String uri = Environment.getProperty("publishTo");
		// TODO: Registry?
		/*
		try {
			ResultsWriter.forUri(uri).write(results);
		} catch (Exception e) {
			System.err.println(String.format("Cannot save benchmark results to '%s'. Error was %s.", uri, e));
		} */

	}

	public OutputFormat createOutputFormat(Options options) {

		// sadly required here as the check cannot be made before calling this method in constructor
		if (options == null) {
			throw new IllegalArgumentException("Options not allowed to be null.");
		}

		PrintStream out;
		if (options.getOutput().hasValue()) {
			try {
				out = new PrintStream(options.getOutput().get());
			} catch (FileNotFoundException ex) {
				throw new IllegalStateException(ex);
			}
		} else {
			// Protect the System.out from accidental closing
			try {
				out = new UnCloseablePrintStream(System.out, Utils.guessConsoleEncoding());
			} catch (UnsupportedEncodingException ex) {
				throw new IllegalStateException(ex);
			}
		}

		return OutputFormatFactory.createFormatInstance(out, options.verbosity().orElse(Defaults.VERBOSITY));
	}
}
