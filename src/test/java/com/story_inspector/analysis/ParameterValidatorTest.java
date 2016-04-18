package com.story_inspector.analysis;

import org.junit.Test;

import junit.framework.Assert;

public class ParameterValidatorTest {

	@Test
	public void testAlwaysValid() {
		final ParameterValidator<String> alwaysValid = ParameterValidator.alwaysValid();
		Assert.assertTrue(alwaysValid.validateParameter("apple").isValid());
		Assert.assertTrue(alwaysValid.validateParameter("banana").isValid());
		Assert.assertTrue(alwaysValid.validateParameter("").isValid());
		Assert.assertTrue(alwaysValid.validateParameter(null).isValid());
	}

	@Test
	public void testNotNull() {
		final ParameterValidator<String> notNull = ParameterValidator.notNull("stringParameter");
		Assert.assertTrue(notNull.validateParameter("apple").isValid());
		Assert.assertFalse(notNull.validateParameter(null).isValid());
	}

	@Test
	public void testCreateValidator() {
		final ParameterValidator<String> stringLength2 = ParameterValidator.createValidator(s -> s.length() == 2, "String must have length 2");
		Assert.assertTrue(stringLength2.validateParameter("ap").isValid());
		Assert.assertFalse(stringLength2.validateParameter("app").isValid());
	}

	@Test
	public void testConcatenateValidators() {
		final ParameterValidator<String> stringLength2 = ParameterValidator.createValidator(s -> s.length() == 2, "String must have length 2");
		final ParameterValidator<String> stringContainsA = ParameterValidator.createValidator(s -> s.toLowerCase().contains("a"),
				"String must contain 'a'");
		final ParameterValidator<String> concatenatedValidator = ParameterValidator.concatenateValidators(stringLength2, stringContainsA);
		Assert.assertTrue(concatenatedValidator.validateParameter("ap").isValid());
		Assert.assertFalse(concatenatedValidator.validateParameter("op").isValid());
		Assert.assertFalse(concatenatedValidator.validateParameter("a").isValid());
		Assert.assertFalse(concatenatedValidator.validateParameter("").isValid());
	}
}
