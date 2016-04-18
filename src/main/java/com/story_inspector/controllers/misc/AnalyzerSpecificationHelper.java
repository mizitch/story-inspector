package com.story_inspector.controllers.misc;

import com.story_inspector.analysis.Analyzer;
import com.story_inspector.analysis.AnalyzerType;

/**
 * Allows users to create, clone or edit analyzers.
 *
 * @author mizitch
 *
 */
public interface AnalyzerSpecificationHelper {
	/**
	 * Create a new analyzer that is a copy of the provided analyzer and allow the user to edit this copy. Returns the new analyzer if this action is
	 * successful.
	 *
	 * @param existingAnalyzer
	 *            The analyzer to copy.
	 * @return The new analyzer, or null if the action is not successful.
	 */
	public <T extends AnalyzerType<T>> Analyzer<T> cloneAnalyzer(Analyzer<T> existingAnalyzer);

	/**
	 * Allows the user to edit an existing analyzer. Returns the edited version of the analyzer if the action is successful.
	 *
	 * @param existingAnalyzer
	 *            The analyzer to edit.
	 * @return The modified analyzer (a new object since {@link Analyzer}s are immutable), or null if the action is not successful.
	 */
	public <T extends AnalyzerType<T>> Analyzer<T> editAnalyzer(Analyzer<T> existingAnalyzer);

	/**
	 * Allows the user to create a new analyzer. Returns the new analyzer if the action is successful.
	 *
	 * @param analyzerType
	 *            The {@link AnalyzerType} of the new analyzer.
	 * @return The new analyzer, or null if the action is not successful.
	 */
	public <T extends AnalyzerType<T>> Analyzer<T> newAnalyzer(T analyzerType);
}
