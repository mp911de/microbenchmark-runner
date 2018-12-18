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

import lombok.RequiredArgsConstructor;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.openjdk.jmh.annotations.Param;
import org.openjdk.jmh.annotations.State;

/**
 * Value object to encapsulate a JMH {@code @State} class.
 */
@RequiredArgsConstructor
class StateClass {

	private final Class<?> stateClass;

	/**
	 * Create a {@link StateClass} given {@link Class}.
	 *
	 * @param stateClass
	 * @return
	 */
	public static StateClass create(Class<?> stateClass) {

		Objects.requireNonNull(stateClass, "State class must not be null!");

		return new StateClass(stateClass);
	}

	/**
	 * @param stateClass
	 * @return {@literal true} if the state class is parametrized.
	 * @see Param
	 */
	public static boolean isParametrized(Class<?> stateClass) {

		Objects.requireNonNull(stateClass, "Class must not be null!");

		if (!stateClass.isAnnotationPresent(State.class)) {
			return false;
		}

		return Stream.concat(Arrays.stream(stateClass.getFields()), Arrays.stream(stateClass.getDeclaredFields()))
				.anyMatch(it -> it.isAnnotationPresent(Param.class));
	}

	/**
	 * @param field
	 * @return the possible {@link Param} values for a {@link Field}.
	 */
	public static List<String> getParameterValues(Field field) {

		Param annotation = field.getAnnotation(Param.class);

		if (annotation != null
				&& (annotation.value().length == 0
						|| (annotation.value().length == 1 && annotation.value()[0].equals(Param.BLANK_ARGS)))
				&& field.getType().isEnum()) {
			return Arrays.asList(field.getType().getEnumConstants()).stream().map(Object::toString)
					.collect(Collectors.toList());
		}

		if (annotation == null || annotation.value().length == 0
				|| (annotation.value().length == 1 && annotation.value()[0].equals(Param.BLANK_ARGS))) {
			return Collections.emptyList();
		}

		return Arrays.asList(annotation.value());
	}

	/**
	 * @return {@link List}of {@link Param} fields.
	 */
	public List<Field> getParametrizedFields() {

		return Stream.concat(Arrays.stream(stateClass.getFields()), Arrays.stream(stateClass.getDeclaredFields()))
				.filter(it -> it.isAnnotationPresent(Param.class)) //
				.distinct() //
				.collect(Collectors.toList());
	}

	@Override
	public String toString() {
		StringBuffer sb = new StringBuffer();
		sb.append(getClass().getSimpleName());
		sb.append(" [").append(stateClass.getName());
		sb.append(']');
		return sb.toString();
	}
}
