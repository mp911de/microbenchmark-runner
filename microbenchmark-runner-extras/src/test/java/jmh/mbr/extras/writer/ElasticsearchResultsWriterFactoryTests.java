/*
 * Copyright 2019 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * https://www.eclipse.org/legal/epl-v20.html
 */
package jmh.mbr.extras.writer;

import static org.assertj.core.api.Assertions.*;

import org.junit.jupiter.api.Test;

class ElasticsearchResultsWriterFactoryTests {

	private ElasticserachResultsWriterFactory factory = new ElasticserachResultsWriterFactory();

	@Test
	void nullUri() {
		assertThat(factory.forUri(null)).isNull();
	}

	@Test
	void emptyUri() {
		assertThat(factory.forUri("")).isNull();
	}

	@Test
	void elasticsearchEnablesFactory() {
		assertThat(factory.forUri("elasticsearch")).isNotNull();
	}

	@Test
	void elasticsearchWithSslEnablesFactory() {
		assertThat(factory.forUri("elasticsearchs")).isNotNull();
	}
}
