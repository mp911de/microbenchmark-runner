/*
 * Copyright 2015-2019 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v20.html
 */
package jmh.mbr.junit5.descriptor;

import java.lang.reflect.AnnotatedElement;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.extension.Extension;
import org.junit.jupiter.engine.extension.ExtensionRegistry;
import org.junit.jupiter.engine.extension.MutableExtensionRegistry;
import org.junit.platform.commons.util.AnnotationUtils;

/**
 * Utility to collect extension classes and create a {@link ExtensionRegistry}.
 */
abstract class ExtensionUtils {

	private ExtensionUtils() {
	}

	static ExtensionRegistry populateNewExtensionRegistryFromExtendWithAnnotation(MutableExtensionRegistry parentRegistry,
			AnnotatedElement annotatedElement) {

		List<Class<? extends Extension>> extensionTypes = AnnotationUtils
				.findRepeatableAnnotations(annotatedElement, ExtendWith.class)
				.stream()
				.map(ExtendWith::value)
				.flatMap(Arrays::stream)
				.collect(Collectors.toList());

		return MutableExtensionRegistry
				.createRegistryFrom(parentRegistry, extensionTypes.stream());
	}
}
