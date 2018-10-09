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

import org.junit.runner.RunWith;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.Fork;
import org.openjdk.jmh.annotations.Measurement;
import org.openjdk.jmh.annotations.Param;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.annotations.Warmup;

/**
 * @author Mark Paluch
 */
@Warmup(iterations = 1, time = 1)
@Measurement(iterations = 1, time = 1)
@Fork(value = 1, warmups = 1)
@RunWith(Microbenchmark.class)
public class PartiallyParametrizedBenchmark {

	@State(Scope.Benchmark)
	public static class ParamState {
		@Param({ "a", "b" }) String foo;
	}

	@Benchmark
	public void foo() {}

	@Benchmark
	public void bar(ParamState paramState) {}
}
