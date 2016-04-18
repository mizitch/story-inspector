package com.story_inspector.analysis;

import java.util.Arrays;

import org.junit.Assert;
import org.junit.Test;

public class ValidationResultTest {

	@Test
	public void testValid() {
		final ValidationResult validResult = ValidationResult.validResult();
		Assert.assertTrue(validResult.isValid());
		Assert.assertTrue(validResult.getValidationErrors().isEmpty());
	}

	@Test
	public void testInvalidArrayConstructor() {
		final ValidationResult invalidResult = ValidationResult.invalidResult("Failure 1", "Failure 2");
		Assert.assertFalse(invalidResult.isValid());
		Assert.assertEquals(Arrays.asList("Failure 1", "Failure 2"), invalidResult.getValidationErrors());
	}

	@Test
	public void testInvalidListConstructor() {
		final ValidationResult invalidResult = ValidationResult.invalidResult(Arrays.asList("Failure 1", "Failure 2"));
		Assert.assertFalse(invalidResult.isValid());
		Assert.assertEquals(Arrays.asList("Failure 1", "Failure 2"), invalidResult.getValidationErrors());
	}

	@Test
	public void testFailedConstruction() {
		AnalysisTestUtils.testFailedConstruction(() -> ValidationResult.invalidResult("Failure!", null));
	}
}
