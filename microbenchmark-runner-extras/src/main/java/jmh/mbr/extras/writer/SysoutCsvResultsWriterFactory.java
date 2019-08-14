/*
 * Copyright 2019 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v20.html
 */
package jmh.mbr.extras.writer;

import jmh.mbr.core.ResultsWriter;
import jmh.mbr.core.ResultsWriterFactory;

/**
 * A {@link ResultsWriterFactory} that writes the output in csv format (to the console). Activated with
 * <code>-DpublishTo=sysout</code>.
 */
public class SysoutCsvResultsWriterFactory implements ResultsWriterFactory {

	@Override
	public ResultsWriter forUri(String uri) {

		if (uri == null || uri.trim().isEmpty() || uri.equals("sysout")) {
			return new SysoutCsvResultsWriter(uri);
		}

		return null;
	}
}
