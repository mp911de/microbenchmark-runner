/*
 * Copyright 2018 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v20.html
 */
package jmh.mbr.core;

import java.util.Collection;

import org.openjdk.jmh.results.RunResult;
import org.openjdk.jmh.runner.format.OutputFormat;

public class TestResultsWriterFactory implements ResultsWriterFactory {

	@Override
	public ResultsWriter forUri(String uri) {
		return uri.equals("urn:empty") ? new TestResultsWriter() : null;
	}

	class TestResultsWriter implements ResultsWriter {
		@Override
		public void write(OutputFormat output, Collection<RunResult> results) {
		}
	}
}
