/*
 * Copyright 2018 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v20.html
 */
package jmh.mbr.junit5.discovery;

import jmh.mbr.junit5.discovery.predicates.IsBenchmarkClass;

import org.junit.platform.commons.support.scanning.ClassFilter;
import org.junit.platform.engine.ConfigurationParameters;
import org.junit.platform.engine.EngineDiscoveryRequest;
import org.junit.platform.engine.TestDescriptor;
import org.junit.platform.engine.discovery.ClassSelector;
import org.junit.platform.engine.discovery.ClasspathRootSelector;
import org.junit.platform.engine.discovery.MethodSelector;
import org.junit.platform.engine.discovery.ModuleSelector;
import org.junit.platform.engine.discovery.PackageSelector;
import org.junit.platform.engine.discovery.UniqueIdSelector;
import org.junit.platform.engine.support.filter.ClasspathScanningSupport;

/**
 * {@link JavaElementsResolver}-based discovery mechanism. Resolves {@link TestDescriptor descriptors} by introspecting
 * classes, methods, and {@link org.junit.platform.engine.UniqueId}.
 */
public class DiscoverySelectorResolver {

	public void resolveSelectors(EngineDiscoveryRequest request, TestDescriptor engineDescriptor) {

		ClassFilter classFilter = ClasspathScanningSupport.buildClassFilter(request, IsBenchmarkClass.INSTANCE);
		resolve(request, engineDescriptor, classFilter);
		filter(engineDescriptor, classFilter);
		pruneTree(engineDescriptor);
	}

	private void resolve(EngineDiscoveryRequest request, TestDescriptor engineDescriptor, ClassFilter classFilter) {

		JavaElementsResolver javaElementsResolver = createJavaElementsResolver(request.getConfigurationParameters(),
				engineDescriptor, classFilter);

		request.getSelectorsByType(ClasspathRootSelector.class).forEach(javaElementsResolver::resolveClasspathRoot);
		request.getSelectorsByType(ModuleSelector.class).forEach(javaElementsResolver::resolveModule);
		request.getSelectorsByType(PackageSelector.class).forEach(javaElementsResolver::resolvePackage);
		request.getSelectorsByType(ClassSelector.class).forEach(javaElementsResolver::resolveClass);
		request.getSelectorsByType(MethodSelector.class).forEach(javaElementsResolver::resolveMethod);
		request.getSelectorsByType(UniqueIdSelector.class).forEach(javaElementsResolver::resolveUniqueId);
	}

	private void filter(TestDescriptor engineDescriptor, ClassFilter classFilter) {
		new DiscoveryFilterApplier().applyClassNamePredicate(classFilter::match, engineDescriptor);
	}

	private void pruneTree(TestDescriptor rootDescriptor) {
		rootDescriptor.accept(TestDescriptor::prune);
	}

	private JavaElementsResolver createJavaElementsResolver(ConfigurationParameters configurationParameters,
			TestDescriptor engineDescriptor, ClassFilter classFilter) {
		return new JavaElementsResolver(engineDescriptor, classFilter, ElementResolvers.getResolvers());
	}

}
