/*
 * Copyright 2018-2020 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v20.html
 */
package jmh.mbr.core;

import java.util.ArrayList;
import java.util.List;
import java.util.ServiceLoader;

import jmh.mbr.core.model.BenchmarkResults;
import org.openjdk.jmh.results.RunResult;
import org.openjdk.jmh.runner.format.OutputFormat;

/**
 * Writes JMH results to an external target. This can be targets such as files, HTTP endpoints, or databases. {@link ResultsWriter} can be contributed through Java's {@link ServiceLoader} plugin mechanism.
 *
 * @see ResultsWriterFactory
 */
public interface ResultsWriter {

	/**
	 * Write the {@link RunResult}s.
	 *
	 * @param output original {@link OutputFormat} to append further details or failures that occurred while writing results.
	 * @param results can be {@literal null}.
	 */
	void write(OutputFormat output, BenchmarkResults results);


	/**
	 * Creates a {@link ResultsWriter} given a {@code uri}. This method considers {@link ResultsWriter} plugins provided by {@link ResultsWriterFactory} via Java's {@link ServiceLoader} mechanism. Returns {@literal null} if no applicable {@link ResultsWriter} was found.
	 *
	 * @param uri
	 * @return the {@link ResultsWriter} or {@literal null} if none was found or none was applicable to {@code uri}.
	 */
	static ResultsWriter forUri(String uri) {

		ServiceLoader<ResultsWriterFactory> loader = ServiceLoader.load(ResultsWriterFactory.class);

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
