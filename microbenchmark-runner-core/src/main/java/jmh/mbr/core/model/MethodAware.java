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

import java.lang.reflect.Method;

/**
 * Interface exposing {@link Method} awareness of a specific descriptor object.
 */
public interface MethodAware {

	/**
	 * @return the underlying method.
	 */
	Method getMethod();

	/**
	 * Check whether the given {@code method} is represented by this object.
	 *
	 * @param method must not be {@literal null}.
	 * @return {@literal true} if the underlying {@link Method} is {@code method.}
	 */
	boolean isUnderlyingMethod(Method method);
}
