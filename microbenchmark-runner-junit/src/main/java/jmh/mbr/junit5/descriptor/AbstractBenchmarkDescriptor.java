/*
 * Copyright 2018-2019 the original author or authors.
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

import jmh.mbr.junit5.config.MbrConfiguration;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.engine.extension.ExtensionRegistry;
import org.junit.jupiter.engine.extension.MutableExtensionRegistry;
import org.junit.platform.engine.EngineExecutionListener;
import org.junit.platform.engine.TestSource;
import org.junit.platform.engine.TestTag;
import org.junit.platform.engine.UniqueId;
import org.junit.platform.engine.support.descriptor.AbstractTestDescriptor;
import org.junit.platform.engine.support.descriptor.ClassSource;
import org.junit.platform.engine.support.descriptor.MethodSource;

/**
 * Abstract base class for Benchmark descriptors. Exposes {@link TestTag tags} and allows contextual {@link org.junit.jupiter.api.extension.Extension} retrieval.
 */
public abstract class AbstractBenchmarkDescriptor extends AbstractTestDescriptor {

	private final Set<TestTag> tags;

	/**
	 * Creates a new {@link AbstractBenchmarkDescriptor} given {@link UniqueId}, {@code displayName} and a {@link TestSource}.
	 *
	 * @param uniqueId the {@link UniqueId} for this descriptor.
	 * @param displayName
	 * @param source source this descriptor.
	 */
	public AbstractBenchmarkDescriptor(UniqueId uniqueId, String displayName, TestSource source) {
		super(uniqueId, displayName, source);

		Set<TestTag> tags = Collections.emptySet();
		if (source instanceof ClassSource) {
			tags = DescriptorUtils.getTags(((ClassSource) source).getJavaClass());
		}
		if (source instanceof MethodSource) {
			try {
				tags = DescriptorUtils
						.getTags(Class.forName(((MethodSource) source).getClassName()));
			}
			catch (ClassNotFoundException e) {
				throw new IllegalStateException(e);
			}
		}

		this.tags = tags;
	}

	@Override
	public Set<TestTag> getTags() {
		return tags;
	}

	/**
	 * Creates a {@link ExtensionContext} for this descriptor containing scoped extensions.
	 *
	 * @param parent optional parent {@link ExtensionContext}. {@literal null} to use no parent so the resulting context serves as root {@link ExtensionContext}.
	 * @param engineExecutionListener the listener.
	 * @param configuration configuration parameters.
	 * @return the {@link ExtensionContext} for this descriptor.
	 */
	public abstract ExtensionContext getExtensionContext(ExtensionContext parent, EngineExecutionListener engineExecutionListener, MbrConfiguration configuration);

	/**
	 * Creates an {@link MutableExtensionRegistry} that contains extensions derived from the benchmark source (annotations on class/method level).
	 *
	 * @param parent the parent {@link MutableExtensionRegistry}.
	 * @return the {@link ExtensionRegistry} derived from this descriptor.
	 */
	public abstract ExtensionRegistry getExtensionRegistry(MutableExtensionRegistry parent);
}
