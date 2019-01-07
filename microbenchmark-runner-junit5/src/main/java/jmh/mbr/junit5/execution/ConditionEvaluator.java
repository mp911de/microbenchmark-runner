/*
 * Copyright 2015-2019 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v20.html
 */
package jmh.mbr.junit5.execution;

import static java.lang.String.*;

import org.junit.jupiter.api.extension.ConditionEvaluationResult;
import org.junit.jupiter.api.extension.ExecutionCondition;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.engine.extension.ExtensionRegistry;
import org.junit.platform.commons.logging.Logger;
import org.junit.platform.commons.logging.LoggerFactory;
import org.junit.platform.commons.util.StringUtils;

/**
 * Evaluates conditions for a benchmark class/benchmark method.
 */
class ConditionEvaluator {

	private static final Logger logger = LoggerFactory.getLogger(ConditionEvaluator.class);

	private static final ConditionEvaluationResult ENABLED = ConditionEvaluationResult.enabled(
			"No 'disabled' conditions encountered");

	/**
	 * Evaluate all {@link ExecutionCondition} extensions registered for the
	 * supplied {@link ExtensionContext}.
	 *
	 * @param extensionRegistry the current {@link ExtensionRegistry}
	 * @param context the current {@link ExtensionContext}
	 * @return the first disabled {@link ConditionEvaluationResult}, or a default enabled {@link ConditionEvaluationResult} if no disabled conditions are encountered.
	 */
	ConditionEvaluationResult evaluate(ExtensionRegistry extensionRegistry, ExtensionContext context) {

		return extensionRegistry.stream(ExecutionCondition.class)
				.map(condition -> evaluate(condition, context))
				.filter(ConditionEvaluationResult::isDisabled)
				.findFirst()
				.orElse(ENABLED);
	}

	/**
	 * Evaluate a single {@link ExecutionCondition} extension.
	 *
	 * @param condition the current {@link ExecutionCondition}
	 * @param context the current {@link ExtensionContext}
	 * @return
	 */
	private ConditionEvaluationResult evaluate(ExecutionCondition condition, ExtensionContext context) {
		try {
			ConditionEvaluationResult result = condition.evaluateExecutionCondition(context);
			logResult(condition.getClass(), result);
			return result;
		} catch (Exception ex) {
			throw evaluationException(condition.getClass(), ex);
		}
	}

	private void logResult(Class<?> conditionType, ConditionEvaluationResult result) {
		logger.debug(() -> format("Evaluation of condition [%s] resulted in: %s", conditionType.getName(), result));
	}

	private ConditionEvaluationException evaluationException(Class<?> conditionType, Exception ex) {
		String cause = StringUtils.isNotBlank(ex.getMessage()) ? ": " + ex.getMessage() : "";
		return new ConditionEvaluationException(
				String.format("Failed to evaluate condition [%s]%s", conditionType.getName(), cause), ex);
	}

	/**
	 * Exception thrown on failures while evaluating an {@link ExecutionCondition}.
	 */
	static class ConditionEvaluationException extends RuntimeException {

		ConditionEvaluationException(String message, Throwable cause) {
			super(message, cause);
		}
	}
}
