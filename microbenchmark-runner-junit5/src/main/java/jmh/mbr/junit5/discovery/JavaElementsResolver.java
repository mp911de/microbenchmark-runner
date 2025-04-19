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

import jmh.mbr.junit5.MicrobenchmarkEngine;
import jmh.mbr.junit5.descriptor.BenchmarkClassDescriptor;
import jmh.mbr.junit5.descriptor.ParametrizedBenchmarkMethodDescriptor;
import jmh.mbr.junit5.discovery.predicates.IsBenchmarkMethod;

import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.Collections;
import java.util.Deque;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.stream.Collectors;

import org.junit.platform.commons.logging.Logger;
import org.junit.platform.commons.logging.LoggerFactory;
import org.junit.platform.commons.support.scanning.ClassFilter;
import org.junit.platform.commons.util.BlacklistedExceptions;
import org.junit.platform.commons.util.ReflectionUtils;
import org.junit.platform.commons.util.StringUtils;
import org.junit.platform.engine.TestDescriptor;
import org.junit.platform.engine.UniqueId;
import org.junit.platform.engine.UniqueId.Segment;
import org.junit.platform.engine.discovery.ClassSelector;
import org.junit.platform.engine.discovery.ClasspathRootSelector;
import org.junit.platform.engine.discovery.MethodSelector;
import org.junit.platform.engine.discovery.ModuleSelector;
import org.junit.platform.engine.discovery.PackageSelector;
import org.junit.platform.engine.discovery.UniqueIdSelector;

/**
 * Resolve {@link TestDescriptor} by traversing classes and methods and attaching these to the root
 * {@link TestDescriptor test engine descriptor}.
 */
class JavaElementsResolver {

	private static final Logger logger = LoggerFactory.getLogger(JavaElementsResolver.class);

	private final TestDescriptor engineDescriptor;
	private final ClassFilter classFilter;
	private final Set<ElementResolver> resolvers;

	JavaElementsResolver(TestDescriptor engineDescriptor, ClassFilter classFilter, Set<ElementResolver> resolvers) {
		this.engineDescriptor = engineDescriptor;
		this.classFilter = classFilter;
		this.resolvers = resolvers;
	}

	void resolveClasspathRoot(ClasspathRootSelector selector) {
		try {
			ReflectionUtils.findAllClassesInClasspathRoot(selector.getClasspathRoot(), this.classFilter)
					.forEach(this::resolveClass);
		} catch (Throwable t) {
			BlacklistedExceptions.rethrowIfBlacklisted(t);
			logger.debug(t,
					() -> String.format("Failed to resolve classes in classpath root '%s'.", selector.getClasspathRoot()));
		}
	}

	void resolveModule(ModuleSelector selector) {
		try {
			ReflectionUtils.findAllClassesInModule(selector.getModuleName(), this.classFilter).forEach(this::resolveClass);
		} catch (Throwable t) {
			BlacklistedExceptions.rethrowIfBlacklisted(t);
			logger.debug(t, () -> String.format("Failed to resolve classes in module '%s'.", selector.getModuleName()));
		}
	}

	void resolvePackage(PackageSelector selector) {
		try {
			ReflectionUtils.findAllClassesInPackage(selector.getPackageName(), this.classFilter).forEach(this::resolveClass);
		} catch (Throwable t) {
			BlacklistedExceptions.rethrowIfBlacklisted(t);
			logger.debug(t, () -> String.format("Failed to resolve classes in package '%s'.", selector.getPackageName()));
		}
	}

	void resolveClass(ClassSelector selector) {
		// Even though resolveClass(Class<?>) has its own similar try-catch block, the
		// try-catch block is necessary here as well since ClassSelector#getJavaClass()
		// may throw an exception.
		try {
			resolveClass(selector.getJavaClass());
		} catch (Throwable t) {
			BlacklistedExceptions.rethrowIfBlacklisted(t);
			logger.debug(t, () -> String.format("Class '%s' could not be resolved.", selector.getClassName()));
		}
	}

	private void resolveClass(Class<?> testClass) {
		try {
			Set<TestDescriptor> resolvedDescriptors = resolveContainerWithParents(testClass);
			resolvedDescriptors.forEach(this::resolveChildren);

			if (resolvedDescriptors.isEmpty()) {
				logger.debug(() -> String.format("Class '%s' could not be resolved.", StringUtils.nullSafeToString(testClass)));
			}
		} catch (Throwable t) {
			BlacklistedExceptions.rethrowIfBlacklisted(t);
			logger.debug(t,
					() -> String.format("Class '%s' could not be resolved.", StringUtils.nullSafeToString(testClass)));
		}
	}

	void resolveMethod(MethodSelector selector) {

		try {
			Class<?> testClass = selector.getJavaClass();
			Method testMethod = selector.getJavaMethod();

			Set<TestDescriptor> potentialParents = resolveContainerWithParents(testClass);
			Set<TestDescriptor> resolvedDescriptors = resolveForAllParents(testMethod, potentialParents);
			resolvedDescriptors.forEach(this::resolveChildren);

			if (resolvedDescriptors.isEmpty()) {
				logger.debug(() -> String.format("Method '%s' could not be resolved.", testMethod.toGenericString()));
			}
		} catch (Throwable t) {
			BlacklistedExceptions.rethrowIfBlacklisted(t);
			logger.debug(t, () -> String.format("Method '%s' in class '%s' could not be resolved.", selector.getMethodName(),
					selector.getClassName()));
		}
	}

	void resolveUniqueId(UniqueIdSelector selector) {

		UniqueId uniqueId = selector.getUniqueId();

		// Ignore Unique IDs from other test engines.
		if (MicrobenchmarkEngine.ENGINE_ID.equals(uniqueId.getEngineId().orElse(null))) {
			try {
				Deque<TestDescriptor> resolvedDescriptors = resolveAllSegments(uniqueId);
				handleResolvedDescriptorsForUniqueId(uniqueId, resolvedDescriptors);
			} catch (Throwable t) {
				BlacklistedExceptions.rethrowIfBlacklisted(t);
				logger.warn(t, () -> String.format("Unique ID '%s' could not be resolved.", selector.getUniqueId()));
			}
		}
	}

	private Set<TestDescriptor> resolveContainerWithParents(Class<?> testClass) {
		return resolveForAllParents(testClass, Collections.singleton(this.engineDescriptor));
	}

	/**
	 * Attempt to resolve all segments for the supplied unique ID.
	 */
	private Deque<TestDescriptor> resolveAllSegments(UniqueId uniqueId) {

		List<Segment> segments = uniqueId.getSegments();
		Deque<TestDescriptor> resolvedDescriptors = new LinkedList<>();
		resolvedDescriptors.addFirst(this.engineDescriptor);

		for (int index = 1; index < segments.size() && resolvedDescriptors.size() == index; index++) {
			Segment segment = segments.get(index);
			TestDescriptor parent = resolvedDescriptors.getLast();
			UniqueId partialUniqueId = parent.getUniqueId().append(segment);

			Optional<TestDescriptor> resolvedDescriptor = findTestDescriptorByUniqueId(partialUniqueId);
			if (!resolvedDescriptor.isPresent()) {
				resolvedDescriptor = this.resolvers.stream().map(resolver -> resolver.resolveUniqueId(segment, parent))
						.filter(Optional::isPresent).map(Optional::get).findFirst();
				resolvedDescriptor.ifPresent(parent::addChild);
			}
			resolvedDescriptor.ifPresent(resolvedDescriptors::addLast);
		}
		return resolvedDescriptors;
	}

	private void handleResolvedDescriptorsForUniqueId(UniqueId uniqueId, Deque<TestDescriptor> resolvedDescriptors) {
		List<Segment> segments = uniqueId.getSegments();
		int numSegmentsToResolve = segments.size() - 1;
		int numSegmentsResolved = resolvedDescriptors.size() - 1;

		if (numSegmentsResolved == 0) {
			logger.warn(() -> String.format("Unique ID '%s' could not be resolved.", uniqueId));
		} else if (numSegmentsResolved != numSegmentsToResolve) {
			logger.warn(() -> {
				List<Segment> unresolved = segments.subList(1, segments.size()); // Remove engine ID
				unresolved = unresolved.subList(numSegmentsResolved, unresolved.size()); // Remove resolved segments
				return String.format("Unique ID '%s' could only be partially resolved. "
								+ "All resolved segments will be executed; however, the " + "following segments could not be resolved: %s",
						uniqueId, unresolved);
			});
		} else {
			resolveChildren(resolvedDescriptors.getLast());
		}
	}

	private Set<TestDescriptor> resolveForAllParents(AnnotatedElement element, Set<TestDescriptor> potentialParents) {
		return potentialParents.stream().flatMap(it -> resolve(element, it).stream()).collect(Collectors.toSet());
	}

	private void resolveChildren(TestDescriptor descriptor) {

		if (descriptor instanceof BenchmarkClassDescriptor) {

			Class<?> testClass = ((BenchmarkClassDescriptor) descriptor).getJavaClass();
			resolveContainedMethods(descriptor, testClass, this::resolve);
		}

		if (descriptor instanceof ParametrizedBenchmarkMethodDescriptor) {

			Method benchmarkMethod = ((ParametrizedBenchmarkMethodDescriptor) descriptor).getMethod();
			resolve(benchmarkMethod, descriptor);
		}
	}

	private void resolveContainedMethods(TestDescriptor containerDescriptor, Class<?> testClass,
										 BiConsumer<AnnotatedElement, TestDescriptor> fixtureResolver) {

		List<Method> benchmarkMethodCandidates = ReflectionUtils.findMethods(testClass, IsBenchmarkMethod.INSTANCE);
		benchmarkMethodCandidates.forEach(it -> {
			Set<TestDescriptor> methodDescriptors = resolve(it, containerDescriptor);
			methodDescriptors.forEach(methodDescriptor -> fixtureResolver.accept(it, methodDescriptor));
		});
	}

	private Set<TestDescriptor> resolve(AnnotatedElement element, TestDescriptor parent) {

		return this.resolvers.stream() //
				.map(resolver -> tryToResolveWithResolver(element, parent, resolver)) //
				.filter(testDescriptors -> !testDescriptors.isEmpty()) //
				.flatMap(Collection::stream) //
				.collect(Collectors.toSet());
	}

	private Set<TestDescriptor> tryToResolveWithResolver(AnnotatedElement element, TestDescriptor parent,
														 ElementResolver resolver) {

		Set<TestDescriptor> resolvedDescriptors = resolver.resolveElement(element, parent);
		Set<TestDescriptor> result = new LinkedHashSet<>();

		resolvedDescriptors.forEach(testDescriptor -> {
			Optional<TestDescriptor> existingTestDescriptor = findTestDescriptorByUniqueId(testDescriptor.getUniqueId());
			if (existingTestDescriptor.isPresent()) {
				result.add(existingTestDescriptor.get());
			} else {
				parent.addChild(testDescriptor);
				result.add(testDescriptor);
			}
		});

		return result;
	}

	@SuppressWarnings("unchecked")
	private Optional<TestDescriptor> findTestDescriptorByUniqueId(UniqueId uniqueId) {
		return (Optional<TestDescriptor>) this.engineDescriptor.findByUniqueId(uniqueId);
	}
}
