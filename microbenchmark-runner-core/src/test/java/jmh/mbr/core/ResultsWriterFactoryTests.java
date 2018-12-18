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

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ResultsWriterFactoryTests {

	@Test
	void test() {
		assertThat(ResultsWriter.forUri("urn:empty")).isNotNull();
	}

	@Test
	void empty() {
		assertThat(ResultsWriter.forUri("file:./target")).isNull();
	}

}
