/*
 * Copyright 2019 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * https://www.eclipse.org/legal/epl-v20.html
 */
package jmh.mbr.extras.writer;

import static org.openjdk.jmh.results.format.ResultFormatType.*;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import jmh.mbr.core.model.BenchmarkResults;
import jmh.mbr.core.model.BenchmarkResults.MetaData;
import jmh.mbr.extras.RunResultGenerator;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.openjdk.jmh.results.format.ResultFormatFactory;
import org.openjdk.jmh.results.format.ResultFormatType;

class JsonResultsFormatterUnitTests {

	@Test
	@Disabled
	void nativeJson() throws UnsupportedEncodingException {

		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		ResultFormatFactory.getInstance(ResultFormatType.JSON, new PrintStream(baos, true, "UTF-8")).writeOut(RunResultGenerator.random());

		String results = new String(baos.toByteArray(), StandardCharsets.UTF_8);
		System.out.println("results: " + results);
	}

	@Test
	@Disabled
	void nativeText() throws UnsupportedEncodingException {

		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		ResultFormatFactory.getInstance(TEXT, new PrintStream(baos, true, "UTF-8")).writeOut(RunResultGenerator.random());

		String results = new String(baos.toByteArray(), StandardCharsets.UTF_8);
		System.out.println("results: " + results);
	}

	@Test
	void json() {

		MetaData metaData = new MetaData("test-project", "1.0.0.SNAPSHOT");
		BenchmarkResults results = new BenchmarkResults(metaData, RunResultGenerator.generate("UnitTest"));

		List<String> json = JsonResultsFormatter.createReport(results);

		json.forEach(result -> {

			Assertions.assertThat(result)
					.contains("\"project\" : \"test-project\"")
					.contains("\"group\" : \"UnitTest\"")
					.contains("\"benchmark\" : \"log\"");
		});
	}

	@Test
	void metadata() {

		Map<String, Object> raw = new LinkedHashMap<>();
		raw.put("jmh.mbr.project", "test-project");
		raw.put("jmh.mbr.project.version", "1.0.0.SNAPSHOT");
		raw.put("jmh.mbr.marker-1", "1-marker");
		raw.put("jmh.mbr.marker-2", "2-marker");

		MetaData metaData = MetaData.from(raw);
		BenchmarkResults results = new BenchmarkResults(metaData, RunResultGenerator.generate("UnitTest"));

		List<String> json = JsonResultsFormatter.createReport(results);

		json.forEach(result -> {

			Assertions.assertThat(result)
					.contains(" \"jmh.mbr.marker-1\" : \"1-marker\",")
					.contains("\"jmh.mbr.marker-2\" : \"2-marker\"");
		});
	}
}
