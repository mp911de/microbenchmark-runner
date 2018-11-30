/*
 * Copyright 2018 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package jmh.mbr.junit5.execution;

import java.io.IOException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.junit.platform.engine.EngineExecutionListener;
import org.junit.platform.engine.TestDescriptor;
import org.junit.platform.engine.TestExecutionResult;
import org.openjdk.jmh.infra.BenchmarkParams;
import org.openjdk.jmh.infra.IterationParams;
import org.openjdk.jmh.results.BenchmarkResult;
import org.openjdk.jmh.results.IterationResult;
import org.openjdk.jmh.results.RunResult;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.format.OutputFormat;
import org.openjdk.jmh.runner.options.ChainedOptionsBuilder;
import org.openjdk.jmh.runner.options.Options;

import jmh.mbr.core.Environment;
import jmh.mbr.core.JmhSupport;
import jmh.mbr.core.StringUtils;
import jmh.mbr.core.model.BenchmarkClass;
import jmh.mbr.core.model.MethodAware;
import jmh.mbr.junit5.descriptor.BenchmarkFixtureDescriptor;
import jmh.mbr.junit5.descriptor.BenchmarkMethodDescriptor;
import jmh.mbr.junit5.descriptor.ParametrizedBenchmarkMethodDescriptor;

/**
 * JMH Benchmark runner.
 * 
 * @author Mark Paluch
 */
public class JmhRunner {

	public void execute(TestDescriptor testDescriptor, EngineExecutionListener listener) {

		JmhSupport support = new JmhSupport();

		ChainedOptionsBuilder optionsBuilder = support.options();

		getIncludes(testDescriptor).forEach(optionsBuilder::include);

		List<TestDescriptor> methods = collectBenchmarkMethods(testDescriptor);

		if (!support.isEnabled()) {
			listener.executionSkipped(testDescriptor, "No benchmarks");
			return;
		}

		if (!shouldRun(methods)) {
			return;
		}

		CacheFunction cache = new CacheFunction(methods);
		Options options = optionsBuilder.build();
		NotifyingOutputFormat notifyingOutputFormat = new NotifyingOutputFormat(listener,
				cache, support.createOutputFormat(options));

		try {
			listener.executionStarted(testDescriptor);
			support.publishResults(new Runner(options, notifyingOutputFormat).run());
			listener.executionFinished(testDescriptor, TestExecutionResult.successful());
		}
		catch (RunnerException e) {
			listener.executionFinished(testDescriptor, TestExecutionResult.failed(e));
		}
	}

	private List<TestDescriptor> collectBenchmarkMethods(TestDescriptor testDescriptor) {
		List<TestDescriptor> methods = new ArrayList<>();
		testDescriptor.accept(it -> {

			if (it instanceof BenchmarkMethodDescriptor
					|| it instanceof ParametrizedBenchmarkMethodDescriptor) {
				methods.add(it);
			}
		});
		return methods;
	}

	private boolean shouldRun(List<TestDescriptor> methods) {
		return !methods.isEmpty();
	}

	private List<String> getIncludes(TestDescriptor testDescriptor) {

		String tests = Environment.getProperty("benchmark");

		if (!StringUtils.hasText(tests)) {

			List<Method> methods = new ArrayList<>();

			testDescriptor.accept(it -> {

				if (it instanceof MethodAware) {
					methods.add(((MethodAware) it).getMethod());
				}
			});

			return methods.stream()
					.map(it -> Pattern.quote(it.getDeclaringClass().getName()) + "\\."
							+ Pattern.quote(it.getName()) + "$")
					.collect(Collectors.toList());
		}

		List<BenchmarkClass> classes = new ArrayList<>();

		testDescriptor.accept(it -> {

			if (it instanceof BenchmarkClass) {
				classes.add((BenchmarkClass) it);
			}
		});

		if (classes.stream().anyMatch(it -> {

			Class<?> javaClass = it.getJavaClass();
			return tests.contains(javaClass.getName())
					|| tests.contains(javaClass.getSimpleName());
		})) {
			if (!tests.contains("#")) {
				return Collections.singletonList(".*" + tests + ".*");
			}

			String[] args = tests.split("#");
			return Collections.singletonList(".*" + args[0] + "." + args[1]);
		}

		return Collections.emptyList();
	}

	/**
	 * {@link OutputFormat} that delegates to another {@link OutputFormat} and notifies
	 * {@link RunNotifier} about the progress.
	 */
	static class NotifyingOutputFormat implements OutputFormat {

		private final EngineExecutionListener listener;
		private final CacheFunction descriptionResolver;
		private final OutputFormat delegate;
		private final List<String> log = new CopyOnWriteArrayList<>();
		private final Map<TestDescriptor, AtomicInteger> expectedContainerCount = new ConcurrentHashMap<>();

		private volatile BenchmarkParams lastKnownBenchmark;
		private volatile boolean recordOutput;

		NotifyingOutputFormat(EngineExecutionListener listener, CacheFunction methods,
				OutputFormat delegate) {
			this.listener = listener;
			this.descriptionResolver = methods;
			this.delegate = delegate;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see
		 * org.openjdk.jmh.runner.format.OutputFormat#iteration(org.openjdk.jmh.infra.
		 * BenchmarkParams, org.openjdk.jmh.infra.IterationParams, int)
		 */
		@Override
		public void iteration(BenchmarkParams benchParams, IterationParams params,
				int iteration) {
			delegate.iteration(benchParams, params, iteration);
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see
		 * org.openjdk.jmh.runner.format.OutputFormat#iterationResult(org.openjdk.jmh.
		 * infra.BenchmarkParams, org.openjdk.jmh.infra.IterationParams, int,
		 * org.openjdk.jmh.results.IterationResult)
		 */
		@Override
		public void iterationResult(BenchmarkParams benchParams, IterationParams params,
				int iteration, IterationResult data) {
			delegate.iterationResult(benchParams, params, iteration, data);
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see
		 * org.openjdk.jmh.runner.format.OutputFormat#startBenchmark(org.openjdk.jmh.infra
		 * .BenchmarkParams)
		 */
		@Override
		public void startBenchmark(BenchmarkParams benchParams) {

			log.clear();

			lastKnownBenchmark = benchParams;

			TestDescriptor descriptor = descriptionResolver.apply(benchParams);

			listener.executionStarted(descriptor);

			delegate.startBenchmark(benchParams);
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see
		 * org.openjdk.jmh.runner.format.OutputFormat#endBenchmark(org.openjdk.jmh.results
		 * .BenchmarkResult)
		 */
		@Override
		public void endBenchmark(BenchmarkResult result) {

			recordOutput = false;

			BenchmarkParams lastKnownBenchmark = this.lastKnownBenchmark;
			TestExecutionResult executionResult = getResult(result, lastKnownBenchmark);
			TestDescriptor descriptor = getDescriptor(result, lastKnownBenchmark);

			listener.executionFinished(descriptor, executionResult);

			notifyFinishedRecursively(descriptor,
					it -> listener.executionFinished(it, executionResult));

			log.clear();
			delegate.endBenchmark(result);
		}

		private void notifyFinishedRecursively(TestDescriptor descriptor,
				Consumer<TestDescriptor> visitor) {

			Optional<TestDescriptor> parent = descriptor.getParent();

			while (parent.isPresent()) {

				TestDescriptor actualParent = parent.get();
				AtomicInteger integer = expectedContainerCount
						.computeIfAbsent(actualParent, it -> {

							AtomicInteger childCount = new AtomicInteger(0);

							it.accept(item -> {
								if (item instanceof BenchmarkMethodDescriptor
										|| item instanceof BenchmarkFixtureDescriptor) {
									childCount.incrementAndGet();
								}
							});
							return childCount;
						});

				if (integer.decrementAndGet() == 0) {
					visitor.accept(actualParent);
				}

				parent = actualParent.getParent();
			}
		}

		private TestExecutionResult getResult(BenchmarkResult result,
				BenchmarkParams lastKnownBenchmark) {

			if (result != null) {
				return TestExecutionResult.successful();
			}

			if (lastKnownBenchmark != null) {

				String output = StringUtils.collectionToDelimitedString(log,
						System.getProperty("line.separator"));
				return TestExecutionResult.failed(new JmhRunnerException(output));
			}

			return TestExecutionResult.successful();
		}

		private TestDescriptor getDescriptor(BenchmarkResult result,
				BenchmarkParams lastKnownBenchmark) {

			if (result != null) {
				return descriptionResolver.apply(result.getParams());
			}

			if (this.lastKnownBenchmark != null) {
				return descriptionResolver.apply(lastKnownBenchmark);
			}

			throw new IllegalStateException("Cannot obtain TestDescriptor");
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see org.openjdk.jmh.runner.format.OutputFormat#startRun()
		 */
		@Override
		public void startRun() {
			delegate.startRun();
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see org.openjdk.jmh.runner.format.OutputFormat#endRun(java.util.Collection)
		 */
		@Override
		public void endRun(Collection<RunResult> result) {
			delegate.endRun(result);
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see org.openjdk.jmh.runner.format.OutputFormat#print(java.lang.String)
		 */
		@Override
		public void print(String s) {
			delegate.print(s);
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see org.openjdk.jmh.runner.format.OutputFormat#println(java.lang.String)
		 */
		@Override
		public void println(String s) {

			if (recordOutput && StringUtils.hasText(s)) {
				log.add(s);
			}

			if (s.equals("<failure>")) {
				recordOutput = true;
			}

			delegate.println(s);
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see org.openjdk.jmh.runner.format.OutputFormat#flush()
		 */
		@Override
		public void flush() {
			delegate.flush();
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see org.openjdk.jmh.runner.format.OutputFormat#close()
		 */
		@Override
		public void close() {
			delegate.close();
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see
		 * org.openjdk.jmh.runner.format.OutputFormat#verbosePrintln(java.lang.String)
		 */
		@Override
		public void verbosePrintln(String s) {
			delegate.verbosePrintln(s);
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see org.openjdk.jmh.runner.format.OutputFormat#write(int)
		 */
		@Override
		public void write(int b) {
			delegate.write(b);
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see org.openjdk.jmh.runner.format.OutputFormat#write(byte[])
		 */
		@Override
		public void write(byte[] b) throws IOException {
			delegate.write(b);
		}
	}

	/**
	 * Exception proxy without stack trace.
	 */
	static class JmhRunnerException extends RuntimeException {

		private static final long serialVersionUID = -1385006784559013618L;

		JmhRunnerException(String message) {
			super(message);
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see java.lang.Throwable#fillInStackTrace()
		 */
		@Override
		public synchronized Throwable fillInStackTrace() {
			return null;
		}
	}

	/**
	 * Cache {@link Function} for benchmark names to {@link TestDescriptor}.
	 */
	static class CacheFunction implements Function<BenchmarkParams, TestDescriptor> {

		private final Map<String, TestDescriptor> methodMap = new ConcurrentHashMap<>();
		private final Collection<TestDescriptor> methods;

		CacheFunction(Collection<TestDescriptor> methods) {
			this.methods = methods;
		}

		/**
		 * Resolve a benchmark name (fqcn + "." + method name) to a
		 * {@link TestDescriptor}.
		 *
		 * @param benchmark
		 * @return
		 */
		@Override
		public TestDescriptor apply(BenchmarkParams benchmark) {

			TestDescriptor descriptor = getBenchmarkDescriptor(benchmark);

			if (descriptor instanceof ParametrizedBenchmarkMethodDescriptor) {

				ParametrizedBenchmarkMethodDescriptor parametrized = (ParametrizedBenchmarkMethodDescriptor) descriptor;
				Map<String, String> lookup = new HashMap<>();
				for (String key : benchmark.getParamsKeys()) {
					lookup.put(key, benchmark.getParam(key));
				}

				for (TestDescriptor child : parametrized.getChildren()) {

					if (child instanceof BenchmarkFixtureDescriptor) {
						BenchmarkFixtureDescriptor fixture = (BenchmarkFixtureDescriptor) child;

						if (fixture.getFixture().getFixture().equals(lookup)) {
							return fixture;
						}
					}
				}
			}

			return descriptor;
		}

		TestDescriptor getBenchmarkDescriptor(BenchmarkParams benchmark) {

			return methodMap.computeIfAbsent(benchmark.getBenchmark(), key -> {

				Optional<TestDescriptor> method = methods.stream()
						.filter(it -> getBenchmarkName(it).equals(key)).findFirst();

				return method.orElseThrow(() -> new IllegalArgumentException(
						String.format("Cannot resolve %s to a BenchmarkDescriptor!",
								benchmark.getBenchmark())));
			});
		}

		private String getBenchmarkName(TestDescriptor descriptor) {

			MethodAware methodAware = (MethodAware) descriptor;
			return methodAware.getMethod().getDeclaringClass().getName() + "."
					+ methodAware.getMethod().getName();
		}
	}
}
