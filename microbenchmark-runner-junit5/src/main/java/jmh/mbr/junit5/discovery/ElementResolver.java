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
package jmh.mbr.junit5.discovery;

import java.lang.reflect.AnnotatedElement;
import java.util.Optional;
import java.util.Set;

import org.junit.platform.engine.TestDescriptor;
import org.junit.platform.engine.UniqueId;

/**
 * @author Mark Paluch
 */
interface ElementResolver {

	/**
	 * Return a set of {@link TestDescriptor TestDescriptors} that can be resolved by this resolver.
	 * <p>
	 * Returned set must be empty if {@code element} cannot be resolved.
	 */
	Set<TestDescriptor> resolveElement(AnnotatedElement element, TestDescriptor parent);

	/**
	 * Return an optional {@link TestDescriptor}.
	 * <p>
	 * Return {@code Optional.empty()} if {@code segment} cannot be resolved.
	 */
	Optional<TestDescriptor> resolveUniqueId(UniqueId.Segment segment, TestDescriptor parent);

}
