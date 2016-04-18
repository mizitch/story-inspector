package com.story_inspector.analysis;

import com.story_inspector.story.Story;

/**
 * Analyzes a story and produces a set of comments on the story as well as some type of summary. Usually customizable based on its
 * {@link AnalyzerType}.
 *
 * Not directly serializable, but the specification used to create this analyzer (retrievable from {@link #extractAnalyzerSpec() extractAnalyzerSpec})
 * is.
 *
 * Implementations of this interface are assumed to be effectively immutable.
 *
 * @param <T>
 *            The {@link AnalyzerType} of this analyzer.
 * @author mizitch
 */
public interface Analyzer<T extends AnalyzerType<T>> extends Describable {

	/**
	 * Returns the {@link AnalyzerType} of this analyzer. Must not be null
	 *
	 * @return The {@link AnalyzerType} of this analyzer. Must not be null.
	 */
	public T getAnalyzerType();

	/**
	 * Run this analyzer on the provided {@link Story}.
	 *
	 * @param story
	 *            The {@link Story} to run this analyzer on.
	 * @return The {@link AnalyzerResult}
	 */
	public AnalyzerResult<T> execute(Story story);

	/**
	 * Returns whether comment recording is suppressed for this analyzer.
	 *
	 * @return Whether comment recording is suppressed for this analyzer.
	 */
	public boolean isCommentRecordingSuppressed();

	/**
	 * Retrieves the {@link AnalyzerSpec} used to create this analyzer. Must not be null
	 *
	 * @return The {@link AnalyzerSpec} used to create this analyzer. Must not be null
	 */
	public AnalyzerSpec<T> extractAnalyzerSpec();
}
