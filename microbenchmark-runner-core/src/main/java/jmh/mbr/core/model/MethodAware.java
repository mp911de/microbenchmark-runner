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
package jmh.mbr.core.model;

import java.lang.reflect.Method;

/**
 * Interface exposing {@link Method} awareness of a specific descriptor object.
 *
 * @author Mark Paluch
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
