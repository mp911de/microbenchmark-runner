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

import lombok.Data;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Utility class to discover {@link BenchmarkArgument} along with their parameter values from {@link StateClass state
 * classes}.
 * 
 * @author Mark Paluch
 */
class BenchmarkParameters {

	static Collection<BenchmarkArgument> discover(List<StateClass> stateClasses) {

		Map<String, BenchmarkArgument> argumentMap = new LinkedHashMap<>();

		stateClasses.stream().map(StateClass::getParametrizedFields).flatMap(Collection::stream).forEach(it -> {

			List<String> parameterValues = StateClass.getParameterValues(it);

			BenchmarkArgument benchmarkArgument = argumentMap.computeIfAbsent(it.getName(), BenchmarkArgument::new);
			benchmarkArgument.getParameters().addAll(parameterValues);
		});

		return argumentMap.values();
	}

	@Data
	static class BenchmarkArgument {

		final String name;
		final Set<String> parameters = new LinkedHashSet<>();

		public BenchmarkArgument(String name) {
			this.name = name;
		}

		public int getParameterCount() {
			return parameters.size();
		}
	}

}
