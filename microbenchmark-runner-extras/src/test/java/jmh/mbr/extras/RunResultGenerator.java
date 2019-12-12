/*
 * Copyright 2019 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * https://www.eclipse.org/legal/epl-v20.html
 */
package jmh.mbr.extras;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Random;
import java.util.TreeSet;
import java.util.concurrent.TimeUnit;

import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.infra.BenchmarkParams;
import org.openjdk.jmh.infra.IterationParams;
import org.openjdk.jmh.results.BenchmarkResult;
import org.openjdk.jmh.results.IterationResult;
import org.openjdk.jmh.results.ResultRole;
import org.openjdk.jmh.results.RunResult;
import org.openjdk.jmh.results.ThroughputResult;
import org.openjdk.jmh.runner.IterationType;
import org.openjdk.jmh.runner.WorkloadParams;
import org.openjdk.jmh.runner.options.TimeValue;


public class RunResultGenerator {

	private static final String JVM_DUMMY = "javadummy";

	private static final String JDK_VERSION_DUMMY = "jdk-11.0.1.jdk";

	private static final String VM_NAME_DUMMY = "DummyVM";

	private static final String VM_VERSION_DUMMY = "4711";

	private static final String JMH_VERSION_DUMMY = "11.0.1+13-LTS";

	public static Collection<RunResult> generate(String name) {

		BenchmarkParams params = params(name);
		return generate(params, benchmarkResults(params, 3, 10, 20, 30));
	}

	public static Collection<RunResult> generate(BenchmarkParams params, Collection<BenchmarkResult> results) {
		return Collections.singleton(new RunResult(params, results));
	}


	public static BenchmarkParams params(String name) {

		BenchmarkParams params = new BenchmarkParams(
				name+".log",
				name + ".benchmark_" + Mode.Throughput,
				false,
				1,
				new int[]{1},
				Collections.<String>emptyList(),

				1,
				1,
				new IterationParams(IterationType.WARMUP, 10, TimeValue.seconds(5), 1),
				new IterationParams(IterationType.MEASUREMENT, 10, TimeValue.seconds(10), 1),
				Mode.Throughput,
				new WorkloadParams(),
				TimeUnit.SECONDS, 1,
				JVM_DUMMY,
				Collections.<String>emptyList(),
				JDK_VERSION_DUMMY, VM_NAME_DUMMY, VM_VERSION_DUMMY, JMH_VERSION_DUMMY,
				TimeValue.days(1));

		return params;
	}

	public static Collection<BenchmarkResult> benchmarkResults(BenchmarkParams params, int iterations, Integer... ops) {

		Collection<BenchmarkResult> benchmarkResults = new ArrayList<>();
		Collection<IterationResult> iterationResults = new ArrayList<>(iterations);

		for (int iteration = 0; iteration < iterations; iteration++) {
			IterationResult iterationResult = generateIterationResult(iteration, params, ops);
			iterationResults.add(iterationResult);
		}

		benchmarkResults.add(new BenchmarkResult(params, iterationResults));
		return benchmarkResults;
	}

	private static IterationResult generateIterationResult(int iteration, BenchmarkParams params, Integer[] ops) {

		IterationResult iterationResult = new IterationResult(params, params.getMeasurement(), null);
		for (Integer operations : ops) {
			iterationResult.addResult(new ThroughputResult(ResultRole.PRIMARY, params.getBenchmark()+".log", operations, 1000 * 1000, TimeUnit.MILLISECONDS));
		}
		return iterationResult;
	}


	public static Collection<RunResult> random() {

		Collection<RunResult> results = new TreeSet<>(RunResult.DEFAULT_SORT_COMPARATOR);

		Random r = new Random(12345);
		Random ar = new Random(12345);

		for (int b = 0; b < r.nextInt(10); b++) {

			WorkloadParams ps = new WorkloadParams();
			ps.put("param0", "value0", 0);
			ps.put("param1", "[value1]", 1);
			ps.put("param2", "{value2}", 2);
			ps.put("param3", "'value3'", 3);
			ps.put("param4", "\"value4\"", 4);
			BenchmarkParams params = new BenchmarkParams(
					"benchmark_" + b,
					RunResultGenerator.class.getName() + ".benchmark_" + b + "_" + Mode.Throughput,
					false,
					r.nextInt(1000),
					new int[]{r.nextInt(1000)},
					Collections.<String>emptyList(),

					r.nextInt(1000),
					r.nextInt(1000),
					new IterationParams(IterationType.WARMUP, r.nextInt(1000), TimeValue.seconds(r.nextInt(1000)), 1),
					new IterationParams(IterationType.MEASUREMENT, r.nextInt(1000), TimeValue.seconds(r.nextInt(1000)), 1),
					Mode.Throughput,
					ps,
					TimeUnit.SECONDS, 1,
					JVM_DUMMY,
					Collections.<String>emptyList(),
					JDK_VERSION_DUMMY, VM_NAME_DUMMY, VM_VERSION_DUMMY, JMH_VERSION_DUMMY,
					TimeValue.days(1));

			Collection<BenchmarkResult> benchmarkResults = new ArrayList<>();
			for (int f = 0; f < r.nextInt(10); f++) {
				Collection<IterationResult> iterResults = new ArrayList<>();
				for (int c = 0; c < r.nextInt(10); c++) {
					IterationResult res = new IterationResult(params, params.getMeasurement(), null);
					res.addResult(new ThroughputResult(ResultRole.PRIMARY, "test", r.nextInt(1000), 1000 * 1000, TimeUnit.MILLISECONDS));
					res.addResult(new ThroughputResult(ResultRole.SECONDARY, "secondary1", r.nextInt(1000), 1000 * 1000, TimeUnit.MILLISECONDS));
					res.addResult(new ThroughputResult(ResultRole.SECONDARY, "secondary2", r.nextInt(1000), 1000 * 1000, TimeUnit.MILLISECONDS));
					if (ar.nextBoolean()) {
						res.addResult(new ThroughputResult(ResultRole.SECONDARY, "secondary3", ar.nextInt(1000), 1000 * 1000, TimeUnit.MILLISECONDS));
					}
					iterResults.add(res);
				}
				benchmarkResults.add(new BenchmarkResult(params, iterResults));
			}
			results.add(new RunResult(params, benchmarkResults));
		}
		return results;
	}
}
