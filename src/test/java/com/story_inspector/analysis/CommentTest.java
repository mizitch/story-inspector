package com.story_inspector.analysis;

import org.easymock.EasyMock;
import org.junit.Before;
import org.junit.Test;

import com.story_inspector.analysis.AnalysisTestUtils.TestAnalyzerType;
import com.story_inspector.story.TextRange;

import junit.framework.Assert;

public class CommentTest {
	private static final String CONTENT = "This text is super bad!";
	private static final TextRange TEXT_RANGE = new TextRange(5, 9);

	private Analyzer<TestAnalyzerType> mockAnalyzer;

	@SuppressWarnings("unchecked")
	@Before
	public void initialize() {
		this.mockAnalyzer = EasyMock.mock(Analyzer.class);
		final AnalyzerSpec<TestAnalyzerType> mockSpec = EasyMock.mock(AnalyzerSpec.class);
		EasyMock.expect(this.mockAnalyzer.extractAnalyzerSpec()).andReturn(mockSpec).anyTimes();
		EasyMock.replay(this.mockAnalyzer, mockSpec);
	}

	@Test
	public void testHappy() {
		final Comment comment = new Comment(this.mockAnalyzer, CONTENT, TEXT_RANGE);
		Assert.assertEquals(CONTENT, comment.getContent());
		Assert.assertEquals(TEXT_RANGE, comment.getSelection());
		Assert.assertEquals(this.mockAnalyzer, comment.getAnalyzer());
	}

	@Test
	public void testFailedConstructions() {

		AnalysisTestUtils.testFailedConstruction(() -> new Comment(null, CONTENT, TEXT_RANGE));
		AnalysisTestUtils.testFailedConstruction(() -> new Comment(this.mockAnalyzer, null, TEXT_RANGE));
		AnalysisTestUtils.testFailedConstruction(() -> new Comment(this.mockAnalyzer, CONTENT, null));
		AnalysisTestUtils.testFailedConstruction(() -> new Comment(this.mockAnalyzer, "", TEXT_RANGE));
	}

	@Test
	public void testEquals() {
		final Comment comment = new Comment(this.mockAnalyzer, CONTENT, TEXT_RANGE);
		final Comment equalComment = new Comment(this.mockAnalyzer, CONTENT, TEXT_RANGE);
		final Comment notEqualComment = new Comment(this.mockAnalyzer, "different content", TEXT_RANGE);

		Assert.assertEquals(comment, comment);
		Assert.assertEquals(comment, equalComment);
		Assert.assertEquals(equalComment, comment);
		Assert.assertFalse(comment.equals(notEqualComment));
		Assert.assertFalse(notEqualComment.equals(comment));
		Assert.assertFalse(comment.equals(null));
		Assert.assertFalse(comment.equals("random string"));
	}

	@Test
	public void testHashCode() {
		final Comment comment = new Comment(this.mockAnalyzer, CONTENT, TEXT_RANGE);
		final Comment equalComment = new Comment(this.mockAnalyzer, CONTENT, TEXT_RANGE);
		final Comment notEqualComment = new Comment(this.mockAnalyzer, "different content", TEXT_RANGE);

		Assert.assertEquals(comment.hashCode(), comment.hashCode());
		Assert.assertEquals(comment.hashCode(), equalComment.hashCode());
		Assert.assertEquals(equalComment.hashCode(), comment.hashCode());
		Assert.assertFalse(comment.hashCode() == notEqualComment.hashCode());
		Assert.assertFalse(notEqualComment.hashCode() == comment.hashCode());
	}

	@Test
	public void testCompareTo() {
		final Comment comment = new Comment(this.mockAnalyzer, CONTENT, TEXT_RANGE);
		final Comment equalComment = new Comment(this.mockAnalyzer, CONTENT, TEXT_RANGE);
		final Comment lesserComment = new Comment(this.mockAnalyzer, CONTENT, TEXT_RANGE.translate(-2));
		final Comment greaterComment = new Comment(this.mockAnalyzer, CONTENT, TEXT_RANGE.translate(2));

		Assert.assertEquals(0, comment.compareTo(comment));

		Assert.assertEquals(0, equalComment.compareTo(comment));
		Assert.assertEquals(0, comment.compareTo(equalComment));

		Assert.assertTrue(comment.compareTo(lesserComment) > 0);
		Assert.assertTrue(lesserComment.compareTo(comment) < 0);

		Assert.assertTrue(comment.compareTo(greaterComment) < 0);
		Assert.assertTrue(greaterComment.compareTo(comment) > 0);

		Assert.assertTrue(lesserComment.compareTo(greaterComment) < 0);
		Assert.assertTrue(greaterComment.compareTo(lesserComment) > 0);
	}
}
