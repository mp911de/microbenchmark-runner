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

import java.lang.reflect.Method;
import java.util.function.Predicate;

import org.openjdk.jmh.annotations.Benchmark;

/**
 * {@link Predicate} to check whether a {@link Method} is a {@code @Benchmark} method.
 *
 * @see IsBenchmarkClass
 */
public enum IsBenchmarkMethod implements Predicate<Method> {

	INSTANCE;

	@Override
	public boolean test(Method theMethod) {
		return theMethod.isAnnotationPresent(Benchmark.class);
	}
}
