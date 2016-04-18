package com.story_inspector.analysis;

import static com.story_inspector.analysis.AnalysisTestUtils.testFailedConstruction;

import java.util.ArrayList;
import java.util.Arrays;

import org.easymock.EasyMock;
import org.junit.Test;

import com.story_inspector.analysis.AnalysisTestUtils.TestAnalyzerType;
import com.story_inspector.analysis.summary.AnalyzerSummaryComponent;
import com.story_inspector.analysis.summary.ReportSummaryWriter;

import junit.framework.Assert;

public class AnalyzerResultTest {

	private Comment comment;
	private AnalyzerSummaryComponent summaryComponent;

	@SuppressWarnings("unchecked")
	private Analyzer<TestAnalyzerType> initializeMockAnalyzer(final boolean producesComments, final boolean commentRecordingSuppressed) {
		final Analyzer<TestAnalyzerType> mockAnalyzer = EasyMock.mock(Analyzer.class);
		final TestAnalyzerType testAnalyzerType = EasyMock.mock(TestAnalyzerType.class);
		this.comment = EasyMock.mock(Comment.class);
		this.summaryComponent = EasyMock.mock(AnalyzerSummaryComponent.class);

		EasyMock.expect(mockAnalyzer.getAnalyzerType()).andReturn(testAnalyzerType).anyTimes();
		EasyMock.expect(mockAnalyzer.getName()).andReturn("Name").anyTimes();
		EasyMock.expect(mockAnalyzer.getDescription()).andReturn("Description").anyTimes();
		EasyMock.expect(mockAnalyzer.isCommentRecordingSuppressed()).andReturn(commentRecordingSuppressed).anyTimes();
		EasyMock.expect(testAnalyzerType.producesComments()).andReturn(producesComments).anyTimes();
		EasyMock.replay(testAnalyzerType, mockAnalyzer);
		return mockAnalyzer;
	}

	@Test
	public void testHappy() {
		final Analyzer<TestAnalyzerType> mockAnalyzer = initializeMockAnalyzer(true, false);
		final AnalyzerResult<TestAnalyzerType> result = new AnalyzerResult<TestAnalyzerType>(mockAnalyzer, Arrays.asList(this.comment),
				Arrays.asList(this.summaryComponent));
		Assert.assertEquals(mockAnalyzer, result.getAnalyzer());
		Assert.assertEquals(Arrays.asList(this.comment), new ArrayList<>(result.getComments()));
		Assert.assertEquals(Arrays.asList(this.summaryComponent), new ArrayList<>(result.getSummaryComponents()));

		// Make sure it writes the summary component at least once, don't test the rest of the writing too closely
		final ReportSummaryWriter mockWriter = EasyMock.niceMock(ReportSummaryWriter.class);
		this.summaryComponent.write(mockWriter);
		EasyMock.expectLastCall().atLeastOnce();
		EasyMock.replay(this.summaryComponent);

		result.writeSummary(mockWriter);
	}

	@Test
	public void testCommentSuppression() {
		final Analyzer<TestAnalyzerType> mockAnalyzer = initializeMockAnalyzer(true, true);
		final AnalyzerResult<TestAnalyzerType> result = new AnalyzerResult<TestAnalyzerType>(mockAnalyzer, Arrays.asList(this.comment),
				Arrays.asList(this.summaryComponent));
		Assert.assertTrue(result.getComments().isEmpty());
	}

	@Test
	public void testFailedConstructions() {
		final Analyzer<TestAnalyzerType> mockAnalyzer = initializeMockAnalyzer(true, false);
		testFailedConstruction(() -> new AnalyzerResult<TestAnalyzerType>(null, Arrays.asList(this.comment), Arrays.asList(this.summaryComponent)));
		testFailedConstruction(() -> new AnalyzerResult<TestAnalyzerType>(mockAnalyzer, null, Arrays.asList(this.summaryComponent)));
		testFailedConstruction(() -> new AnalyzerResult<TestAnalyzerType>(mockAnalyzer, Arrays.asList(this.comment), null));
		testFailedConstruction(() -> new AnalyzerResult<TestAnalyzerType>(mockAnalyzer, Arrays.asList(null), Arrays.asList(this.summaryComponent)));
		testFailedConstruction(() -> new AnalyzerResult<TestAnalyzerType>(mockAnalyzer, Arrays.asList(this.comment), Arrays.asList(null)));

		final Analyzer<TestAnalyzerType> mockAnalyzerNoCommentsAllowed = initializeMockAnalyzer(false, false);
		testFailedConstruction(() -> new AnalyzerResult<TestAnalyzerType>(mockAnalyzerNoCommentsAllowed, Arrays.asList(this.comment),
				Arrays.asList(this.summaryComponent)));
	}
}
