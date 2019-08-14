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

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Collection;
import java.util.Collections;

import jmh.mbr.core.ResultsWriter;
import org.openjdk.jmh.results.RunResult;
import org.openjdk.jmh.runner.format.OutputFormat;
import org.openjdk.jmh.util.FileUtils;

class CsvResultsWriter implements ResultsWriter {

	private final String uri;

	public CsvResultsWriter(String uri) {
		this.uri = uri;
	}

	@Override
	public void write(OutputFormat output, Collection<RunResult> results) {

		String report;

		try {
			report = CsvResultsFormatter.createReport(results);
		}
		catch (Exception e) {
			output.println("Report creation failed: " + captureStackTrace(e));
			return;
		}

		File file = new File(uri.substring("csv:".length()));
		output.println(System.lineSeparator());
		output.println("Writing result to file: " + file);
		file.getParentFile().mkdirs();
		if (file.getParentFile().exists()) {
			try {
				FileUtils.writeLines(file, Collections.singleton(report));
			}
			catch (IOException e) {
				output.println("Write failed: " + e
						.getMessage() + " " + captureStackTrace(e));
			}
		}
	}

	private String captureStackTrace(Exception e) {
		StringWriter trace = new StringWriter();
		e.printStackTrace(new PrintWriter(trace));
		return trace.toString();
	}

}
