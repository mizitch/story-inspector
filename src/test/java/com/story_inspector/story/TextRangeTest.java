package com.story_inspector.story;

import java.util.Arrays;
import java.util.HashSet;

import org.junit.Test;

import junit.framework.Assert;

public class TextRangeTest {

	@Test
	public void testHappy() {
		final TextRange range = new TextRange(0, 10);
		final TextRange rangeA = new TextRange(0, 5);
		final TextRange rangeB = new TextRange(5, 10);

		Assert.assertEquals(0, range.getStartIndex());
		Assert.assertEquals(10, range.getEndIndex());
		Assert.assertEquals(10, range.getLength());
		Assert.assertEquals(5, rangeB.getLength());
		Assert.assertEquals(" word", rangeB.getCoveredText("Wordy words"));

		Assert.assertTrue(range.contains(rangeA));
		Assert.assertTrue(range.contains(rangeB));
		Assert.assertFalse(rangeA.contains(range));
		Assert.assertFalse(rangeB.contains(range));
		Assert.assertFalse(rangeA.contains(rangeB));
		Assert.assertFalse(rangeB.contains(rangeA));

		Assert.assertTrue(range.intersects(rangeA));
		Assert.assertTrue(range.intersects(rangeB));
		Assert.assertTrue(rangeA.intersects(range));
		Assert.assertTrue(rangeB.intersects(range));
		Assert.assertFalse(rangeA.intersects(rangeB));
		Assert.assertFalse(rangeB.intersects(rangeA));

		Assert.assertFalse(range.contains(-1));
		Assert.assertTrue(range.contains(0));
		Assert.assertTrue(range.contains(5));
		Assert.assertFalse(range.contains(10));

		Assert.assertFalse(rangeB.contains(4));
		Assert.assertTrue(rangeB.contains(5));
		Assert.assertTrue(rangeB.contains(7));
		Assert.assertFalse(rangeB.contains(10));

		Assert.assertTrue(TextRange.intersectsRangeSet(rangeA, new HashSet<>(Arrays.asList(range, rangeB))));
		Assert.assertFalse(TextRange.intersectsRangeSet(rangeA, new HashSet<>(Arrays.asList(rangeB))));

		Assert.assertEquals(rangeB, rangeA.translate(5));
		Assert.assertEquals(rangeA, rangeB.translate(-5));
	}

	@Test(expected = IllegalArgumentException.class)
	public void testNegativeStart() {
		new TextRange(-1, 10);
	}

	@Test(expected = IllegalArgumentException.class)
	public void testEndLessThanStart() {
		new TextRange(5, 4);
	}

	@Test(expected = IllegalArgumentException.class)
	public void testBothNegative() {
		new TextRange(-5, -1);
	}

}
