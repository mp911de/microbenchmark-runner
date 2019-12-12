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

import jmh.mbr.extras.writer.ElasticsearchResultsWriter.ConnectionString;
import org.junit.jupiter.api.Test;

class ElasticsearchResultsWriterUnitTests {

	// todo: test all the escaping paths for usernames containing : and @ but that is furture work

	@Test
	void justHostAndPort() {

		ConnectionString connectionString = ElasticsearchResultsWriter.ConnectionString.fromUri("elasticsearch://es-server:666");
		assertThat(connectionString.host).isEqualTo("es-server");
		assertThat(connectionString.port).isEqualTo(666);
		assertThat(connectionString.password).isNull();
		assertThat(connectionString.username).isNull();
		assertThat(connectionString.ssl).isFalse();
	}

	@Test
	void justServicename() {

		ConnectionString connectionString = ElasticsearchResultsWriter.ConnectionString.fromUri("elasticsearch");
		assertThat(connectionString.host).isEqualTo("localhost");
		assertThat(connectionString.port).isEqualTo(9200);
		assertThat(connectionString.password).isNull();
		assertThat(connectionString.username).isNull();
		assertThat(connectionString.ssl).isFalse();
	}

	@Test
	void withSSL() {

		ConnectionString connectionString = ElasticsearchResultsWriter.ConnectionString.fromUri("elasticsearchs://es-server:666");
		assertThat(connectionString.host).isEqualTo("es-server");
		assertThat(connectionString.port).isEqualTo(666);
		assertThat(connectionString.password).isNull();
		assertThat(connectionString.username).isNull();
		assertThat(connectionString.ssl).isTrue();
	}

	@Test
	void withUserPassword() {

		ConnectionString connectionString = ElasticsearchResultsWriter.ConnectionString.fromUri("elasticsearch://es-user:es-pwd@es-host:666");
		assertThat(connectionString.host).isEqualTo("es-host");
		assertThat(connectionString.port).isEqualTo(666);
		assertThat(connectionString.password).isEqualTo("es-pwd".toCharArray());
		assertThat(connectionString.username).isEqualTo("es-user");
		assertThat(connectionString.ssl).isFalse();
	}
}
