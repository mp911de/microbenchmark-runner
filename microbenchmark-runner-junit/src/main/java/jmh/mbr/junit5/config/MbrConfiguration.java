/*
 * Copyright 2025 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v20.html
 */
package jmh.mbr.junit5.config;

import org.junit.jupiter.engine.config.JupiterConfiguration;
import org.junit.platform.engine.support.store.Namespace;
import org.junit.platform.engine.support.store.NamespacedHierarchicalStore;

public interface MbrConfiguration extends JupiterConfiguration {

	NamespacedHierarchicalStore<Namespace> getStore();
}
