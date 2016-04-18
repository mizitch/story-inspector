package com.story_inspector.analysis.summary;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.apache.commons.lang3.tuple.ImmutablePair;

import com.story_inspector.analysis.Analyzer;
import com.story_inspector.story.Chapter;
import com.story_inspector.story.Paragraph;
import com.story_inspector.story.Scene;
import com.story_inspector.story.Sentence;
import com.story_inspector.story.Story;
import com.story_inspector.story.TextNode;
import com.story_inspector.story.TextRange;

/**
 * A basic summary of frequency statistics for an {@link Analyzer} that finds entities within a story.
 *
 * @author mizitch
 *
 */
public class FrequencyStatisticsSummaryComponent extends KeyValueListSummaryComponent {

	/**
	 * Create a new instance.
	 *
	 * @param story
	 *            The analyzed story.
	 * @param matches
	 *            The matches found in the story.
	 */
	public FrequencyStatisticsSummaryComponent(final Story story, final Collection<TextRange> matches) {
		super(generateKeyValuePairs(story, matches));
	}

	private static List<ImmutablePair<String, String>> generateKeyValuePairs(final Story story, final Collection<TextRange> matches) {
		final List<ImmutablePair<String, String>> pairs = new ArrayList<>();
		pairs.add(new ImmutablePair<>("Total #", String.valueOf(matches.size())));
		pairs.add(new ImmutablePair<>("Max in single chapter", String.valueOf(getMaxPerNodeType(story, matches, Chapter.class))));
		pairs.add(new ImmutablePair<>("Max in single scene", String.valueOf(getMaxPerNodeType(story, matches, Scene.class))));
		pairs.add(new ImmutablePair<>("Max in single paragraph", String.valueOf(getMaxPerNodeType(story, matches, Paragraph.class))));
		pairs.add(new ImmutablePair<>("Max in single sentence", String.valueOf(getMaxPerNodeType(story, matches, Sentence.class))));
		return pairs;
	}

	private static <T extends TextNode> int getMaxPerNodeType(final Story story, final Collection<TextRange> matches, final Class<T> nodeType) {
		int maxSoFar = 0;
		for (final T node : story.getChildrenAtLevel(nodeType)) {
			int count = 0;
			for (final TextRange range : matches) {
				if (node.getRange().intersects(range))
					count++;
			}
			if (count > maxSoFar)
				maxSoFar = count;
		}
		return maxSoFar;
	}
}
