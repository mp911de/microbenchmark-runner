/*
 * Copyright 2018 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v20.html
 */
package jmh.mbr.core;

/**
 * SPI for {@link ResultsWriter} plugins. Uses an opaque {@code uri} to specify the desired target where results can be written to.
 *
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
