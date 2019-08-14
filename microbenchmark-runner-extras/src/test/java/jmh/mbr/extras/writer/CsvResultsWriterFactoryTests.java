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

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.Arrays;
import java.util.Collections;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openjdk.jmh.results.RunResult;
import org.openjdk.jmh.runner.format.OutputFormat;
import org.openjdk.jmh.runner.format.OutputFormatFactory;
import org.openjdk.jmh.runner.options.VerboseMode;

import static org.assertj.core.api.Assertions.*;

class CsvResultsWriterFactoryTests {

	private static String TARGET_FILE = "target/result.csv";

	private CsvResultsWriterFactory factory = new CsvResultsWriterFactory();

	@BeforeEach
	void init() {
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
		output(runResult);
		assertThat(new File(TARGET_FILE)).exists();
	}

	private String output(RunResult... runResult) {
		OutputStream stream = new ByteArrayOutputStream();
		OutputFormat output = OutputFormatFactory
				.createFormatInstance(new PrintStream(stream), VerboseMode.NORMAL);
		factory.forUri("csv:" + TARGET_FILE).write(output, Arrays.asList(runResult));
		return stream.toString();
	}
}
