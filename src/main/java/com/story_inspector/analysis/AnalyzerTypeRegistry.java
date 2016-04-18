package com.story_inspector.analysis;

import java.util.Set;

/**
 * Provides access to {@link AnalyzerType} singletons.
 *
 * @author mizitch
 *
 */
public interface AnalyzerTypeRegistry {

	/**
	 * Gets the newest versions of all {@link AnalyzerType}s.
	 *
	 * @return
	 */
	public Set<AnalyzerType<?>> getCurrentAnalyzerTypes();

	/**
	 * Gets the most current version of the {@link AnalyzerType} with the provided id.
	 *
	 * @param analyzerTypeId
	 *            The {@link AnalyzerType} id.
	 * @return The most current version of the {@link AnalyzerType} with the provided id.
	 * @see {@link AnalyzerType#getId()}
	 */
	public AnalyzerType<?> getAnalyzerTypeById(String analyzerTypeId);

	/**
	 * Gets the {@link AnalyzerType} with the provided {@code id} and {@code version}
	 *
	 * @param id
	 *            The id of the {@link AnalyzerType} to look up.
	 * @param version
	 *            The version of the {@link AnalyzerType} to look up.
	 * @return The {@link AnalyzerType} associated with the provided {@code id} and {@code version}
	 */
	public AnalyzerType<?> getAnalyzerTypeByIdAndVersion(String id, int version);
}
