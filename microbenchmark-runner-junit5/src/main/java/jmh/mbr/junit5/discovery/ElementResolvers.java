/*
 * Copyright 2018 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v20.html
 */
package jmh.mbr.junit5.discovery;

import java.util.LinkedHashSet;
import java.util.Set;

/**
 * Utility to obtain {@link ElementResolver}s.
 */
class ElementResolvers {

	/**
	 * @return a {@link Set} of known {@link ElementResolver}s.
	 */
	public static Set<ElementResolver> getResolvers() {

		Set<ElementResolver> resolvers = new LinkedHashSet<>();
		resolvers.add(new BenchmarkContainerResolver());
		resolvers.add(new BenchmarkMethodResolver());
		resolvers.add(new BenchmarkFixtureResolver());

		return resolvers;
	}
}
