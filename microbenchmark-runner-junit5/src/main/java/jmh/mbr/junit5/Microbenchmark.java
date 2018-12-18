/*
 * Copyright 2018 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v20.html
 */
package jmh.mbr.junit5;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.junit.platform.commons.annotation.Testable;

/**
 * {@code @Microbenchmark} is used to signal that the annotated type is a <em>microbenchmark class</em> that should be
 * ran using the Microbenchmark Runner.
 * <p>
 * Benchmark classes executed with the Microbenchmark Runner can participate in tooling support without the need to
 * integrate with JMH-specific tool support.
 */
@Retention(RetentionPolicy.SOURCE)
@Target(ElementType.TYPE)
@Documented
@Testable
public @interface Microbenchmark {
}
