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

	@Override
	public Set<TestTag> getTags() {
		return tags;
	}
}
