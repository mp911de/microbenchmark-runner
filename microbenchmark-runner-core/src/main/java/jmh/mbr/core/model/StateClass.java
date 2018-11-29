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

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.openjdk.jmh.annotations.Param;
import org.openjdk.jmh.annotations.State;

import lombok.RequiredArgsConstructor;

/**
 * Value object to encapsulate a JMH {@code @State} class.
 * 
 * @author Mark Paluch
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

		return Stream
				.concat(Arrays.stream(stateClass.getFields()),
						Arrays.stream(stateClass.getDeclaredFields()))
				.anyMatch(it -> it.isAnnotationPresent(Param.class));
	}

	/**
	 * @param field
	 * @return the possible {@link Param} values for a {@link Field}.
	 */
	public static List<String> getParameterValues(Field field) {

		Param annotation = field.getAnnotation(Param.class);

		if (annotation != null
				&& (annotation.value().length == 0 || (annotation.value().length == 1
						&& annotation.value()[0].equals(Param.BLANK_ARGS)))
				&& field.getType().isEnum()) {
			return Arrays.asList(field.getType().getEnumConstants()).stream()
					.map(Object::toString).collect(Collectors.toList());
		}

		if (annotation == null || annotation.value().length == 0
				|| (annotation.value().length == 1
						&& annotation.value()[0].equals(Param.BLANK_ARGS))) {
			return Collections.emptyList();
		}

		return Arrays.asList(annotation.value());
	}

	/**
	 * @return {@link List}of {@link Param} fields.
	 */
	public List<Field> getParametrizedFields() {

		return Stream
				.concat(Arrays.stream(stateClass.getFields()),
						Arrays.stream(stateClass.getDeclaredFields()))
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
