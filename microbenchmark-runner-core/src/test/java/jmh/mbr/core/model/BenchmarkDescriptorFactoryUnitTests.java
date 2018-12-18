/*
 * Copyright 2018 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v20.html
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
