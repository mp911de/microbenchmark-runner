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
package jmh.mbr.junit5.discovery.predicates;

import java.util.Arrays;
import java.util.function.Predicate;
import java.util.stream.Stream;

/**
 * {@link Predicate} to check whether a {@link Class} contains {@code @Benchmark} methods.
 * 
 * @author Mark Paluch
 * @see IsBenchmarkMethod
 */
public enum IsBenchmarkClass implements Predicate<Class<?>> {

	INSTANCE;

	@Override
	public boolean test(Class<?> theClass) {
		return Stream.concat(Arrays.stream(theClass.getDeclaredMethods()), Arrays.stream(theClass.getMethods()))
				.anyMatch(IsBenchmarkMethod.INSTANCE);
	}
}
