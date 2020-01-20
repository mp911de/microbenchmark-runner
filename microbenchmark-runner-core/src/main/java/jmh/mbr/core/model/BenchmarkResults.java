/*
 * Copyright 2019-2020 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * https://www.eclipse.org/legal/epl-v20.html
 */
package jmh.mbr.core.model;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.BiFunction;
import java.util.stream.Stream;

import jmh.mbr.core.Environment;
import jmh.mbr.core.model.BenchmarkResults.BenchmarkResult;
import org.openjdk.jmh.infra.BenchmarkParams;
import org.openjdk.jmh.results.Result;
import org.openjdk.jmh.results.RunResult;

/**
 * Wrapper for {@link RunResult RunResults}.
 */
public class BenchmarkResults implements Iterable<BenchmarkResult> {

	private final List<RunResult> runResults;
	private final MetaData metaData;

	public BenchmarkResults(MetaData metaData, Collection<RunResult> runResults) {

		this.runResults = new ArrayList<>(runResults);
		this.metaData = metaData;
	}

	/**
	 * Obtain a {@link Stream} of {@link BenchmarkResult}.
	 *
	 * @return a {@link Stream} of {@link BenchmarkResult}.
	 */
	public Stream<BenchmarkResult> stream() {
		return runResults.stream().map(it -> new BenchmarkResult(metaData, it));
	}

	public MetaData getMetaData() {
		return metaData;
	}

	/**
	 * @return the raw {@link RunResult jmh results}.
	 */
	public List<RunResult> getRawResults() {
		return runResults;
	}

	@Override
	public Iterator<BenchmarkResult> iterator() {
		return runResults.stream().map(it -> new BenchmarkResult(metaData, it))
				.iterator();
	}

	/**
	 * Wrapper for a single {@link RunResult} along with execution {@link MetaData}.
	 */
	public static class BenchmarkResult {

		private final MetaData metaData;
		private final RunResult runResult;

		public BenchmarkResult(MetaData metaData, RunResult runResult) {

			this.metaData = metaData;
			this.runResult = runResult;
		}

		public <T> T map(BiFunction<MetaData, RunResult, T> function) {
			return function.apply(metaData, runResult);
		}

		public MetaData getMetaData() {
			return metaData;
		}

		public Collection<org.openjdk.jmh.results.BenchmarkResult> getBenchmarkResults() {
			return runResult.getBenchmarkResults();
		}

		public Result getPrimaryResult() {
			return runResult.getPrimaryResult();
		}

		public Map<String, Result> getSecondaryResults() {
			return runResult.getSecondaryResults();
		}

		public org.openjdk.jmh.results.BenchmarkResult getAggregatedResult() {
			return runResult.getAggregatedResult();
		}

		public BenchmarkParams getParams() {
			return runResult.getParams();
		}
	}

	public static class MetaData {

		private String project;
		private String version;
		private Instant time;
		private String os;
		private Map<String, Object> additionalParameters = new LinkedHashMap<>();

		private MetaData() {
			this.time = Instant.now();
		}

		public MetaData(String project, String version) {
			this();
			this.project = project;
			this.version = version;
		}

		public static MetaData none() {
			return new MetaData();
		}

		public String getProject() {
			return project;
		}

		public String getVersion() {
			return version;
		}

		public Instant getTime() {
			return time;
		}

		public String getOs() {
			return os != null ? os : Environment.getOsName();
		}

		public Map<String, Object> getAdditionalParameters() {
			return additionalParameters;
		}

		public boolean hasAdditionalMetadata() {
			return !additionalParameters.isEmpty();
		}

		public static MetaData from(Map<String, Object> metadata) {

			MetaData target = new MetaData();

			for (Entry<String, Object> entry : metadata.entrySet()) {

				switch (entry.getKey()) {
				case "os":
					target.os = entry.getValue().toString();
					continue;
				case "jmh.mbr.project":
					target.project = entry.getValue().toString();
					continue;
				case "jmh.mbr.project.version":
					target.version = entry.getValue().toString();
					continue;
				default:
					target.additionalParameters.put(entry.getKey(), entry.getValue());
				}
			}

			return target;
		}

		@Override
		public String toString() {
			return "MetaData{" +
					"project='" + project + '\'' +
					", version='" + version + '\'' +
					", time=" + time +
					", os='" + os + '\'' +
					", additionalParameters=" + additionalParameters +
					'}';
		}
	}
}
