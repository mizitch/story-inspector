package com.story_inspector.analysis.summary;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import com.story_inspector.story.Story;
import com.story_inspector.story.TextRange;

/**
 * Collection of utility methods for generating report summaries.
 *
 * @author mizitch
 *
 */
public class SummaryGenerators {

	private SummaryGenerators() {
		throw new UnsupportedOperationException("This is a collection of utility methods, don't instantiate");
	}

	/**
	 * Generates summary components to cover frequency of some type of entity encountered within a story.
	 *
	 * @param story
	 *            The {@link Story} searched.
	 * @param matches
	 *            The collection of {@link TextRange}s where the entity was found.
	 * @return
	 */
	public static List<AnalyzerSummaryComponent> generateBasicFrequencySummary(final Story story, final Collection<TextRange> matches) {
		final AnalyzerSummaryComponent statistics = new FrequencyStatisticsSummaryComponent(story, matches);
		final AnalyzerSummaryComponent heatMap = new HeatMapSummaryComponent(story, matches);

		return Arrays.asList(statistics, heatMap);
	}
}
