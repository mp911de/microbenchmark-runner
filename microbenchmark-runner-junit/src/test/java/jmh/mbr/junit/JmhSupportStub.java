/*
 * Copyright 2019-2020 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v20.html
 */
package jmh.mbr.junit;

import java.util.ArrayList;
import java.util.List;

import jmh.mbr.core.BenchmarkConfiguration;
import jmh.mbr.core.JmhSupport;
import jmh.mbr.core.model.BenchmarkResults;
import org.openjdk.jmh.runner.format.OutputFormat;

/**
 * Stub for {@link jmh.mbr.junit.execution.JmhRunner} that collects results into {@link List} additional to publication.
 */
public class JmhSupportStub extends JmhSupport {

	final List<BenchmarkResults> resultsList = new ArrayList<>();
	final BenchmarkConfiguration initOptions;

	public JmhSupportStub(BenchmarkConfiguration jmhOptions) {

		super(jmhOptions);
		this.initOptions = jmhOptions;
	}

	@Override
	public void publishResults(OutputFormat output, BenchmarkResults results) {

		this.resultsList.add(results);
		super.publishResults(output, results);
	}

	public BenchmarkResults getBenchmarkResults() {

		if (resultsList.isEmpty()) {
			return null;
		}

		return resultsList.get(resultsList.size() - 1);
	}

	public BenchmarkConfiguration getInitOptions() {
		return initOptions;
	}
}
