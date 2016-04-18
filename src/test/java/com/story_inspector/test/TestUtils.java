package com.story_inspector.test;

import org.apache.commons.lang3.builder.EqualsBuilder;

import junit.framework.Assert;

public class TestUtils {

	private TestUtils() {
		throw new UnsupportedOperationException();
	}

	public static void assertReflectionEquals(final Object expected, final Object actual) {
		Assert.assertTrue("Expected: " + expected + ", but was " + actual, EqualsBuilder.reflectionEquals(expected, actual));
	}

	public static void assertReflectionEquals(final Object expected, final Object actual, final String... excludedFields) {
		Assert.assertTrue("Expected: " + expected + ", but was " + actual, EqualsBuilder.reflectionEquals(expected, actual, excludedFields));
	}
}
