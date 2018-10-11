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

import lombok.EqualsAndHashCode;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Represents a parametrized fixture.
 * 
 * @author Mark Paluch
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
	 * @param name
	 * @param parameter
	 * @return
	 */
	public static BenchmarkFixture create(String name, String parameter) {
		return new BenchmarkFixture(Collections.singletonMap(name, parameter));
	}

	/**
	 * Create an enhanced {@link BenchmarkFixture} that contains all parameter values and the given parameter tuple.
	 * 
	 * @param name
	 * @param parameter
	 * @return
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

	/* 
	 * (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		StringBuffer sb = new StringBuffer();
		sb.append(getClass().getSimpleName());
		sb.append(fixture);
		return sb.toString();
	}
}
