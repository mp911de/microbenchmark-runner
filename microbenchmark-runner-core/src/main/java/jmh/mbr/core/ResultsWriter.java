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

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.ServiceLoader;

import org.openjdk.jmh.results.RunResult;
import org.openjdk.jmh.results.format.ResultFormatFactory;
import org.openjdk.jmh.results.format.ResultFormatType;
import org.openjdk.jmh.runner.format.OutputFormat;

import lombok.SneakyThrows;

/**
 * @author Christoph Strobl
 */
public interface ResultsWriter {

	/**
	 * Write the {@link RunResult}s.
	 * @param output
	 *
	 * @param results can be {@literal null}.
	 */
	void write(OutputFormat output, Collection<RunResult> results);

	/**
	 * Convert {@link RunResult}s to JMH Json representation.
	 *
	 * @param results
	 * @return json string representation of results.
	 * @see org.openjdk.jmh.results.format.JSONResultFormat
	 */
	@SneakyThrows
	static String jsonifyResults(Collection<RunResult> results) {

		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		ResultFormatFactory
				.getInstance(ResultFormatType.JSON, new PrintStream(baos, true, "UTF-8"))
				.writeOut(results);

		return new String(baos.toByteArray(), StandardCharsets.UTF_8);
	}

	static ResultsWriter forUri(String uri) {
		ServiceLoader<ResultsWriterFactory> loader = ServiceLoader
				.load(ResultsWriterFactory.class);
		List<ResultsWriter> result = new ArrayList<>();
		for (ResultsWriterFactory factory : loader) {
			ResultsWriter writer = factory.forUri(uri);
			if (writer != null) {
				result.add(writer);
			}
		}
		return result.isEmpty() ? null : new CompositeResultsWriter(result);
	}
}

class CompositeResultsWriter implements ResultsWriter {

	private List<ResultsWriter> writers = new ArrayList<>();

	public CompositeResultsWriter(List<ResultsWriter> writers) {
		this.writers = writers;
	}

	@Override
	public void write(OutputFormat output, Collection<RunResult> results) {
		for (ResultsWriter writer : writers) {
			writer.write(output, results);
		}
	}

	public void add(ResultsWriter writer) {
		if (writer != null) {
			this.writers.add(writer);
		}
	}

}
