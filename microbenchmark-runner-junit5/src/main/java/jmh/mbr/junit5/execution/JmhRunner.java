/*
 * Copyright 2018 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v20.html
 */
package jmh.mbr.junit5.execution;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
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
import org.opentest4j.TestAbortedException;

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
 */
public class JmhRunner {

	public void execute(TestDescriptor testDescriptor, EngineExecutionListener listener) {

		JmhSupport support = new JmhSupport();

		ChainedOptionsBuilder optionsBuilder = support.options();

		getIncludes(testDescriptor).forEach(optionsBuilder::include);

		if (!support.isEnabled()) {
			listener.executionSkipped(testDescriptor, "No benchmarks");
			return;
		}

		List<TestDescriptor> methods = collectBenchmarkMethods(listener, testDescriptor, optionsBuilder);

		if (!shouldRun(methods)) {
			return;
		}

		CacheFunction cache = new CacheFunction(methods);
		Options options = optionsBuilder.build();
		NotifyingOutputFormat notifyingOutputFormat = new NotifyingOutputFormat(listener, cache,
				support.createOutputFormat(options));

		try {
			listener.executionStarted(testDescriptor);
			support.publishResults(notifyingOutputFormat, new Runner(options, notifyingOutputFormat).run());
			executeAfters(methods);
			listener.executionFinished(testDescriptor, TestExecutionResult.successful());
		} catch (RunnerException e) {
			listener.executionFinished(testDescriptor, TestExecutionResult.failed(e));
		}
	}

	private boolean executeBefores(EngineExecutionListener listener, TestDescriptor test, Class<?> testClass) {
		boolean include = true;
		for (Method method : testClass.getDeclaredMethods()) {
			if (method.isAnnotationPresent(BeforeAll.class)) {
				if (Modifier.isStatic(method.getModifiers())) {
					try {
						method.invoke(null);
					} catch (TestAbortedException e) {
						include = false;
						break;
					} catch (RuntimeException e) {
						throw e;
					} catch (InvocationTargetException e) {
						if ((e.getTargetException() instanceof TestAbortedException)) {
							include = false;
							break;
						}
					} catch (Exception e) {
						if ((e.getCause() instanceof TestAbortedException)) {
							include = false;
							break;
						}
					}
				} else {
					throw new IllegalStateException("Cannot execute non-static @BeforeAll: " + method);
				}
			}
		}
		if (!include) {
			listener.executionSkipped(test, "Assumptions failed");
		}
		return include;
	}

	private List<TestDescriptor> executeAfters(List<TestDescriptor> methods) {
		List<TestDescriptor> filtered = new ArrayList<>();
		for (TestDescriptor test : methods) {
			if (test instanceof MethodAware) {
				MethodAware aware = (MethodAware) test;
				Class<?> testClass = aware.getMethod().getDeclaringClass();
				for (Method method : testClass.getDeclaredMethods()) {
					if (method.isAnnotationPresent(AfterAll.class)) {
						if (Modifier.isStatic(method.getModifiers())) {
							try {
								method.invoke(null);
								filtered.add(test);
							} catch (RuntimeException e) {
								throw e;
							} catch (Exception e) {
								throw new IllegalStateException("Cannot invoke before method", e);
							}
						} else {
							throw new IllegalStateException("Cannot execute non-static @AfterAll: " + method);
						}
					}
				}
			}
		}
		return filtered;
	}

	private List<TestDescriptor> collectBenchmarkMethods(EngineExecutionListener listener, TestDescriptor testDescriptor,
			ChainedOptionsBuilder options) {
		List<TestDescriptor> methods = new ArrayList<>();
		Set<Class<?>> seen = new HashSet<>();
		Set<Class<?>> excluded = new HashSet<>();
		testDescriptor.accept(it -> {

			if (it instanceof BenchmarkMethodDescriptor || it instanceof ParametrizedBenchmarkMethodDescriptor) {
				Method method = ((MethodAware) it).getMethod();
				Class<?> testClass = method.getDeclaringClass();
				if (excluded.contains(testClass)) {
					options.exclude(Pattern.quote(testClass.getName()) + "\\." + Pattern.quote(method.getName()) + "$");
					return;
				}
				if (!seen.contains(testClass)) {
					seen.add(testClass);
					if (!executeBefores(listener, it, testClass)) {
						excluded.add(testClass);
						options.exclude(Pattern.quote(testClass.getName()) + "\\." + Pattern.quote(method.getName()) + "$");
						return;
					}
				}
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
					.map(it -> Pattern.quote(it.getDeclaringClass().getName()) + "\\." + Pattern.quote(it.getName()) + "$")
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
			return tests.contains(javaClass.getName()) || tests.contains(javaClass.getSimpleName());
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
	 * {@link OutputFormat} that delegates to another {@link OutputFormat} and notifies {@link RunNotifier} about the
	 * progress.
	 */
	static class NotifyingOutputFormat implements OutputFormat {

		private final EngineExecutionListener listener;
		private final CacheFunction descriptionResolver;
		private final OutputFormat delegate;
		private final List<String> log = new CopyOnWriteArrayList<>();
		private final Map<TestDescriptor, AtomicInteger> expectedContainerCount = new ConcurrentHashMap<>();

		private volatile BenchmarkParams lastKnownBenchmark;
		private volatile boolean recordOutput;

		NotifyingOutputFormat(EngineExecutionListener listener, CacheFunction methods, OutputFormat delegate) {
			this.listener = listener;
			this.descriptionResolver = methods;
			this.delegate = delegate;
		}

		@Override
		public void iteration(BenchmarkParams benchParams, IterationParams params, int iteration) {
			delegate.iteration(benchParams, params, iteration);
		}

		@Override
		public void iterationResult(BenchmarkParams benchParams, IterationParams params, int iteration,
				IterationResult data) {
			delegate.iterationResult(benchParams, params, iteration, data);
		}

		@Override
		public void startBenchmark(BenchmarkParams benchParams) {

			log.clear();

			lastKnownBenchmark = benchParams;

			TestDescriptor descriptor = descriptionResolver.apply(benchParams);

			if (descriptor != null) {
				listener.executionStarted(descriptor);
				delegate.startBenchmark(benchParams);
			}
		}

		@Override
		public void endBenchmark(BenchmarkResult result) {

			recordOutput = false;

			BenchmarkParams lastKnownBenchmark = this.lastKnownBenchmark;
			TestExecutionResult executionResult = getResult(result, lastKnownBenchmark);
			TestDescriptor descriptor = getDescriptor(result, lastKnownBenchmark);

			listener.executionFinished(descriptor, executionResult);

			notifyFinishedRecursively(descriptor, it -> listener.executionFinished(it, executionResult));

			log.clear();
			delegate.endBenchmark(result);
		}

		private void notifyFinishedRecursively(TestDescriptor descriptor, Consumer<TestDescriptor> visitor) {

			Optional<TestDescriptor> parent = descriptor.getParent();

			while (parent.isPresent()) {

				TestDescriptor actualParent = parent.get();
				AtomicInteger integer = expectedContainerCount.computeIfAbsent(actualParent, it -> {

					AtomicInteger childCount = new AtomicInteger(0);

					it.accept(item -> {
						if (item instanceof BenchmarkMethodDescriptor || item instanceof BenchmarkFixtureDescriptor) {
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

		private TestExecutionResult getResult(BenchmarkResult result, BenchmarkParams lastKnownBenchmark) {

			if (result != null) {
				return TestExecutionResult.successful();
			}

			if (lastKnownBenchmark != null) {

				String output = StringUtils.collectionToDelimitedString(log, System.getProperty("line.separator"));
				return TestExecutionResult.failed(new JmhRunnerException(output));
			}

			return TestExecutionResult.successful();
		}

		private TestDescriptor getDescriptor(BenchmarkResult result, BenchmarkParams lastKnownBenchmark) {

			if (result != null) {
				return descriptionResolver.apply(result.getParams());
			}

			if (this.lastKnownBenchmark != null) {
				return descriptionResolver.apply(lastKnownBenchmark);
			}

			throw new IllegalStateException("Cannot obtain TestDescriptor");
		}

		@Override
		public void startRun() {
			delegate.startRun();
		}

		@Override
		public void endRun(Collection<RunResult> result) {
			delegate.endRun(result);
		}

		@Override
		public void print(String s) {
			delegate.print(s);
		}

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

		@Override
		public void flush() {
			delegate.flush();
		}

		@Override
		public void close() {
			delegate.close();
		}

		@Override
		public void verbosePrintln(String s) {
			delegate.verbosePrintln(s);
		}

		@Override
		public void write(int b) {
			delegate.write(b);
		}

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
		 * Resolve a benchmark name (fqcn + "." + method name) to a {@link TestDescriptor}.
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

				Optional<TestDescriptor> method = methods.stream().filter(it -> getBenchmarkName(it).equals(key)).findFirst();

				return method.orElseThrow(() -> new IllegalArgumentException(
						String.format("Cannot resolve %s to a BenchmarkDescriptor!", benchmark.getBenchmark())));
			});
		}

		private String getBenchmarkName(TestDescriptor descriptor) {

			MethodAware methodAware = (MethodAware) descriptor;
			return methodAware.getMethod().getDeclaringClass().getName() + "." + methodAware.getMethod().getName();
		}
	}
}
