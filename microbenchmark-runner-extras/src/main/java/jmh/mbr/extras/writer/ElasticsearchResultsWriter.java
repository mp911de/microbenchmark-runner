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

import java.io.IOException;

import jmh.mbr.core.ResultsWriter;
import jmh.mbr.core.model.BenchmarkResults;
import jmh.mbr.core.model.BenchmarkResults.BenchmarkResult;
import org.apache.http.HttpHost;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestClientBuilder;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.xcontent.XContentType;
import org.openjdk.jmh.runner.format.OutputFormat;

public class ElasticsearchResultsWriter implements ResultsWriter {

	private final RestHighLevelClient client;

	public ElasticsearchResultsWriter(String uri) {
		this(createClient(ConnectionString.fromUri(uri)));
	}

	ElasticsearchResultsWriter(RestHighLevelClient client) {
		this.client = client;
	}

	@Override
	public void write(OutputFormat output, BenchmarkResults results) {
		results.forEach(this::formatAndPublish);
	}

	private void formatAndPublish(BenchmarkResult result) {
		publishJson(result.getMetaData().getProject(), result.map(JsonResultsFormatter::format));
	}

	void publishJson(String index, String json) {

		IndexRequest request = new IndexRequest(index);
		request.source(json, XContentType.JSON);

		try {
			client.index(request, RequestOptions.DEFAULT);
		} catch (IOException e) {
			System.err.print(e);
		}
	}

	static RestHighLevelClient createClient(ConnectionString connectionString) {

		RestClientBuilder builder = RestClient.builder(connectionString.getHttpHost());

		// TODO: set auth header

		return new RestHighLevelClient(builder);
	}

	/**
	 * elasticsearch://[username]:[password]@[host]:[port]/
	 *
	 * @author Christoph Strobl
	 */
	static class ConnectionString {

		final String host;
		final int port;
		final String username;
		final char[] password;
		final boolean ssl;

		ConnectionString(String host, int port, String username, char[] password, boolean ssl) {

			this.host = host;
			this.port = port;
			this.username = username;
			this.password = password;
			this.ssl = ssl;
		}

		static ConnectionString fromUri(String uri) {

			boolean ssl = ssl(uri);

			if (!uri.contains("://")) {
				return new ConnectionString("localhost", 9200, null, null, ssl);
			}

			String pruned = uri.substring(uri.indexOf("://") + 3);

			UserPassword upw = UserPassword.from(pruned);
			HostPort hostPort = HostPort.from(pruned);

			return new ConnectionString(hostPort.host, hostPort.port, upw.username, upw.password, ssl);
		}

		private static boolean ssl(String uri) {
			return uri.startsWith("elasticsearchs");
		}

		HttpHost getHttpHost() {
			return new HttpHost(this.host, this.port, ssl ? "https" : "http");
		}


		boolean hasCredentials() {
			return false;
		}

		private static class UserPassword {

			final String username;
			final char[] password;

			public UserPassword(String username, char[] password) {
				this.username = username;
				this.password = password;
			}

			static UserPassword none() {
				return new UserPassword(null, null);
			}

			static UserPassword from(String connectionString) {

				if (!connectionString.contains("@")) {
					return none();
				}

				String[] args = connectionString.split("@");

				if (!args[0].contains(":")) {
					none();
				}

				String[] userPwd = args[0].split(":");
				return new UserPassword(userPwd[0], userPwd[1].toCharArray());
			}
		}

		private static class HostPort {


			final String host;
			final int port;

			public HostPort(String host, int port) {
				this.host = host;
				this.port = port;
			}

			static HostPort local() {
				return new HostPort("localhost", 9200);
			}

			static HostPort from(String connectionString) {

				String part = connectionString;

				if (connectionString.contains("@")) {
					String[] args = connectionString.split("@");
					part = args[1];
				}

				if (!part.contains(":")) {
					return local();
				}

				String[] hostPort = part.split(":");
				return new HostPort(hostPort[0], Integer.parseInt(hostPort[1]));
			}
		}
	}
}
