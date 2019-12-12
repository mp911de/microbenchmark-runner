/*
 * Copyright 2018 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v20.html
 */
package jmh.mbr.core;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

import jmh.mbr.core.model.BenchmarkResults;
import org.openjdk.jmh.results.RunResult;
import org.openjdk.jmh.runner.format.OutputFormat;

public class TestResultsWriterFactory implements ResultsWriterFactory {

	static Map<String, Supplier<ResultsWriter>> REGISTRY = new HashMap<>();

	static {
		REGISTRY.put("urn:empty", TestResultsWriter::new);
	}

	@Override
	public ResultsWriter forUri(String uri) {
		return REGISTRY.getOrDefault(uri, () -> null).get();
	}

	static class TestResultsWriter implements ResultsWriter {

		@Override
		public void write(OutputFormat output, BenchmarkResults results) {
		}
	}
}
