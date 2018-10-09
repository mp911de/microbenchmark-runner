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

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.Arrays;
import java.util.Objects;

/**
 * {@link BenchmarkDescriptor} for a {@link org.openjdk.jmh.annotations.Benchmark} {@link Method}.
 * 
 * @author Mark Paluch
 */
public class BenchmarkMethod implements BenchmarkDescriptor {

	private final Method method;

	/**
	 * Returns a new {@link BenchmarkMethod} for {@link Method}.
	 */
	public BenchmarkMethod(Method method) {

		Objects.requireNonNull(method, "Method must not be null!");

		this.method = method;
	}

	/**
	 * @return the underlying Java method.
	 */
	public Method getMethod() {
		return method;
	}

	/**
	 * @return the method's name.
	 */
	public String getName() {
		return method.getName();
	}

	/**
	 * @return the method's parameters.
	 */
	public Parameter[] getParameters() {
		return method.getParameters();
	}

	/**
	 * @return the method's parameter types.
	 */
	private Class<?>[] getParameterTypes() {
		return method.getParameterTypes();
	}

	/**
	 * @return the return type of the method.
	 */
	public Class<?> getReturnType() {
		return method.getReturnType();
	}

	/**
	 * @return the class where the method is actually declared.
	 */
	public Class<?> getDeclaringClass() {
		return method.getDeclaringClass();
	}

	@Override
	public boolean equals(Object obj) {
		if (!BenchmarkMethod.class.isInstance(obj)) {
			return false;
		}
		return ((BenchmarkMethod) obj).method.equals(method);
	}

	@Override
	public int hashCode() {
		return method.hashCode();
	}

	/**
	 * Returns the annotations on this method
	 */
	public Annotation[] getAnnotations() {
		return method.getAnnotations();
	}

	/**
	 * Returns the annotation of type {@code annotationType} on this method, if one exists.
	 */
	public <T extends Annotation> T getAnnotation(Class<T> annotationType) {
		return method.getAnnotation(annotationType);
	}

	@Override
	public String toString() {
		return method.toString();
	}

	/**
	 * Returns {@literal true} whether the method is a parametrized one.
	 */
	public boolean isParametrized() {

		if (StateClass.isParametrized(method.getDeclaringClass())) {
			return true;
		}

		return Arrays.stream(method.getParameters()) //
				.map(Parameter::getType) //
				.anyMatch(StateClass::isParametrized);
	}
}
