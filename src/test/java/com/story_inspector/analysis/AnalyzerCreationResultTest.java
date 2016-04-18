package com.story_inspector.analysis;

import static com.story_inspector.analysis.AnalysisTestUtils.testFailedConstruction;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import org.easymock.EasyMock;
import org.junit.Before;
import org.junit.Test;

import com.story_inspector.analysis.AnalysisTestUtils.TestAnalyzerType;

import junit.framework.Assert;

public class AnalyzerCreationResultTest {

	private TestAnalyzerType testAnalyzerType;
	private Analyzer<TestAnalyzerType> testAnalyzer;
	private final String stringArgId = "stringArg";
	private final String intArgId = "intArg";
	private final ParameterSpec<String> stringParameterSpec = AnalysisTestUtils.createParameterSpec(this.stringArgId, String.class);
	private final ParameterSpec<Integer> intParameterSpec = AnalysisTestUtils.createParameterSpec(this.intArgId, Integer.class);

	@Before
	@SuppressWarnings("unchecked")
	public void initialize() {
		this.testAnalyzerType = EasyMock.createMock(TestAnalyzerType.class);
		EasyMock.expect(this.testAnalyzerType.getParameterSpecs()).andReturn(Arrays.asList(this.stringParameterSpec, this.intParameterSpec))
				.anyTimes();
		this.testAnalyzer = EasyMock.createMock(Analyzer.class);
		EasyMock.expect(this.testAnalyzer.getAnalyzerType()).andReturn(this.testAnalyzerType).anyTimes();
		EasyMock.replay(this.testAnalyzerType, this.testAnalyzer);
	}

	@Test
	public void testSuccessfulCreation() {
		final AnalyzerCreationResult<TestAnalyzerType> successfulResult = new AnalyzerCreationResult<TestAnalyzerType>(this.testAnalyzer,
				ValidationResult.validResult(), createParameterResults(ValidationResult.validResult(), ValidationResult.validResult()));
		Assert.assertTrue(successfulResult.wasSuccessful());
		Assert.assertEquals(this.testAnalyzer, successfulResult.getAnalyzer());
		Assert.assertTrue(successfulResult.getGlobalResult().isValid());
		Assert.assertEquals(successfulResult.getParameterResults().keySet(),
				new HashSet<>(Arrays.asList(this.stringParameterSpec, this.intParameterSpec)));
		Assert.assertTrue(successfulResult.getParameterResults().get(this.stringParameterSpec).isValid());
		Assert.assertTrue(successfulResult.getParameterResults().get(this.intParameterSpec).isValid());
	}

	@Test
	public void testGlobalResultInvalid() {
		final AnalyzerCreationResult<TestAnalyzerType> failedResult = new AnalyzerCreationResult<TestAnalyzerType>(this.testAnalyzerType,
				ValidationResult.invalidResult("Something global wrong"),
				createParameterResults(ValidationResult.validResult(), ValidationResult.validResult()));
		Assert.assertFalse(failedResult.wasSuccessful());
		Assert.assertNull(failedResult.getAnalyzer());
		Assert.assertFalse(failedResult.getGlobalResult().isValid());
		Assert.assertEquals(failedResult.getParameterResults().keySet(),
				new HashSet<>(Arrays.asList(this.stringParameterSpec, this.intParameterSpec)));
		Assert.assertTrue(failedResult.getParameterResults().get(this.stringParameterSpec).isValid());
		Assert.assertTrue(failedResult.getParameterResults().get(this.intParameterSpec).isValid());
	}

	@Test
	public void testParameterResultInvalid() {
		final AnalyzerCreationResult<TestAnalyzerType> failedResult = new AnalyzerCreationResult<TestAnalyzerType>(this.testAnalyzerType,
				ValidationResult.validResult(), createParameterResults(ValidationResult.validResult(), ValidationResult.invalidResult("bad int")));
		Assert.assertFalse(failedResult.wasSuccessful());
		Assert.assertNull(failedResult.getAnalyzer());
		Assert.assertTrue(failedResult.getGlobalResult().isValid());
		Assert.assertEquals(failedResult.getParameterResults().keySet(),
				new HashSet<>(Arrays.asList(this.stringParameterSpec, this.intParameterSpec)));
		Assert.assertTrue(failedResult.getParameterResults().get(this.stringParameterSpec).isValid());
		Assert.assertFalse(failedResult.getParameterResults().get(this.intParameterSpec).isValid());
	}

	@SuppressWarnings("serial")
	@Test
	public void testFailedConstructions() {
		final TestAnalyzerType nullAnalyzerType = null;
		final Analyzer<TestAnalyzerType> nullAnalyzer = null;

		// Null analyzer type
		testFailedConstruction(() -> new AnalyzerCreationResult<TestAnalyzerType>(nullAnalyzerType, ValidationResult.validResult(),
				createParameterResults(ValidationResult.validResult(), ValidationResult.validResult())));

		// Null global results
		testFailedConstruction(() -> new AnalyzerCreationResult<TestAnalyzerType>(this.testAnalyzer, null,
				createParameterResults(ValidationResult.validResult(), ValidationResult.validResult())));

		// Null parameter results
		testFailedConstruction(() -> new AnalyzerCreationResult<TestAnalyzerType>(this.testAnalyzer, ValidationResult.validResult(), null));

		// Null analyzer but valid results
		testFailedConstruction(() -> new AnalyzerCreationResult<TestAnalyzerType>(nullAnalyzer, ValidationResult.validResult(),
				createParameterResults(ValidationResult.validResult(), ValidationResult.validResult())));

		// Non-null analyzer but global result invalid
		testFailedConstruction(() -> new AnalyzerCreationResult<TestAnalyzerType>(this.testAnalyzer, ValidationResult.invalidResult("bad global"),
				createParameterResults(ValidationResult.validResult(), ValidationResult.validResult())));

		// Non-null analyzer but parameter result invalid
		testFailedConstruction(() -> new AnalyzerCreationResult<TestAnalyzerType>(this.testAnalyzer, ValidationResult.validResult(),
				createParameterResults(ValidationResult.invalidResult("bad string"), ValidationResult.validResult())));

		// Missing parameter results
		testFailedConstruction(
				() -> new AnalyzerCreationResult<TestAnalyzerType>(this.testAnalyzer, ValidationResult.validResult(), Collections.emptyMap()));

		// Extra parameter results
		testFailedConstruction(() -> new AnalyzerCreationResult<TestAnalyzerType>(this.testAnalyzer, ValidationResult.validResult(),
				new HashMap<ParameterSpec<?>, ValidationResult>() {
					{
						put(AnalyzerCreationResultTest.this.stringParameterSpec, ValidationResult.validResult());
						put(AnalyzerCreationResultTest.this.intParameterSpec, ValidationResult.validResult());
						put(AnalysisTestUtils.createParameterSpec("extraId", Boolean.class), ValidationResult.validResult());
					}
				}));
	}

	@SuppressWarnings("serial")
	private Map<ParameterSpec<?>, ValidationResult> createParameterResults(final ValidationResult stringResult, final ValidationResult intResult) {
		return new HashMap<ParameterSpec<?>, ValidationResult>() {
			{
				put(AnalyzerCreationResultTest.this.stringParameterSpec, stringResult);
				put(AnalyzerCreationResultTest.this.intParameterSpec, intResult);
			}
		};
	}
}
