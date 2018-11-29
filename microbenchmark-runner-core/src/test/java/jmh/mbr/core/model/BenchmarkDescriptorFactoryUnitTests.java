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
package jmh.mbr.core.model;

import static org.assertj.core.api.Assertions.*;

import jmh.mbr.core.model.BenchmarkDescriptorFactoryUnitTests.BenchmarkClass.OneParameter;
import jmh.mbr.core.model.BenchmarkDescriptorFactoryUnitTests.BenchmarkClass.Three1;
import jmh.mbr.core.model.BenchmarkDescriptorFactoryUnitTests.BenchmarkClass.Three2;
import jmh.mbr.core.model.BenchmarkDescriptorFactoryUnitTests.BenchmarkClass.TwoParameters;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.Param;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.State;

/**
 * Unit tests for {@link BenchmarkDescriptorFactory}.
 *
 * @author Mark Paluch
 * @author Dave Syer
 */
class BenchmarkDescriptorFactoryUnitTests {

	@Test
	void shouldNotCreateFixtures() {

		BenchmarkDescriptorFactory factory = BenchmarkDescriptorFactory.create(BenchmarkClass.class);
		BenchmarkMethod simple = factory.getRequiredBenchmarkMethod("simple");

		List<BenchmarkFixture> fixtures = factory.createFixtures(simple);
		assertThat(fixtures).isEmpty();
	}

	@Test
	void shouldCreateOneFixtureForSingleParametrizedMethod() {

		BenchmarkDescriptorFactory factory = BenchmarkDescriptorFactory.create(BenchmarkClass.class);
		BenchmarkMethod single = factory.getRequiredBenchmarkMethod("single", OneParameter.class);

		List<BenchmarkFixture> fixtures = factory.createFixtures(single);
		assertThat(fixtures).hasSize(1);
	}

	@Test
	void shouldCreateMultipleFixtureForParametrizedMethodWithTwoParams() {

		BenchmarkDescriptorFactory factory = BenchmarkDescriptorFactory.create(BenchmarkClass.class);
		BenchmarkMethod single = factory.getRequiredBenchmarkMethod("single", TwoParameters.class);

		List<BenchmarkFixture> fixtures = factory.createFixtures(single);
		assertThat(fixtures).hasSize(2);
	}

	@Test
	void shouldCreateMultipleFixturesForParameterMatrix() {

		BenchmarkDescriptorFactory factory = BenchmarkDescriptorFactory.create(BenchmarkClass.class);
		BenchmarkMethod single = factory.getRequiredBenchmarkMethod("multi", OneParameter.class, TwoParameters.class);

		List<BenchmarkFixture> fixtures = factory.createFixtures(single);
		assertThat(fixtures).hasSize(2);
	}

	@Test
	public void shouldCreateMultipleFixturesFor3x3ParameterMatrix() {

		BenchmarkDescriptorFactory factory = BenchmarkDescriptorFactory.create(BenchmarkClass.class);
		BenchmarkMethod single = factory.getRequiredBenchmarkMethod("nine", Three1.class, Three2.class);

		List<BenchmarkFixture> fixtures = factory.createFixtures(single);
		assertThat(fixtures).hasSize(9);
	}

	@Test
	public void shouldCreateMultipleFixturesParametrizedBenchmarkClass() {

		BenchmarkDescriptorFactory factory = BenchmarkDescriptorFactory.create(ParametrizedBenchmarkClass.class);
		BenchmarkMethod single = factory.getRequiredBenchmarkMethod("simple");

		List<BenchmarkFixture> fixtures = factory.createFixtures(single);
		assertThat(fixtures).hasSize(3);
	}

	@Test
	public void shouldCreateMultipleFixturesEnumParametrizedBenchmarkClass() {

		BenchmarkDescriptorFactory factory = BenchmarkDescriptorFactory.create(EnumParametrizedBenchmarkClass.class);
		BenchmarkMethod single = factory.getRequiredBenchmarkMethod("simple");

		List<BenchmarkFixture> fixtures = factory.createFixtures(single);
		assertThat(fixtures).hasSize(3);
	}

	static class BenchmarkClass {

		@Benchmark
		void simple() {

		}

		@Benchmark
		void single(OneParameter single) {

		}

		@Benchmark
		void single(TwoParameters two) {

		}

		@Benchmark
		void multi(OneParameter one, TwoParameters two) {

		}

		@Benchmark
		void nine(Three1 one, Three2 two) {

		}

		@State(Scope.Benchmark)
		static class OneParameter {

			@Param("bar") String foo;
		}

		@State(Scope.Benchmark)
		static class TwoParameters {

			@Param({ "1", "2" }) String param2;
		}

		@State(Scope.Benchmark)
		static class Three1 {

			@Param({ "1", "2", "3" }) String foo;
		}

		@State(Scope.Benchmark)
		static class Three2 {

			@Param({ "1", "2", "3" }) String bar;
		}
	}

	@State(Scope.Benchmark)
	static class ParametrizedBenchmarkClass {

		@Param({ "1", "2", "3" }) String foo;

		@Benchmark
		void simple() {

		}
	}

	@State(Scope.Benchmark)
	static class EnumParametrizedBenchmarkClass {

		public static enum Sample {
			ONE, TWO, THREE
		}

		@Param Sample foo;

		@Benchmark
		void simple() {

		}
	}
}
