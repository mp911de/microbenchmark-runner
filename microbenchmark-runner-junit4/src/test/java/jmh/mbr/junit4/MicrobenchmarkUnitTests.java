/*
 * Copyright 2018 the original author or authors.
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
package jmh.mbr.junit4;

import static org.assertj.core.api.Assertions.*;

import org.junit.jupiter.api.Test;
import org.junit.runner.Description;
import org.junit.runners.model.InitializationError;

/**
 * @author Mark Paluch
 */
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
