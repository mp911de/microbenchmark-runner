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

import java.util.Collections;
import java.util.Set;

import org.junit.platform.engine.TestSource;
import org.junit.platform.engine.TestTag;
import org.junit.platform.engine.UniqueId;
import org.junit.platform.engine.support.descriptor.AbstractTestDescriptor;
import org.junit.platform.engine.support.descriptor.ClassSource;
import org.junit.platform.engine.support.descriptor.MethodSource;

/**
 * Abstract base class for Benchmark descriptors.
 *
 * @author Mark Paluch
 */
abstract class AbstractBenchmarkDescriptor extends AbstractTestDescriptor {

	private final Set<TestTag> tags;

	public AbstractBenchmarkDescriptor(UniqueId uniqueId, String displayName, TestSource source) {
		super(uniqueId, displayName, source);

		Set<TestTag> tags = Collections.emptySet();
		if (source instanceof ClassSource) {
			tags = DescriptorUtils.getTags(((ClassSource) source).getJavaClass());
		}
		if (source instanceof MethodSource) {
			try {
				tags = DescriptorUtils.getTags(Class.forName(((MethodSource) source).getClassName()));
			} catch (ClassNotFoundException e) {
				throw new IllegalStateException(e);
			}
		}

		this.tags = tags;
	}

	/*
	 * (non-Javadoc)
	 * @see org.junit.platform.engine.TestDescriptor#getTags()
	 */
	@Override
	public Set<TestTag> getTags() {
		return tags;
	}
}
