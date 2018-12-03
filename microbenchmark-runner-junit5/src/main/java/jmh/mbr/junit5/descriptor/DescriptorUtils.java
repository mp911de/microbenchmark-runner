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
 *
 * @author Mark Paluch
 */
class DescriptorUtils {

	private static final Logger logger = LoggerFactory.getLogger(BenchmarkClassDescriptor.class);

	protected static Set<TestTag> getTags(AnnotatedElement element) {

		return AnnotationUtils.findRepeatableAnnotations(element, Tag.class).stream().map(Tag::value).filter((tag) -> {
			boolean isValid = TestTag.isValid(tag);
			if (!isValid) {
				logger.warn(() -> {
					return String.format("Configuration error: invalid tag syntax in @Tag(\"%s\") declaration on [%s]. Tag will be ignored.", tag, element);
				});
			}

			return isValid;
		}).map(TestTag::create).collect(Collectors.collectingAndThen(Collectors.toCollection(LinkedHashSet::new), Collections::unmodifiableSet));
	}
}
