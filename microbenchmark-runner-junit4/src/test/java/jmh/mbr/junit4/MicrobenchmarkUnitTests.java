/*
 * Copyright 2018 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v20.html
 */
package jmh.mbr.junit4;

import static org.assertj.core.api.Assertions.*;

import org.junit.jupiter.api.Test;
import org.junit.runner.Description;
import org.junit.runners.model.InitializationError;

class MicrobenchmarkUnitTests {

	@Test
	void shouldDescribeParametrizedBenchmark() throws InitializationError {

		Microbenchmark runner = new Microbenchmark(ParametrizedBenchmark.class);
		Description description = runner.getDescription();

		assertThat(description.getTestClass()).isEqualTo(ParametrizedBenchmark.class);
		assertThat(description.getMethodName()).isNull();
		assertThat(description.getChildren()).hasSize(2);

		Description fixture = description.getChildren().get(0);

		assertThat(fixture.getTestClass()).isNull();
		assertThat(fixture.getMethodName()).isNull();
		assertThat(fixture.getDisplayName()).isEqualTo("[foo=a]");
		assertThat(fixture.getChildren()).hasSize(1);

		Description method = fixture.getChildren().get(0);

		assertThat(method.getTestClass()).isEqualTo(ParametrizedBenchmark.class);
		assertThat(method.getMethodName()).isEqualTo("foo");
	}

	@Test
	void shouldDescribeSimpleBenchmark() throws InitializationError {

		Microbenchmark runner = new Microbenchmark(SimpleBenchmark.class);
		Description description = runner.getDescription();

		assertThat(description.getTestClass()).isEqualTo(SimpleBenchmark.class);
		assertThat(description.getMethodName()).isNull();
		assertThat(description.getChildren()).hasSize(1);

		Description method = description.getChildren().get(0);

		assertThat(method.getTestClass()).isEqualTo(SimpleBenchmark.class);
		assertThat(method.getMethodName()).isEqualTo("foo");
		assertThat(method.getChildren()).isEmpty();
	}

	@Test
	void shouldDescribePartiallyParametrizedBenchmark() throws InitializationError {

		Microbenchmark runner = new Microbenchmark(PartiallyParametrizedBenchmark.class);
		Description description = runner.getDescription();

		assertThat(description.getTestClass()).isEqualTo(PartiallyParametrizedBenchmark.class);
		assertThat(description.getMethodName()).isNull();
		assertThat(description.getChildren()).hasSize(3);
	}
}
