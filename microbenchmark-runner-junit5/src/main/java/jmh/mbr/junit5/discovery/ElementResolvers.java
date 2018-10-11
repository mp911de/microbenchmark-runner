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
package jmh.mbr.junit5.discovery;

import java.util.LinkedHashSet;
import java.util.Set;

/**
 * Utility to obtain {@link ElementResolver}s.
 * 
 * @author Mark Paluch
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
