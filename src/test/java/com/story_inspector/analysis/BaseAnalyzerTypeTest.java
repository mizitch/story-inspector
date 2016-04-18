package com.story_inspector.analysis;

import static com.story_inspector.analysis.AnalysisTestUtils.createParameterSpec;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import org.easymock.EasyMock;
import org.junit.Before;
import org.junit.Test;

import junit.framework.Assert;

public class BaseAnalyzerTypeTest {
	private static final int TEST_ANALYZER_TYPE_VERSION = 1;
	private static final String TEST_ANALYZER_TYPE_ID = "TestAnalyzerType";
	private static final String TEST_ANALYZER_TYPE_DESCRIPTION = "A test analyzer type that doesn't do anything";
	private static final String TEST_ANALYZER_TYPE_NAME = "Test Analyzer Type";

	private class TestAnalyzerType extends BaseAnalyzerType<TestAnalyzerType> {

		@SuppressWarnings("unchecked")
		private final Analyzer<TestAnalyzerType> mockAnalyzer = EasyMock.createMock(Analyzer.class);

		private TestAnalyzerType(final List<ParameterSpec<?>> parameterSpecs) {
			super(TEST_ANALYZER_TYPE_NAME, TEST_ANALYZER_TYPE_DESCRIPTION, TEST_ANALYZER_TYPE_ID, TEST_ANALYZER_TYPE_VERSION, false, parameterSpecs);
			EasyMock.expect(this.mockAnalyzer.getAnalyzerType()).andReturn(this);
			EasyMock.replay(this.mockAnalyzer);
		}

		@Override
		protected Analyzer<TestAnalyzerType> createAnalyzer(final AnalyzerSpec<TestAnalyzerType> spec) {
			return this.mockAnalyzer;
		}

	}

	private class PassthroughAnalyzerType extends BaseAnalyzerType<PassthroughAnalyzerType> {

		private PassthroughAnalyzerType(final String name, final String description, final String id, final int version,
				final boolean producesComments, final List<ParameterSpec<?>> parameterSpecs) {
			super(name, description, id, version, producesComments, parameterSpecs);
		}

		@Override
		protected Analyzer<PassthroughAnalyzerType> createAnalyzer(final AnalyzerSpec<PassthroughAnalyzerType> spec) {
			throw new UnsupportedOperationException();
		}

	}

	private String stringArgId;
	private String intArgId;

	private ParameterSpec<String> stringParameterSpec;
	private ParameterSpec<Integer> intParameterSpec;
	private TestAnalyzerType testAnalyzerType;

	@Before
	public void initializeTestAnalyzerType() {
		this.stringArgId = "stringArg";
		this.intArgId = "intArg";

		this.stringParameterSpec = createParameterSpec(this.stringArgId, String.class, s -> s.length() == 2);
		this.intParameterSpec = createParameterSpec(this.intArgId, Integer.class, i -> i >= 0);
		this.testAnalyzerType = new TestAnalyzerType(Arrays.asList(this.stringParameterSpec, this.intParameterSpec));
	}

	@Test
	public void testBasicGetters() {
		final TestAnalyzerType analyzerType = new TestAnalyzerType(Collections.emptyList());
		Assert.assertEquals(TEST_ANALYZER_TYPE_NAME, analyzerType.getName());
		Assert.assertEquals(TEST_ANALYZER_TYPE_DESCRIPTION, analyzerType.getDescription());
		Assert.assertEquals(TEST_ANALYZER_TYPE_ID, analyzerType.getId());
		Assert.assertEquals(TEST_ANALYZER_TYPE_VERSION, analyzerType.getVersion());
		Assert.assertEquals(Collections.emptyList(), analyzerType.getParameterSpecs());
	}

	@Test
	public void testConstructor() {
		// First test a valid construction
		new PassthroughAnalyzerType(TEST_ANALYZER_TYPE_NAME, TEST_ANALYZER_TYPE_DESCRIPTION, TEST_ANALYZER_TYPE_ID, TEST_ANALYZER_TYPE_VERSION, false,
				Collections.emptyList());

		// Then test a bunch of illegal constructions
		// Blank id
		testIllegalConstruction(() -> new PassthroughAnalyzerType(TEST_ANALYZER_TYPE_NAME, TEST_ANALYZER_TYPE_DESCRIPTION, "",
				TEST_ANALYZER_TYPE_VERSION, false, Collections.emptyList()));

		// Version of 0
		testIllegalConstruction(() -> new PassthroughAnalyzerType(TEST_ANALYZER_TYPE_NAME, TEST_ANALYZER_TYPE_DESCRIPTION, TEST_ANALYZER_TYPE_ID, 0,
				false, Collections.emptyList()));

		// Version of -1
		testIllegalConstruction(() -> new PassthroughAnalyzerType(TEST_ANALYZER_TYPE_NAME, TEST_ANALYZER_TYPE_DESCRIPTION, TEST_ANALYZER_TYPE_ID, -1,
				false, Collections.emptyList()));

		// Null parameter specs
		testIllegalConstruction(() -> new PassthroughAnalyzerType(TEST_ANALYZER_TYPE_NAME, TEST_ANALYZER_TYPE_DESCRIPTION, TEST_ANALYZER_TYPE_ID,
				TEST_ANALYZER_TYPE_VERSION, false, Arrays.asList(null, null)));

		// Parameter specs with duplicate ids
		testIllegalConstruction(() -> new PassthroughAnalyzerType(TEST_ANALYZER_TYPE_NAME, TEST_ANALYZER_TYPE_DESCRIPTION, TEST_ANALYZER_TYPE_ID,
				TEST_ANALYZER_TYPE_VERSION, false,
				Arrays.asList(createParameterSpec("id", String.class, s -> true), createParameterSpec("id", Boolean.class, b -> true))));
	}

	private void testIllegalConstruction(final Supplier<BaseAnalyzerType<?>> creator) {
		try {
			creator.get();
		} catch (final IllegalArgumentException e) {
			return;
		}
		Assert.fail("Successfully created base analyzer type, shouldn't be possible");
	}

	@Test
	public void testHappyAnalyzerCreation() {
		final AnalyzerCreationResult<TestAnalyzerType> result = tryCreateAnalyzer("no", 3);

		Assert.assertTrue(result.wasSuccessful());
		Assert.assertTrue(result.getGlobalResult().isValid());
		Assert.assertEquals(new HashSet<String>(Arrays.asList(this.intArgId, this.stringArgId)),
				result.getParameterResults().keySet().stream().map(ParameterSpec::getId).collect(Collectors.toSet()));
		Assert.assertTrue(result.getParameterResults().values().stream().allMatch(ValidationResult::isValid));
		Assert.assertEquals(this.testAnalyzerType.mockAnalyzer, result.getAnalyzer());
	}

	@Test
	public void testInvalidParameter() {
		final AnalyzerCreationResult<TestAnalyzerType> result = tryCreateAnalyzer("no", -1);

		Assert.assertFalse(result.wasSuccessful());
		Assert.assertTrue(result.getGlobalResult().isValid());
		Assert.assertEquals(new HashSet<>(Arrays.asList(this.stringParameterSpec, this.intParameterSpec)), result.getParameterResults().keySet());
		Assert.assertTrue(result.getParameterResults().get(this.stringParameterSpec).isValid());
		Assert.assertFalse(result.getParameterResults().get(this.intParameterSpec).isValid());
		Assert.assertNull(result.getAnalyzer());
	}

	@SuppressWarnings("serial")
	@Test
	public void testWrongParameterType() {
		final AnalyzerCreationResult<TestAnalyzerType> result = tryCreateAnalyzer(new HashMap<String, Object>() {
			{
				put(BaseAnalyzerTypeTest.this.stringArgId, true);
				put(BaseAnalyzerTypeTest.this.intArgId, 3);
			}
		});

		Assert.assertFalse(result.wasSuccessful());
		Assert.assertTrue(result.getGlobalResult().isValid());
		Assert.assertEquals(new HashSet<>(Arrays.asList(this.stringParameterSpec, this.intParameterSpec)), result.getParameterResults().keySet());
		Assert.assertFalse(result.getParameterResults().get(this.stringParameterSpec).isValid());
		Assert.assertTrue(result.getParameterResults().get(this.intParameterSpec).isValid());
		Assert.assertNull(result.getAnalyzer());
	}

	@SuppressWarnings("serial")
	@Test
	public void testExtraParameter() {
		final AnalyzerCreationResult<TestAnalyzerType> result = tryCreateAnalyzer(new HashMap<String, Object>() {
			{
				put(BaseAnalyzerTypeTest.this.stringArgId, "no");
				put(BaseAnalyzerTypeTest.this.intArgId, 3);
				put("extraParameter", true);
			}
		});

		Assert.assertFalse(result.wasSuccessful());
		Assert.assertFalse(result.getGlobalResult().isValid());
		Assert.assertEquals(new HashSet<>(Arrays.asList(this.stringParameterSpec, this.intParameterSpec)), result.getParameterResults().keySet());
		Assert.assertTrue(result.getParameterResults().values().stream().allMatch(ValidationResult::isValid));
		Assert.assertNull(result.getAnalyzer());
	}

	@SuppressWarnings("serial")
	@Test
	public void testWrongAnalyzerType() {
		final TestAnalyzerType wrongAnalyzerType = new TestAnalyzerType(Arrays.asList(this.stringParameterSpec, this.intParameterSpec));

		final AnalyzerCreationResult<TestAnalyzerType> result = this.testAnalyzerType.tryCreateAnalyzer(new AnalyzerSpec<TestAnalyzerType>(
				"Test analyzer spec", "Test analyzer spec description", wrongAnalyzerType, false, new HashMap<String, Object>() {
					{
						put(BaseAnalyzerTypeTest.this.stringArgId, "no");
						put(BaseAnalyzerTypeTest.this.intArgId, 3);
					}
				}));

		Assert.assertFalse(result.wasSuccessful());
		Assert.assertFalse(result.getGlobalResult().isValid());
		Assert.assertEquals(new HashSet<>(Arrays.asList(this.stringParameterSpec, this.intParameterSpec)), result.getParameterResults().keySet());
		Assert.assertTrue(result.getParameterResults().values().stream().allMatch(ValidationResult::isValid));
		Assert.assertNull(result.getAnalyzer());
	}

	@SuppressWarnings("serial")
	private AnalyzerCreationResult<TestAnalyzerType> tryCreateAnalyzer(final String stringArg, final Integer intArg) {
		return tryCreateAnalyzer(new HashMap<String, Object>() {
			{
				put(BaseAnalyzerTypeTest.this.stringArgId, stringArg);
				put(BaseAnalyzerTypeTest.this.intArgId, intArg);
			}
		});
	}

	private AnalyzerCreationResult<TestAnalyzerType> tryCreateAnalyzer(final Map<String, Object> parameterValues) {
		return this.testAnalyzerType.tryCreateAnalyzer(createAnalyzerSpec(parameterValues));
	}

	private AnalyzerSpec<TestAnalyzerType> createAnalyzerSpec(final Map<String, Object> parameterValues) {
		return new AnalyzerSpec<TestAnalyzerType>("Test analyzer spec", "Test analyzer spec description", this.testAnalyzerType, false,
				parameterValues);
	}
}
