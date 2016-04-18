package com.story_inspector.controllers.analyzerRegistry;

import java.util.List;

import com.story_inspector.analysis.Analyzer;

/**
 * Registry for default analyzers pre-programmed into Story Inspector.
 *
 * @author mizitch
 *
 */
public interface DefaultAnalyzerRegistry {

	/**
	 * Returns a list of all defined default {@link Analyzer}s.
	 *
	 * @return A list of all defined default {@link Analyzer}s.
	 */
	public List<Analyzer<?>> getAllDefaultAnalyzers();
}
