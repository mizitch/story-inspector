package com.story_inspector.test.limitedIntegration.io;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.HashMap;
import java.util.Map;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import com.story_inspector.analysis.Analyzer;
import com.story_inspector.analysis.AnalyzerCreationResult;
import com.story_inspector.analysis.AnalyzerSpec;
import com.story_inspector.analysis.analyzers.WordSearchAnalyzerType;
import com.story_inspector.analysis.parameterTypes.DialogueSearchPattern;
import com.story_inspector.analysis.parameterTypes.StringSet;
import com.story_inspector.analysis.serialization.AnalysisSerializer;
import com.story_inspector.test.SpringBasedTest;
import com.story_inspector.test.TestUtils;

import junit.framework.Assert;

public class AnalyzerSpecSerializationTest extends SpringBasedTest {

	@Autowired
	private AnalysisSerializer serializer;

	@Autowired
	private WordSearchAnalyzerType wordSearchAnalyzerType;

	@Test
	public void testBasic() throws Exception {
		// Initialize spec
		final Map<String, Object> parameterValues = new HashMap<>();
		final StringSet wordList = new StringSet();
		wordList.add("whisper");
		wordList.add("stare");
		parameterValues.put("searchWords", wordList);
		parameterValues.put("searchByStem", true);
		parameterValues.put("dialogueSearchPattern", DialogueSearchPattern.ALL_BUT_DIALOGUE);
		final AnalyzerSpec<WordSearchAnalyzerType> spec = new AnalyzerSpec<WordSearchAnalyzerType>("Mitch's word search 3", "Mitch!",
				this.wordSearchAnalyzerType, false, parameterValues);
		final AnalyzerCreationResult<WordSearchAnalyzerType> creationResult = this.wordSearchAnalyzerType.tryCreateAnalyzer(spec);
		Assert.assertTrue(creationResult.wasSuccessful());
		final Analyzer<WordSearchAnalyzerType> analyzer = creationResult.getAnalyzer();

		// Write and read spec
		final File testFile = createTestFile();
		this.serializer.writeAnalyzer(analyzer, new FileOutputStream(testFile));
		@SuppressWarnings("unchecked")
		final Analyzer<WordSearchAnalyzerType> readAnalyzer = (Analyzer<WordSearchAnalyzerType>) this.serializer
				.readAnalyzer(new FileInputStream(testFile));
		final AnalyzerSpec<WordSearchAnalyzerType> readSpec = readAnalyzer.extractAnalyzerSpec();

		// Assert that version read from file matches original spec
		TestUtils.assertReflectionEquals(spec, readSpec);
	}
}
