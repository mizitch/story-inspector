package com.story_inspector.controllers.analyzerRegistry;

import java.util.Collection;

import com.story_inspector.analysis.Analyzer;

import javafx.collections.ObservableMap;

/**
 * Registry for user-created {@link Analyzer}s.
 *
 * @author mizitch
 *
 */
public interface CustomAnalyzerRegistry {

	/**
	 * Returns a map of unique id to custom {@link Analyzer}s.
	 *
	 * @return A map of unique id to custom {@link Analyzer}s.
	 */
	public ObservableMap<String, Analyzer<?>> getAllCustomAnalyzers();

	/**
	 * Deletes custom {@link Analyzer} with the provided id.
	 *
	 * @param analyzerId
	 */
	public void deleteCustomAnalyzer(String analyzerId);

	/**
	 * Deletes all custom {@link Analyzer}s with the provided ids.
	 *
	 * @param analyzerIds
	 *            Ids of {@link Analyzer}s to delete.
	 */
	public default void deleteCustomAnalyzers(final Collection<String> analyzerIds) {
		for (final String id : analyzerIds)
			deleteCustomAnalyzer(id);
	}

	/**
	 * Add the provided {@link Analyzer}s to this registry.
	 *
	 * @param analyzers
	 *            {@link Analyzer}s to add.
	 */
	public default void addCustomAnalyzers(final Collection<Analyzer<?>> analyzers) {
		for (final Analyzer<?> analyzer : analyzers)
			addCustomAnalyzer(analyzer);

	}

	/**
	 * Add the provided {@link Analyzer} to this registry.
	 *
	 * @param analyzer
	 *            {@link Analyzer} to add.
	 */
	public void addCustomAnalyzer(Analyzer<?> analyzer);

	/**
	 * Sets the custom {@link Analyzer} with the provided id to the provided new value.
	 *
	 * @param analyzerId
	 *            Id of {@link Analyzer} to set.
	 * @param newAnalyzer
	 *            New value for custom {@link Analyzer}.
	 */
	public void setCustomAnalyzer(String analyzerId, Analyzer<?> newAnalyzer);
}
