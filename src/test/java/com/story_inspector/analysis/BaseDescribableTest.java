package com.story_inspector.analysis;

import org.junit.Test;

import junit.framework.Assert;

public class BaseDescribableTest {

	private static final String DESCRIPTION = "Description";
	private static final String NAME = "Name";

	private class TestDescribable extends BaseDescribable {

		public TestDescribable(final String name, final String description) {
			super(name, description);
		}
	}

	@Test
	public void testHappy() {
		final TestDescribable describable = new TestDescribable(NAME, DESCRIPTION);
		Assert.assertEquals(NAME, describable.getName());
		Assert.assertEquals(DESCRIPTION, describable.getDescription());
	}

	@Test
	public void testFailedConstructions() {
		AnalysisTestUtils.testFailedConstruction(() -> new TestDescribable(null, DESCRIPTION));
		AnalysisTestUtils.testFailedConstruction(() -> new TestDescribable(NAME, null));
	}

	@Test
	public void testEquals() {
		final TestDescribable describable = new TestDescribable(NAME, DESCRIPTION);
		final TestDescribable equalDescribable = new TestDescribable(NAME, DESCRIPTION);
		final TestDescribable notEqualDescribable = new TestDescribable("Other Name", DESCRIPTION);

		Assert.assertEquals(describable, describable);
		Assert.assertEquals(describable, equalDescribable);
		Assert.assertEquals(equalDescribable, describable);
		Assert.assertFalse(describable.equals(notEqualDescribable));
		Assert.assertFalse(notEqualDescribable.equals(describable));
		Assert.assertFalse(describable.equals(null));
		Assert.assertFalse(describable.equals("random string"));

	}
}
