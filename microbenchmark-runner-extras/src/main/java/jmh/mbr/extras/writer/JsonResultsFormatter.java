/*
 * Copyright 2019-2020 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * https://www.eclipse.org/legal/epl-v20.html
 */
package jmh.mbr.extras.writer;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.Map.Entry;
import java.util.function.Function;
import java.util.stream.Collectors;

import jmh.mbr.core.StringUtils;
import jmh.mbr.core.model.BenchmarkResults;
import jmh.mbr.core.model.BenchmarkResults.BenchmarkResult;
import jmh.mbr.core.model.BenchmarkResults.MetaData;
import org.openjdk.jmh.infra.BenchmarkParams;
import org.openjdk.jmh.results.Result;
import org.openjdk.jmh.results.RunResult;

class JsonResultsFormatter {

	static List<String> createReport(BenchmarkResults results) {
		return results.stream().map(JsonResultsFormatter::format)
				.collect(Collectors.toList());
	}

	static String format(MetaData metaData, RunResult runResult) {
		return format(new BenchmarkResult(metaData, runResult));
	}

	static String format(BenchmarkResult result) {

		StringBuilder sb = new StringBuilder();
		sb.append("{\n");
		sb.append(formatMetadata(result.getMetaData()));
		sb.append(formatMainData(result.getParams()));
		sb.append(formatEnvironmentData(result.getMetaData(), result.getParams()));
		sb.append(formatResult("primary", result.getPrimaryResult()));
		sb.append('}');
		return sb.toString();
	}

	static String formatMetadata(MetaData metaData) {

		StringBuilder sb = new StringBuilder();

		sb.append("    \"date\" : \"" + metaData.getTime().toString() + "\",\n");
		sb.append("    \"project\" : \"" + metaData.getProject() + "\",\n");
		sb.append("    \"version\" : \"" + metaData.getVersion() + "\",\n");

		return sb.toString();
	}

	static String formatMainData(BenchmarkParams params) {

		StringBuilder sb = new StringBuilder();
		sb.append("    \"group\" : \"" + extractClass(params.getBenchmark()) + "\",\n");
		sb.append("    \"benchmark\" : \"" + extractBenchmarkName(params.getBenchmark()) + "\",\n");
		sb.append("    \"method\" : \"" + params.getBenchmark() + "\",\n");
		sb.append("    \"mode\" : \"" + params.getMode().shortLabel() + "\",\n");

		return sb.toString();
	}

	static String formatEnvironmentData(MetaData metaData, BenchmarkParams params) {

		StringBuilder sb = new StringBuilder();
		sb.append("    \"env\" : {\n");
		sb.append("        \"jvm\" : \"" + toJsonString(params.getJvm()) + "\",\n");
		sb.append("        \"jvmArgs\" : " + toJsonArray(params.getJvmArgs()) + ",\n");
		sb.append("        \"vmVersion\" : \"" + toJsonString(params.getVmVersion()) + "\",\n");
		sb.append("        \"os\" : \"" + metaData.getOs() + "\"");

		if(metaData.hasAdditionalMetadata()) {

			for(Entry<String,Object> entry : metaData.getAdditionalParameters().entrySet()) {
				sb.append(",\n");
				sb.append("        \""+entry.getKey()+"\" : \"" + entry.getValue() + "\"");
			}
		}
		sb.append("\n");
		sb.append("    },\n");
		return sb.toString();
	}

	static String formatResult(String name, Result result) {

		StringBuilder sb = new StringBuilder();
		sb.append("    \"" + name + "\" : {\n");
		sb.append("        \"score\" : " + formatNumber(result.getScore()) + ",\n");
		sb.append("        \"scoreError\" : " + formatNumber(result.getScoreError()) + ",\n");
		{

			List<Double> scoreConfidence = Arrays.stream(result.getScoreConfidence()).boxed().collect(Collectors.toList());
			sb.append("        \"scoreConfidence\" : " + toJsonArray(scoreConfidence, JsonResultsFormatter::formatNumber) + ",\n");
		}
		sb.append("        \"scoreUnit\" : \"" + result.getScoreUnit() + "\"\n");

		sb.append("    }\n"); // primaryMetric end
		return sb.toString();
	}


	static String toJsonString(String s) {
		StringBuilder sb = new StringBuilder();
		for (char c : s.toCharArray()) {
			if (Character.isISOControl(c)) {
				continue;
			}
			switch (c) {
				// use & as escape character to escape the tidying
				case '&':
					sb.append("&&");
					break;
				// we cannot escape to \\\\ since this would create sequences interpreted by the tidying
				case '\\':
					sb.append("&/");
					break;
				case '"':
					sb.append("&'");
					break;
				// escape spacial chars for the tidying formatting below that might appear in a string
				case ',':
					sb.append(";");
					break;
				case '[':
					sb.append("<");
					break;
				case ']':
					sb.append(">");
					break;
				case '<':
					sb.append("&-");
					break;
				case '>':
					sb.append("&=");
					break;
				case ';':
					sb.append("&:");
					break;
				case '{':
					sb.append("&(");
					break;
				case '}':
					sb.append("&)");
					break;
				default:
					sb.append(c);
			}
		}
		return sb.toString();
	}

	private static String toJsonArray(Collection<String> col) {
		return toJsonArray(col, JsonResultsFormatter::wrap);
	}


	private static <T> String toJsonArray(Collection<T> col, Function<T, String> mapFunction) {
		return "[" + StringUtils.collectionToDelimitedString(col.stream().map(mapFunction).collect(Collectors.toList()), ",") + "]";
	}

	private static String wrap(String source) {
		return "\"" + source + "\"";
	}

	private static java.lang.String formatNumber(Number number) {

		if (number == null) {
			return "NaN";
		}

		double value = number.doubleValue();
		if (Double.isInfinite(value)) {
			if (value == Double.POSITIVE_INFINITY) {
				return "+INF";
			}
			return "-INF";
		}

		DecimalFormatSymbols symbols = new DecimalFormatSymbols(Locale.US);
		symbols.setDecimalSeparator('.');
		return new DecimalFormat("###.###", symbols).format(value);
	}

	private static String extractClass(String source) {

		int index = source.lastIndexOf('.');
		if (index <= 0) {
			return source;
		}

		String tmp = source.substring(0, index);
		return tmp.substring(tmp.lastIndexOf(".") + 1);
	}

	private static String extractBenchmarkName(String source) {

		int index = source.lastIndexOf('.');
		if (index <= 0) {
			return source;
		}
		return source.substring(source.lastIndexOf(".") + 1);
	}
}
