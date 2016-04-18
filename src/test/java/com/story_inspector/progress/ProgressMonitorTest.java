package com.story_inspector.progress;

import org.junit.Test;

import junit.framework.Assert;

public class ProgressMonitorTest {

	private float currentPercent;
	private String currentMessage;
	private final ProgressMonitor testMonitor = (percent, message) -> {
		this.currentPercent = percent;
		this.currentMessage = message;
	};

	@Test
	public void testSubMonitor() throws Exception {
		final ProgressMonitor subMonitor = this.testMonitor.subMonitor(0.5f, 0.6f, "Title");
		subMonitor.reportProgress(0.5f, "Halfway done");
		Assert.assertEquals(this.currentPercent, 0.55f);
		Assert.assertEquals(this.currentMessage, "Title: Halfway done");
	}

	@Test(expected = IllegalArgumentException.class)
	public void testSubmonitorStartUnder() {
		this.testMonitor.subMonitor(-1f, 0.6f, "Title");
	}

	@Test(expected = IllegalArgumentException.class)
	public void testSubmonitorStartOver() {
		this.testMonitor.subMonitor(1.1f, 1.2f, "Title");
	}

	@Test(expected = IllegalArgumentException.class)
	public void testSubmonitorEndUnder() {
		this.testMonitor.subMonitor(-0.5f, -0.2f, "Title");
	}

	@Test(expected = IllegalArgumentException.class)
	public void testSubmonitorEndOver() {
		this.testMonitor.subMonitor(0.5f, 1.2f, "Title");
	}

	@Test(expected = IllegalArgumentException.class)
	public void testSubmonitorStartGreaterThanEnd() {
		this.testMonitor.subMonitor(0.9f, 0.2f, "Title");
	}

	@Test(expected = IllegalArgumentException.class)
	public void testBlankTitle() {
		this.testMonitor.subMonitor(0.1f, 0.2f, "");
	}

	@Test(expected = TaskCanceledException.class)
	public void testExceptionPassing() throws Exception {
		final ProgressMonitor exceptionMonitor = (p, m) -> {
			throw new TaskCanceledException("Task canceled");
		};
		final ProgressMonitor subMonitor = exceptionMonitor.subMonitor(0.1f, 0.2f, "Title");
		subMonitor.reportProgress(0.25f, "Quarter done");
	}
}
