/*
 * Copyright 2020 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v20.html
 */
package jmh.mbr.core;

import java.util.List;

import jmh.mbr.core.model.BenchmarkResults;
import org.openjdk.jmh.runner.format.OutputFormat;

/**
 * Composite {@link ResultsWriter}.
 */
class CompositeResultsWriter implements ResultsWriter {

	private final List<ResultsWriter> writers;

	CompositeResultsWriter(List<ResultsWriter> writers) {
		this.writers = writers;
	}

	@Override
	public void write(OutputFormat output, BenchmarkResults results) {
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
