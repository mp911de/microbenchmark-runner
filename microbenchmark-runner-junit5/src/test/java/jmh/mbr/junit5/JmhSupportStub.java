/*
 * Copyright 2019-2020 the original author or authors.
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
package jmh.mbr.junit5;

import java.util.ArrayList;
import java.util.List;

import jmh.mbr.core.BenchmarkConfiguration;
import jmh.mbr.core.JmhSupport;
import jmh.mbr.core.model.BenchmarkResults;
import org.openjdk.jmh.runner.format.OutputFormat;

/**
 * Stub for {@link jmh.mbr.junit5.execution.JmhRunner} that collects results into {@link List} additional to publication.
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
