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

import jmh.mbr.core.ResultsWriter;
import jmh.mbr.core.ResultsWriterFactory;

/**
 * {@link ResultsWriterFactory} for Elasticsearch.
 * Activated with <code>-Djmh.mbr.report.publishTo=elasticsearch://[username]:[password]@[host]:[port]/</code>. The index is derived from the project name ({@code jmh.mbr.project}).
 *
 * @see ElasticsearchResultsWriter
 */
public class ElasticserachResultsWriterFactory implements ResultsWriterFactory {

	@Override
	public ResultsWriter forUri(String uri) {

		if (uri == null || !uri.startsWith("elasticsearch")) {
			return null;
		}

		return new ElasticsearchResultsWriter(uri);
	}
}
