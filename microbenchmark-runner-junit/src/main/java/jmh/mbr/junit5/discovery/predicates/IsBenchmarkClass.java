/*
 * Copyright 2018 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v20.html
 */
package jmh.mbr.junit5.discovery.predicates;

import java.util.Arrays;
import java.util.function.Predicate;
import java.util.stream.Stream;

/**
 * {@link Predicate} to check whether a {@link Class} contains {@code @Benchmark} methods.
 *
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
