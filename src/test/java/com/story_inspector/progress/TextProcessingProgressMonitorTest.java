package com.story_inspector.progress;

import org.junit.Test;

import junit.framework.Assert;

public class TextProcessingProgressMonitorTest {

	private float currentPercent;
	private String currentMessage;
	private final ProgressMonitor testMonitor = (percent, message) -> {
		this.currentPercent = percent;
		this.currentMessage = message;
	};

	@Test
	public void testHappy() throws Exception {
		final TextProcessingProgressMonitor textMonitor = new TextProcessingProgressMonitor(this.testMonitor, 10);
		textMonitor.reportProgress(5);
		Assert.assertEquals(0.5f, this.currentPercent);
		Assert.assertEquals("5 / 10 characters", this.currentMessage);
	}

	@Test(expected = IllegalArgumentException.class)
	public void testNegativeProgress() throws Exception {
		final TextProcessingProgressMonitor textMonitor = new TextProcessingProgressMonitor(this.testMonitor, 10);
		textMonitor.reportProgress(-1);
	}

	@Test(expected = IllegalArgumentException.class)
	public void testProgressTooLarge() throws Exception {
		final TextProcessingProgressMonitor textMonitor = new TextProcessingProgressMonitor(this.testMonitor, 10);
		textMonitor.reportProgress(10);
	}

	@Test(expected = NullPointerException.class)
	public void testNullParent() throws Exception {
		new TextProcessingProgressMonitor(null, 10);
	}

	@Test(expected = IllegalArgumentException.class)
	public void testNegativeLength() throws Exception {
		new TextProcessingProgressMonitor(this.testMonitor, -1);
	}

	@Test(expected = TaskCanceledException.class)
	public void testExceptionPassing() throws Exception {
		final ProgressMonitor exceptionMonitor = (p, m) -> {
			throw new TaskCanceledException("Task canceled");
		};
		final TextProcessingProgressMonitor textMonitor = new TextProcessingProgressMonitor(exceptionMonitor, 10);
		textMonitor.reportProgress(5);
	}
}
