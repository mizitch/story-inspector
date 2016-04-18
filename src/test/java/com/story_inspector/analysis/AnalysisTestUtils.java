package com.story_inspector.analysis;

import java.util.function.Predicate;
import java.util.function.Supplier;

import junit.framework.Assert;

public class AnalysisTestUtils {
	private AnalysisTestUtils() {
		throw new UnsupportedOperationException("Collection of static methods, can't be instantiated");
	}

	public static <T> ParameterSpec<T> createParameterSpec(final String id, final Class<T> type) {
		return new ParameterSpec<T>(id, "Test parameter", "Test parameter description", type, ParameterValidator.alwaysValid());
	}

	public static <T> ParameterSpec<T> createParameterSpec(final String id, final Class<T> type, final Predicate<T> validationPredicate) {
		return new ParameterSpec<T>(id, "Test parameter", "Test parameter description", type,
				ParameterValidator.createValidator(validationPredicate, "Validation failed"));
	}

	public static abstract class TestAnalyzerType implements AnalyzerType<TestAnalyzerType> {

	}

	public static void testFailedConstruction(final Supplier<?> creator) {
		try {
			creator.get();
		} catch (IllegalArgumentException | NullPointerException e) {
			return;
		}
		Assert.fail("Creates analyzer creation result successfully, shouldn't have been possible");
	}
}
