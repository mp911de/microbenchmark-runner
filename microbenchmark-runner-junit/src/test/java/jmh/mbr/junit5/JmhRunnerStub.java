/*
 * Copyright 2018-2020 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v20.html
 */
package jmh.mbr.junit5;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import jmh.mbr.core.BenchmarkConfiguration;
import jmh.mbr.core.JmhSupport;
import jmh.mbr.core.model.BenchmarkClass;
import jmh.mbr.core.model.BenchmarkDescriptor;
import jmh.mbr.core.model.BenchmarkDescriptorFactory;
import jmh.mbr.core.model.BenchmarkMethod;
import jmh.mbr.core.model.BenchmarkResults;
import jmh.mbr.junit5.config.MbrConfiguration;
import jmh.mbr.junit5.descriptor.BenchmarkClassDescriptor;
import jmh.mbr.junit5.descriptor.BenchmarkMethodDescriptor;
import jmh.mbr.junit5.execution.JmhRunner;
import org.junit.jupiter.engine.extension.MutableExtensionRegistry;
import org.junit.platform.engine.EngineExecutionListener;
import org.junit.platform.engine.TestDescriptor;
import org.junit.platform.engine.TestExecutionResult;
import org.junit.platform.engine.UniqueId;
import org.junit.platform.engine.reporting.ReportEntry;
import org.openjdk.jmh.results.RunResult;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.format.OutputFormat;
import org.openjdk.jmh.runner.options.Options;

/**
 * {@link JmhRunner} that collects run results.
 */
public class JmhRunnerStub extends JmhRunner {

	List<RunData> runData = new ArrayList<>();
	List<Collection<RunResult>> stubResults = new ArrayList<>();
	JmhSupportStub supportStub;

	public JmhRunnerStub(MbrConfiguration configuration, MutableExtensionRegistry extensionRegistry) {
		super(configuration, extensionRegistry);
	}

	public void execute(Class<?> benchmarkClass) {
		execute(benchmarkClass, null);
	}

	public void execute(Class<?> benchmarkClass, EngineExecutionListener listener) {
		super.execute(createDescriptor(benchmarkClass), new ArgumentCapturingEngineExecutionListener(listener));
	}

	@Override
	protected Collection<RunResult> runBenchmarks(Options options, OutputFormat outputFormat) throws RunnerException {

		this.runData.add(new RunData(options, outputFormat));

		if (stubResults.isEmpty()) {
			return super.runBenchmarks(options, outputFormat);
		}
		return getNextResults();
	}


	public JmhRunnerStub onRunReturn(Collection<RunResult> results) {

		stubResults.add(results);
		return this;
	}

	public JmhRunnerStub onRunReturnEmptyResult() {
		return onRunReturn(Collections.emptyList());
	}

	@Override
	protected JmhSupport initJmhSupport(BenchmarkConfiguration parameters) {
		supportStub = new JmhSupportStub(parameters);
		return supportStub;
	}

	public RunData getRunData(int run) {
		return runData.get(run);
	}

	public RunData getRunData() {

		if (runData.isEmpty()) {
			return null;
		}

		return runData.get(runData.size() - 1);
	}

	public BenchmarkConfiguration getJmhInitOptions() {
		return supportStub != null ? supportStub.getInitOptions() : null;
	}

	public BenchmarkResults getResult() {
		return supportStub != null ? supportStub.getBenchmarkResults() : null;
	}

	private BenchmarkClassDescriptor createDescriptor(Class<?> javaClass) {

		BenchmarkClass benchmarkClass = BenchmarkDescriptorFactory.create(javaClass)
				.createDescriptor();

		BenchmarkClassDescriptor descriptor = new BenchmarkClassDescriptor(UniqueId
				.root("root", "root"), benchmarkClass);

		for (BenchmarkDescriptor child : benchmarkClass.getChildren()) {

			if (child instanceof BenchmarkMethod) {
				BenchmarkMethod method = (BenchmarkMethod) child;

				descriptor.addChild(new BenchmarkMethodDescriptor(descriptor.getUniqueId()
						.append("method", (method).getName()), method));
			}
		}

		return descriptor;
	}

	private Collection<RunResult> getNextResults() {

		int index = runData.size() - 1;
		if (stubResults.size() >= index) {
			return stubResults.get(index);
		}
		return Collections.emptyList();
	}


	static class RunData {

		Options options;
		OutputFormat outputFormat;

		public RunData(Options options, OutputFormat outputFormat) {
			this.options = options;
			this.outputFormat = outputFormat;
		}

		@Override
		public String toString() {
			return "RunData{" +
					"options=" + options +
					", outputFormat=" + outputFormat +
					'}';
		}
	}

	static class ArgumentCapturingEngineExecutionListener implements EngineExecutionListener {

		EngineExecutionListener delegate;

		List<TestDescriptor> dynamic = new ArrayList<>();
		List<TestDescriptor> skipped = new ArrayList<>();
		List<TestDescriptor> started = new ArrayList<>();
		List<TestDescriptor> finished = new ArrayList<>();
		List<TestDescriptor> published = new ArrayList<>();

		public ArgumentCapturingEngineExecutionListener(EngineExecutionListener delegate) {
			this.delegate = delegate;
		}

		@Override
		public void dynamicTestRegistered(TestDescriptor testDescriptor) {

			dynamic.add(testDescriptor);
			if (delegate != null) {
				delegate.dynamicTestRegistered(testDescriptor);
			}
		}

		@Override
		public void executionSkipped(TestDescriptor testDescriptor, String reason) {
			skipped.add(testDescriptor);
			if (delegate != null) {
				delegate.executionSkipped(testDescriptor, reason);
			}
		}

		@Override
		public void executionStarted(TestDescriptor testDescriptor) {
			started.add(testDescriptor);
			if (delegate != null) {
				delegate.executionStarted(testDescriptor);
			}
		}

		@Override
		public void executionFinished(TestDescriptor testDescriptor, TestExecutionResult testExecutionResult) {
			finished.add(testDescriptor);
			if (delegate != null) {
				delegate.executionFinished(testDescriptor, testExecutionResult);
			}
		}

		@Override
		public void reportingEntryPublished(TestDescriptor testDescriptor, ReportEntry entry) {
			published.add(testDescriptor);
			if (delegate != null) {
				delegate.reportingEntryPublished(testDescriptor, entry);
			}
		}
	}
}
