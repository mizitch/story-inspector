package com.story_inspector.analysis;

import static com.story_inspector.analysis.AnalysisTestUtils.testFailedConstruction;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.easymock.EasyMock;
import org.junit.Before;
import org.junit.Test;

import com.story_inspector.analysis.AnalysisTestUtils.TestAnalyzerType;

import junit.framework.Assert;

public class AnalyzerSpecTest {
	private static final String ANALYZER_NAME = "Name";
	private static final String ANALYZER_DESCRIPTION = "Description";
	private static final boolean COMMENT_RECORDING_SUPPRESSED = false;
	@SuppressWarnings("serial")
	private static final Map<String, Object> ANALYZER_PARAMETER_VALUES = Collections.unmodifiableMap(new HashMap<String, Object>() {
		{
			put("id1", "value");
			put("id2", 3);
		}
	});

	private TestAnalyzerType testAnalyzerType;

	@Before
	public void initialize() {
		this.testAnalyzerType = EasyMock.mock(TestAnalyzerType.class);
	}

	@Test
	public void testHappy() {
		final AnalyzerSpec<TestAnalyzerType> spec = new AnalyzerSpec<TestAnalyzerType>(ANALYZER_NAME, ANALYZER_DESCRIPTION, this.testAnalyzerType,
				COMMENT_RECORDING_SUPPRESSED, ANALYZER_PARAMETER_VALUES);

		Assert.assertEquals(this.testAnalyzerType, spec.getAnalyzerType());
		Assert.assertEquals(ANALYZER_NAME, spec.getName());
		Assert.assertEquals(ANALYZER_DESCRIPTION, spec.getDescription());
		Assert.assertEquals(COMMENT_RECORDING_SUPPRESSED, spec.isCommentRecordingSuppressed());
		Assert.assertEquals(ANALYZER_PARAMETER_VALUES, spec.getAnalyzerParameterValues());
	}

	@Test
	public void testFailedConstructions() {
		testFailedConstruction(() -> new AnalyzerSpec<TestAnalyzerType>(ANALYZER_NAME, ANALYZER_DESCRIPTION, this.testAnalyzerType,
				COMMENT_RECORDING_SUPPRESSED, null));

		testFailedConstruction(() -> new AnalyzerSpec<TestAnalyzerType>(ANALYZER_NAME, ANALYZER_DESCRIPTION, null, COMMENT_RECORDING_SUPPRESSED,
				ANALYZER_PARAMETER_VALUES));
	}

	@Test
	public void testEquals() {
		final AnalyzerSpec<TestAnalyzerType> spec = new AnalyzerSpec<TestAnalyzerType>(ANALYZER_NAME, ANALYZER_DESCRIPTION, this.testAnalyzerType,
				COMMENT_RECORDING_SUPPRESSED, ANALYZER_PARAMETER_VALUES);

		final AnalyzerSpec<TestAnalyzerType> equalSpec = new AnalyzerSpec<TestAnalyzerType>(ANALYZER_NAME, ANALYZER_DESCRIPTION,
				this.testAnalyzerType, COMMENT_RECORDING_SUPPRESSED, ANALYZER_PARAMETER_VALUES);

		final AnalyzerSpec<TestAnalyzerType> notEqualSpec = new AnalyzerSpec<TestAnalyzerType>("Different name", ANALYZER_DESCRIPTION,
				this.testAnalyzerType, COMMENT_RECORDING_SUPPRESSED, ANALYZER_PARAMETER_VALUES);

		Assert.assertEquals(spec, spec);
		Assert.assertEquals(spec, equalSpec);
		Assert.assertEquals(equalSpec, spec);
		Assert.assertFalse(spec.equals(notEqualSpec));
		Assert.assertFalse(notEqualSpec.equals(spec));
		Assert.assertFalse(spec.equals(null));
		Assert.assertFalse(spec.equals("random string"));

	}

	@Test
	public void testHashCode() {
		final AnalyzerSpec<TestAnalyzerType> spec = new AnalyzerSpec<TestAnalyzerType>(ANALYZER_NAME, ANALYZER_DESCRIPTION, this.testAnalyzerType,
				COMMENT_RECORDING_SUPPRESSED, ANALYZER_PARAMETER_VALUES);

		final AnalyzerSpec<TestAnalyzerType> equalSpec = new AnalyzerSpec<TestAnalyzerType>(ANALYZER_NAME, ANALYZER_DESCRIPTION,
				this.testAnalyzerType, COMMENT_RECORDING_SUPPRESSED, ANALYZER_PARAMETER_VALUES);

		final AnalyzerSpec<TestAnalyzerType> notEqualSpec = new AnalyzerSpec<TestAnalyzerType>("Different name", ANALYZER_DESCRIPTION,
				this.testAnalyzerType, COMMENT_RECORDING_SUPPRESSED, ANALYZER_PARAMETER_VALUES);

		Assert.assertEquals(spec.hashCode(), spec.hashCode());
		Assert.assertEquals(spec.hashCode(), equalSpec.hashCode());
		Assert.assertEquals(equalSpec.hashCode(), spec.hashCode());
		Assert.assertFalse(spec.hashCode() == notEqualSpec.hashCode());
		Assert.assertFalse(notEqualSpec.hashCode() == spec.hashCode());

	}
}
