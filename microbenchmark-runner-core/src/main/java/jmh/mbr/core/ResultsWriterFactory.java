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
package jmh.mbr.core;

/**
 * SPI for {@link ResultsWriter} plugins. Uses an opaque {@code uri} to specify the desired target where results can be written to.
 *
 * @author Dave Syer
 * @author Mark Paluch
 * @see java.util.ServiceLoader
 */
public interface ResultsWriterFactory {

	/**
	 * Creates a new {@link ResultsWriter} for {@code uri}.
	 * Implementations may return {@literal null} if the {@code uri} is not supported.
	 *
	 * @param uri target location to which results are written to.
	 * @return the {@link ResultsWriter} implementation or {@literal null} if the {@code uri} is not supported.
	 */
	ResultsWriter forUri(String uri);
}
