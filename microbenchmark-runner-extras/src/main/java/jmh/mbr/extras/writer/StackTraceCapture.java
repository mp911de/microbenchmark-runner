/*
 * Copyright 2020 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v20.html
 */
package jmh.mbr.extras.writer;

import java.io.PrintWriter;
import java.io.StringWriter;

/**
 * Utility to capture a {@link Throwable#getStackTrace() stack trace} to {@link String}.
 */
class StackTraceCapture {

	static String from(Throwable throwable) {
		StringWriter trace = new StringWriter();
		throwable.printStackTrace(new PrintWriter(trace));
		return trace.toString();
	}
}
