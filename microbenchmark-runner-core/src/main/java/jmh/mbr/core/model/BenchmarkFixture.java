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

import lombok.EqualsAndHashCode;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Represents a parametrized fixture.
 */
@EqualsAndHashCode
public class BenchmarkFixture implements BenchmarkDescriptor {

	private final Map<String, Object> fixture;

	private BenchmarkFixture(Map<String, Object> fixture) {
		this.fixture = fixture;
	}

	/**
	 * Create a {@link BenchmarkFixture}.
	 *
	 * @param name name of the fixture parameter. Typically a field name.
	 * @param parameter value of the fixture parameter.
	 * @return the {@link BenchmarkFixture}.
	 * @see org.openjdk.jmh.annotations.Param
	 */
	public static BenchmarkFixture create(String name, String parameter) {
		return new BenchmarkFixture(Collections.singletonMap(name, parameter));
	}

	/**
	 * Create an enhanced {@link BenchmarkFixture} that contains all parameter values and the given parameter tuple.
	 *
	 * @param name name of the fixture parameter. Typically a field name.
	 * @param parameter value of the fixture parameter.
	 * @return the {@link BenchmarkFixture}.
	 */
	public BenchmarkFixture enhance(String name, String parameter) {

		Map<String, Object> fixture = new LinkedHashMap<>(this.fixture.size() + 1);
		fixture.putAll(this.fixture);
		fixture.put(name, parameter);

		return new BenchmarkFixture(fixture);
	}

	public Map<String, Object> getFixture() {
		return fixture;
	}

	public String getDisplayName() {

		String name = fixture.toString();
		if (name.startsWith("{") && name.endsWith("}")) {
			return "[" + name.substring(1, name.length() - 1) + "]";
		}

		return name;
	}

	@Override
	public String toString() {
		StringBuffer sb = new StringBuffer();
		sb.append(getClass().getSimpleName());
		sb.append(fixture);
		return sb.toString();
	}
}
