package com.story_inspector.analysis;

import org.junit.Test;

import junit.framework.Assert;

public class ParameterSpecTest {

	private static final ParameterValidator<String> PARAMETER_VALIDATOR = ParameterValidator.alwaysValid();
	private static final String DEFAULT_VALUE = "";
	private static final String DESCRIPTION = "A string";
	private static final String NAME = "String Param";
	private static final String ID = "stringParam";

	@Test
	public void testHappy() {
		final ParameterSpec<String> spec = new ParameterSpec<String>(ID, NAME, DESCRIPTION, String.class, PARAMETER_VALIDATOR, DEFAULT_VALUE);

		Assert.assertEquals(ID, spec.getId());
		Assert.assertEquals(NAME, spec.getName());
		Assert.assertEquals(DESCRIPTION, spec.getDescription());
		Assert.assertEquals(String.class, spec.getParameterType());
		Assert.assertTrue(spec.hasDefaultValue());
		Assert.assertEquals(DEFAULT_VALUE, spec.getDefaultValue());
		Assert.assertEquals(PARAMETER_VALIDATOR, spec.getParameterValidator());
	}

	@Test
	public void testShorterConstructor() {
		final ParameterSpec<String> spec = new ParameterSpec<String>(ID, NAME, DESCRIPTION, String.class, PARAMETER_VALIDATOR);
		Assert.assertFalse(spec.hasDefaultValue());
		Assert.assertNull(spec.getDefaultValue());
	}

	@Test
	public void testShortestConstructor() {
		final ParameterSpec<String> spec = new ParameterSpec<String>(ID, NAME, DESCRIPTION, String.class);
		Assert.assertFalse(spec.hasDefaultValue());
		Assert.assertNull(spec.getDefaultValue());
		Assert.assertTrue(spec.getParameterValidator().validateParameter("").isValid());
	}

	@Test
	public void testFailedConstructions() {
		AnalysisTestUtils.testFailedConstruction(() -> new ParameterSpec<String>(null, NAME, DESCRIPTION, String.class));
		AnalysisTestUtils.testFailedConstruction(() -> new ParameterSpec<String>(ID, NAME, DESCRIPTION, null));
		AnalysisTestUtils.testFailedConstruction(() -> new ParameterSpec<String>("", NAME, DESCRIPTION, null));
		AnalysisTestUtils.testFailedConstruction(() -> new ParameterSpec<String>(ID, NAME, DESCRIPTION, String.class, null));
	}
}
