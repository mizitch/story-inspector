package com.story_inspector.test.limitedIntegration.inspection;

import java.io.File;
import java.io.FileOutputStream;
import java.util.Arrays;
import java.util.HashMap;

import org.junit.Test;

import com.story_inspector.analysis.Analyzer;
import com.story_inspector.analysis.AnalyzerSpec;
import com.story_inspector.analysis.analyzers.WordSearchAnalyzerType;
import com.story_inspector.analysis.parameterTypes.DialogueSearchPattern;
import com.story_inspector.analysis.parameterTypes.StringSet;
import com.story_inspector.analysis.reports.Report;
import com.story_inspector.analysis.reports.ReportSectionSpec;
import com.story_inspector.analysis.reports.ReportSpec;

import junit.framework.Assert;

public class NovelTest extends InspectionTest {

	@Test
	public void testMonteCristo() throws Exception {
		final File outputFile = super.createTestFile(".docx");
		final Report result = executeInspectionTest(super.getStoryInputStream("count_of_monte_cristo.docx"), createTestReportSpec(),
				new FileOutputStream(outputFile), (percentage, message) -> {
				});
		Assert.assertFalse(result.getReportSections().get(0).getAnalyzerResults().get(0).getComments().isEmpty());
		// TODO more assertions around behavior, on the generated report, generated file and at least something on the progress monitor
		// count of monte cristo is a pretty big document, so getting too exact seems counter-productive
		// don't want to assert strict equality between generated report and saved report (or file). Makes it brittle to minor implementation
		// changes (like formatting, what text goes in comment contents, adding a new section to report summary, etc) which I don't really want
		// Maybe have pregenerated report and output file and make some looser comparisons between the two?
	}

	// TODO: pull this from file too?
	private ReportSpec createTestReportSpec() {

		final WordSearchAnalyzerType analyzerType = getAnalyzerTypeByJavaType(WordSearchAnalyzerType.class);

		final AnalyzerSpec<WordSearchAnalyzerType> analyzerSpec = new AnalyzerSpec<WordSearchAnalyzerType>("Test word search",
				"Searches for 'stare' and 'whisper' with stemming on, excluding dialogue", analyzerType, false, new HashMap<String, Object>() {
					{
						put("searchWords", new StringSet(Arrays.asList("whisper", "stare")));
						put("searchByStem", true);
						put("dialogueSearchPattern", DialogueSearchPattern.ALL_BUT_DIALOGUE);
					}
				});
		final Analyzer<?> analyzer = analyzerType.tryCreateAnalyzer(analyzerSpec).getAnalyzer();

		final ReportSectionSpec sectionSpec = new ReportSectionSpec("Test section", "Section of test report", Arrays.asList(analyzer));

		return new ReportSpec("Test Report", "A test report, does a simple word search", Arrays.asList(sectionSpec));
	}
}
