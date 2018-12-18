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

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.Arrays;
import java.util.Objects;

/**
 * {@link BenchmarkDescriptor} for a {@link org.openjdk.jmh.annotations.Benchmark} {@link Method}.
 */
public class BenchmarkMethod implements BenchmarkDescriptor, MethodAware {

	private final Method method;

	/**
	 * Creates a new {@link BenchmarkMethod} for {@link Method}.
	 *
	 * @param method the underlying {@link Method}.
	 */
	public BenchmarkMethod(Method method) {

		Objects.requireNonNull(method, "Method must not be null!");

		this.method = method;
	}

	@Override
	public Method getMethod() {
		return method;
	}

	@Override
	public boolean isUnderlyingMethod(Method method) {
		return this.method.equals(method);
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
	 * @return the annotations on this method.
	 */
	public Annotation[] getAnnotations() {
		return method.getAnnotations();
	}

	/**
	 * Returns the annotation of type {@code annotationType} on this method, if one exists.
	 *
	 * @param annotationType type of annotation to retrieve.
	 * @param <T> annotation type.
	 * @return the annotation or {@literal null} if not found.
	 */
	public <T extends Annotation> T getAnnotation(Class<T> annotationType) {
		return method.getAnnotation(annotationType);
	}

	@Override
	public String toString() {
		return method.toString();
	}

	/**
	 * @return {@literal true} whether the method is a parametrized one.
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
