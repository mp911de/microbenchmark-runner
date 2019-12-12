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

import java.io.PrintWriter;
import java.io.StringWriter;

import jmh.mbr.core.ResultsWriter;
import jmh.mbr.core.model.BenchmarkResults;
import org.openjdk.jmh.runner.format.OutputFormat;

class SysoutCsvResultsWriter implements ResultsWriter {

	private final String uri;

	SysoutCsvResultsWriter(String uri) {
		this.uri = uri;
	}

	@Override
	public void write(OutputFormat output, BenchmarkResults results) {

		try {

			String report = CsvResultsFormatter.createReport(results.getRawResults());
			output.println(report);
		} catch (Exception e) {

			StringWriter trace = new StringWriter();
			e.printStackTrace(new PrintWriter(trace));

			output.println("Report creation failed: " + trace.toString());
		}
	}
}
