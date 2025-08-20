/*
 * Copyright 2018-2019 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v20.html
 */
package jmh.mbr.junit5.execution;

import java.io.IOException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
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
import java.util.stream.Stream;

import jmh.mbr.core.BenchmarkConfiguration;
import jmh.mbr.core.JmhSupport;
import jmh.mbr.core.StringUtils;
import jmh.mbr.core.model.BenchmarkResults;
import jmh.mbr.core.model.BenchmarkResults.MetaData;
import jmh.mbr.core.model.MethodAware;
import jmh.mbr.junit5.config.MbrConfiguration;
import jmh.mbr.junit5.descriptor.AbstractBenchmarkDescriptor;
import jmh.mbr.junit5.descriptor.BenchmarkClassDescriptor;
import jmh.mbr.junit5.descriptor.BenchmarkFixtureDescriptor;
import jmh.mbr.junit5.descriptor.BenchmarkMethodDescriptor;
import jmh.mbr.junit5.descriptor.ParametrizedBenchmarkMethodDescriptor;
import org.junit.jupiter.api.extension.ConditionEvaluationResult;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.engine.extension.ExtensionRegistry;
import org.junit.jupiter.engine.extension.MutableExtensionRegistry;
import org.junit.platform.engine.EngineExecutionListener;
import org.junit.platform.engine.TestDescriptor;
import org.junit.platform.engine.TestExecutionResult;
import org.junit.platform.engine.support.hierarchical.Node.SkipResult;
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

/**
 * JMH Benchmark runner.
 */
public class JmhRunner {

	private static final ConditionEvaluator evaluator = new ConditionEvaluator();

	private final MbrConfiguration configuration;
	private final MutableExtensionRegistry extensionRegistry;

	public JmhRunner(MbrConfiguration configuration, MutableExtensionRegistry extensionRegistry) {
		this.configuration = configuration;
		this.extensionRegistry = extensionRegistry;
	}

	public void execute(TestDescriptor testDescriptor, EngineExecutionListener listener) {

		BenchmarkConfiguration jmhOptions = new ConfigurationParameterBenchmarkConfiguration(configuration);
		JmhSupport support = initJmhSupport(jmhOptions);

		ChainedOptionsBuilder optionsBuilder = support.options();

		List<AbstractBenchmarkDescriptor> methods = collectBenchmarkMethods(testDescriptor);
		List<AbstractBenchmarkDescriptor> includes = getIncludes(testDescriptor);

		if (!support.isEnabled()) {
			listener.executionSkipped(testDescriptor, "No benchmarks");
			return;
		}

		List<String> includePatterns = evaluateBenchmarksToRun(includes, listener);

		if (!shouldRun(includePatterns)) {
			return;
		}

		includePatterns.forEach(optionsBuilder::include);

		CacheFunction cache = new CacheFunction(methods);
		Options runOptions = optionsBuilder.build();
		NotifyingOutputFormat notifyingOutputFormat = new NotifyingOutputFormat(listener, cache,
				support.createOutputFormat(runOptions));

		try {
			listener.executionStarted(testDescriptor);
			for (TestDescriptor child : testDescriptor.getChildren()) {
				listener.executionStarted(child);
			}

			support.publishResults(notifyingOutputFormat, new BenchmarkResults(MetaData
					.from(jmhOptions
							.asMap()), runBenchmarks(runOptions, notifyingOutputFormat)));
			listener.executionFinished(testDescriptor, TestExecutionResult.successful());
		} catch (RuntimeException | RunnerException e) {

			listener.executionFinished(testDescriptor, TestExecutionResult.failed(e));
			for (TestDescriptor child : testDescriptor.getChildren()) {
				listener.executionFinished(child, TestExecutionResult.failed(e));
			}
		}
	}

	protected Collection<RunResult> runBenchmarks(Options options, OutputFormat outputFormat) throws RunnerException {
		return new Runner(options, outputFormat).run();
	}

	protected JmhSupport initJmhSupport(BenchmarkConfiguration parameters) {
		return new JmhSupport(parameters);
	}

	private List<AbstractBenchmarkDescriptor> collectBenchmarkMethods(TestDescriptor testDescriptor) {

		List<AbstractBenchmarkDescriptor> methods = new ArrayList<>();

		testDescriptor.accept(it -> {

			if (it instanceof BenchmarkMethodDescriptor || it instanceof ParametrizedBenchmarkMethodDescriptor) {
				methods.add((AbstractBenchmarkDescriptor) it);
			}
		});

		return methods;
	}

	private boolean shouldRun(List<?> methods) {
		return !methods.isEmpty();
	}

	@SuppressWarnings("unchecked")
	private List<AbstractBenchmarkDescriptor> getIncludes(TestDescriptor testDescriptor) {

		String tests = configuration.getRawConfigurationParameter("benchmark").orElse(null);

		List<BenchmarkClassDescriptor> classes = new ArrayList<>();

		testDescriptor.accept(it -> {

			if (it instanceof BenchmarkClassDescriptor) {
				classes.add((BenchmarkClassDescriptor) it);
			}
		});

		if (!StringUtils.hasText(tests)) {
			return (List) classes;
		}

		return classes.stream().filter(it -> {

			Class<?> javaClass = it.getJavaClass();
			return tests.contains(javaClass.getName()) || tests.contains(javaClass.getSimpleName());
		}).flatMap(it -> {

			if (tests.contains("#")) {

				String[] split = tests.split("#");
				String methodNameFilter = split[1];

				return it.getChildren().stream()
						.filter(MethodAware.class::isInstance)
						.map(MethodAware.class::cast)
						.filter(member -> member.getMethod().getName().equals(methodNameFilter));
			} else {
				return Stream.of(it);
			}
		}).map(AbstractBenchmarkDescriptor.class::cast).collect(Collectors.toList());
	}

	protected List<String> evaluateBenchmarksToRun(List<AbstractBenchmarkDescriptor> includes, EngineExecutionListener listener) {

		try (ExtensionContextProvider contextProvider = ExtensionContextProvider.create(listener, configuration)) {

			List<String> includePatterns = new ArrayList<>();

			includes.stream()
					.filter(BenchmarkClassDescriptor.class::isInstance)
					.map(BenchmarkClassDescriptor.class::cast)
					.forEach(descriptor -> {

						ExtensionContext classExtensionContext = contextProvider.getExtensionContext(descriptor);
						List<String> methodIncludePatterns = new ArrayList<>();

						SkipResult shouldRun = shouldRun(classExtensionContext, descriptor, listener);

						if (shouldRun.isSkipped()) {
							listener.executionSkipped(descriptor, shouldRun.getReason().get());
							return;
						}

						descriptor.accept(it -> {
							if (it instanceof MethodAware) {
								shouldRun(classExtensionContext, (MethodAware) it, listener).includeIfEnabled(methodIncludePatterns);
							}
						});

						if (methodIncludePatterns.isEmpty()) {
							listener.executionSkipped(descriptor, "No methods to run");
						} else {
							includePatterns.addAll(methodIncludePatterns);
						}
					});

			includes.stream()
					.filter(MethodAware.class::isInstance)
					.forEach(descriptor -> {
						ExtensionContext parentContext = contextProvider.getExtensionContext(descriptor.getParent());
						shouldRun(parentContext, (MethodAware) descriptor, listener).includeIfEnabled(includePatterns);
					});

			return includePatterns;
		}
	}

	private SkipResult shouldRun(ExtensionContext parent, AbstractBenchmarkDescriptor descriptor, EngineExecutionListener listener) {

		ExtensionRegistry extensionRegistry = descriptor.getExtensionRegistry(this.extensionRegistry);
		ExtensionContext extensionContext = descriptor.getExtensionContext(parent, listener, configuration);

		ConditionEvaluationResult evaluationResult = evaluator.evaluate(extensionRegistry, extensionContext);

		if (evaluationResult.isDisabled()) {
			return SkipResult.skip(evaluationResult.getReason().orElse("<unknown>"));
		}
		return SkipResult.doNotSkip();
	}

	private ConditionalExecution shouldRun(ExtensionContext parent, MethodAware methodAware, EngineExecutionListener listener) {

		AbstractBenchmarkDescriptor descriptor = (AbstractBenchmarkDescriptor) methodAware;
		SkipResult skipResult = shouldRun(parent, descriptor, listener);

		return new ConditionalExecution(skipResult, methodAware);
	}

	/**
	 * Value object representing the outcome of condition evaluation for a benchmark method.
	 */
	static class ConditionalExecution {

		private final SkipResult skipResult;
		private final MethodAware methodAware;

		private ConditionalExecution(SkipResult skipResult, MethodAware methodAware) {
			this.skipResult = skipResult;
			this.methodAware = methodAware;
		}

		public void includeIfEnabled(List<String> includePatterns) {
			if (!skipResult.isSkipped()) {
				Method method = methodAware.getMethod();
				includePatterns.add(Pattern.quote(method.getDeclaringClass().getName().replace('$', '.')) + "\\." + Pattern.quote(method.getName()) + "$");
			}
		}
	}

	/**
	 * {@link OutputFormat} that delegates to another {@link OutputFormat} and notifies {@link EngineExecutionListener} about the
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

			listener.executionStarted(descriptor);

			delegate.startBenchmark(benchParams);
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
		private final Collection<? extends TestDescriptor> methods;

		CacheFunction(Collection<? extends TestDescriptor> methods) {
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

				Optional<? extends TestDescriptor> method = methods.stream().filter(it -> getBenchmarkName(it).equals(key)).findFirst();

				return method.orElseThrow(() -> new IllegalArgumentException(
						String.format("Cannot resolve %s to a BenchmarkDescriptor!", benchmark.getBenchmark())));
			});
		}

		private String getBenchmarkName(TestDescriptor descriptor) {

			MethodAware methodAware = (MethodAware) descriptor;
			return methodAware.getMethod().getDeclaringClass().getName().replace('$', '.') + "." + methodAware.getMethod().getName();
		}
	}
}
