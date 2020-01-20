/*
 * Copyright 2019-2020 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v20.html
 */
package jmh.mbr.extras.writer;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Collections;

import jmh.mbr.core.ResultsWriter;
import jmh.mbr.core.model.BenchmarkResults;
import org.openjdk.jmh.runner.format.OutputFormat;
import org.openjdk.jmh.util.FileUtils;

class CsvResultsWriter implements ResultsWriter {

	private final String uri;

	public CsvResultsWriter(String uri) {
		this.uri = uri;
	}

	@Override
	public void write(OutputFormat output, BenchmarkResults results) {

		String report;

		try {
			report = CsvResultsFormatter.createReport(results.getRawResults());
		}
		catch (Exception e) {
			output.println("Report creation failed: " + StackTraceCapture.from(e));
			return;
		}
		try {

			File file = new File(uri.substring("csv:".length())).getCanonicalFile();
			output.println(System.lineSeparator());
			output.println("Writing result to file: " + file);

			File parent = file.getParentFile();
			if (parent != null) {

				parent.mkdirs();

				if (parent.exists()) {
					FileUtils.writeLines(file, Collections.singleton(report));
					return;
				}
			}

			throw new FileNotFoundException("Parent directory " + parent + " does not exist");
		}
		catch (IOException e) {
			output.println("Write failed: " + e
					.getMessage() + " " + StackTraceCapture.from(e));
		}
	}
}
