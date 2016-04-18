package com.story_inspector.analysis;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.easymock.EasyMock;
import org.junit.Before;
import org.junit.Test;

import com.story_inspector.story.Story;

import junit.framework.Assert;

public class BaseAnalyzerTest {

	private static final boolean TEST_ANALYZER_COMMENT_RECORDING_SUPPRESSED = false;

	private static final String TEST_ANALYZER_DESCRIPTION = "Description";

	private static final String TEST_ANALYZER_NAME = "Name";

	private abstract class TestAnalyzerType implements AnalyzerType<TestAnalyzerType> {

	}

	private class TestAnalyzer extends BaseAnalyzer<TestAnalyzerType> {
		@SuppressWarnings("unchecked")
		private final AnalyzerResult<TestAnalyzerType> mockResult = EasyMock.createMock(AnalyzerResult.class);

		private final AnalyzerSpec<TestAnalyzerType> spec;

		private TestAnalyzer(final AnalyzerSpec<TestAnalyzerType> spec) {
			super(spec);
			this.spec = spec;
		}

		@Override
		public AnalyzerResult<TestAnalyzerType> execute(final Story story) {
			return this.mockResult;
		}

		@Override
		protected Map<String, Object> retrieveParameterValues() {
			return this.spec.getAnalyzerParameterValues();
		}

	}

	private TestAnalyzerType testAnalyzerType;

	private AnalyzerSpec<TestAnalyzerType> testAnalyzerSpec;

	private TestAnalyzer testAnalyzer;

	private TestAnalyzer equalTestAnalyzer;

	private TestAnalyzer notEqualTestAnalyzer;

	@SuppressWarnings("serial")
	private final Map<String, Object> testParameterValues = new HashMap<String, Object>() {
		{
			put("key1", "value1");
			put("key2", "value2");
		}
	};

	@Before
	public void initializeTestAnalyzers() {
		this.testAnalyzerType = EasyMock.createMock(TestAnalyzerType.class);
		this.testAnalyzerSpec = new AnalyzerSpec<TestAnalyzerType>(TEST_ANALYZER_NAME, TEST_ANALYZER_DESCRIPTION, this.testAnalyzerType,
				TEST_ANALYZER_COMMENT_RECORDING_SUPPRESSED, this.testParameterValues);
		this.testAnalyzer = new TestAnalyzer(this.testAnalyzerSpec);

		// Separate instance from same spec
		this.equalTestAnalyzer = new TestAnalyzer(this.testAnalyzerSpec);
		// Same except empty parameter values
		this.notEqualTestAnalyzer = new TestAnalyzer(new AnalyzerSpec<TestAnalyzerType>(TEST_ANALYZER_NAME, TEST_ANALYZER_DESCRIPTION,
				this.testAnalyzerType, TEST_ANALYZER_COMMENT_RECORDING_SUPPRESSED, Collections.emptyMap()));
	}

	@Test
	public void testBasicGetters() {
		Assert.assertEquals(this.testAnalyzerType, this.testAnalyzer.getAnalyzerType());
		Assert.assertEquals(TEST_ANALYZER_NAME, this.testAnalyzer.getName());
		Assert.assertEquals(TEST_ANALYZER_DESCRIPTION, this.testAnalyzer.getDescription());
		Assert.assertEquals(TEST_ANALYZER_COMMENT_RECORDING_SUPPRESSED, this.testAnalyzer.isCommentRecordingSuppressed());
	}

	@Test
	public void testExtractAnalyzerSpec() {
		final AnalyzerSpec<TestAnalyzerType> extractedSpec = this.testAnalyzer.extractAnalyzerSpec();
		Assert.assertEquals(this.testAnalyzerType, extractedSpec.getAnalyzerType());
		Assert.assertEquals(TEST_ANALYZER_NAME, extractedSpec.getName());
		Assert.assertEquals(TEST_ANALYZER_DESCRIPTION, extractedSpec.getDescription());
		Assert.assertEquals(TEST_ANALYZER_COMMENT_RECORDING_SUPPRESSED, extractedSpec.isCommentRecordingSuppressed());
		Assert.assertEquals(this.testParameterValues, extractedSpec.getAnalyzerParameterValues());
	}

	@Test
	public void testEquals() {
		Assert.assertEquals(this.testAnalyzer, this.testAnalyzer);
		Assert.assertEquals(this.testAnalyzer, this.equalTestAnalyzer);
		Assert.assertFalse(this.testAnalyzer.equals(this.notEqualTestAnalyzer));
		Assert.assertFalse(this.testAnalyzer.equals(null));
		Assert.assertFalse(this.testAnalyzer.equals("Random string"));
	}

	@Test
	public void testHashCode() {
		Assert.assertEquals(this.testAnalyzer.hashCode(), this.testAnalyzer.hashCode());
		Assert.assertEquals(this.testAnalyzer.hashCode(), this.equalTestAnalyzer.hashCode());
		Assert.assertFalse(this.testAnalyzer.hashCode() == this.notEqualTestAnalyzer.hashCode());
	}
}
