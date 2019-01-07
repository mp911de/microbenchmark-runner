/*
 * Copyright 2018 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v20.html
 */
package jmh.mbr.junit5.descriptor;

import java.lang.reflect.AnnotatedElement;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.stream.Collectors;

import org.junit.jupiter.api.Tag;
import org.junit.platform.commons.logging.Logger;
import org.junit.platform.commons.logging.LoggerFactory;
import org.junit.platform.commons.util.AnnotationUtils;
import org.junit.platform.engine.TestTag;

/**
 * Utilities for {@link org.junit.platform.engine.TestDescriptor}.
 */
class DescriptorUtils {

	private static final Logger logger = LoggerFactory.getLogger(BenchmarkClassDescriptor.class);

	private DescriptorUtils() {
	}

	protected static Set<TestTag> getTags(AnnotatedElement element) {

		return AnnotationUtils.findRepeatableAnnotations(element, Tag.class).stream().map(Tag::value).filter((tag) -> {

			boolean isValid = TestTag.isValid(tag);
			if (!isValid) {
				logger.warn(() -> String.format("Configuration error: invalid tag syntax in @Tag(\"%s\") declaration on [%s]. Tag will be ignored.", tag, element));
			}

			return isValid;
		}).map(TestTag::create)
				.collect(Collectors.collectingAndThen(Collectors.toCollection(LinkedHashSet::new), Collections::unmodifiableSet));
	}
}
