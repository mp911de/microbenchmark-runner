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

import java.io.IOException;
import java.util.Base64;

import jmh.mbr.core.ResultsWriter;
import jmh.mbr.core.model.BenchmarkResults;
import jmh.mbr.core.model.BenchmarkResults.BenchmarkResult;
import org.apache.http.Header;
import org.apache.http.HttpHeaders;
import org.apache.http.HttpHost;
import org.apache.http.message.BasicHeader;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestClientBuilder;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.xcontent.XContentType;
import org.openjdk.jmh.runner.format.OutputFormat;

/**
 * {@link ResultsWriter} to write {@link BenchmarkResults} to Elasticserarch.
 */
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
		results.forEach(result -> formatAndPublish(output, result));
	}

	private void formatAndPublish(OutputFormat output, BenchmarkResult result) {
		publishJson(output, result.getMetaData().getProject(), result
				.map(JsonResultsFormatter::format));
	}

	void publishJson(OutputFormat output, String index, String json) {

		IndexRequest request = new IndexRequest(index);
		request.source(json, XContentType.JSON);

		try {
			client.index(request, RequestOptions.DEFAULT);
		}
		catch (IOException e) {
			output.println("Write failed: " + e
					.getMessage() + " " + StackTraceCapture.from(e));
		}
	}

	static RestHighLevelClient createClient(ConnectionString connectionString) {

		RestClientBuilder builder = RestClient.builder(connectionString.getHttpHost());

		if (connectionString.hasCredentials()) {
			builder.setDefaultHeaders(new Header[] {
					new BasicHeader(HttpHeaders.AUTHORIZATION, connectionString
							.getBasicAuth())
			});
		}

		return new RestHighLevelClient(builder);
	}

	/**
	 * elasticsearch://[username]:[password]@[host]:[port]/
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

			boolean ssl = isSsl(uri);

			if (!uri.contains("://")) {
				return new ConnectionString("localhost", 9200, null, null, ssl);
			}

			String authority = uri.substring(uri.indexOf("://") + 3);

			UserPassword upw = UserPassword.from(authority);
			HostPort hostPort = HostPort.from(authority);

			return new ConnectionString(hostPort.host, hostPort.port, upw.username, upw.password, ssl);
		}

		HttpHost getHttpHost() {
			return new HttpHost(this.host, this.port, this.ssl ? "https" : "http");
		}

		boolean hasCredentials() {
			return this.username != null && this.password != null;
		}

		private String getBasicAuth() {

			String credentialsString = this.username + ":" + new String(this.password);
			byte[] encodedBytes = Base64.getEncoder()
					.encode(credentialsString.getBytes());
			return "Basic " + new String(encodedBytes);
		}

		private static boolean isSsl(String uri) {
			return uri.startsWith("elasticsearchs");
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
					return none();
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
