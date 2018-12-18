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

import lombok.Data;
import lombok.RequiredArgsConstructor;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Utility class to discover {@link BenchmarkArgument} along with their parameter values from {@link StateClass state
 * classes}.
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
	@RequiredArgsConstructor
	static class BenchmarkArgument {

		final String name;
		final Set<String> parameters = new LinkedHashSet<>();

		int getParameterCount() {
			return parameters.size();
		}
	}

}
