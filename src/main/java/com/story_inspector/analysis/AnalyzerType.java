package com.story_inspector.analysis;

import java.util.List;

/**
 * An {@link AnalyzerType} validates and converts a list of parameter values into an {@link Analyzer}. It exposes a list of {@link ParameterSpec} that
 * specify the values it expects.
 *
 * When a non-backwards compatible change is made to an {@link AnalyzerType} (such as removing a parameter or adding a required parameter), one should
 * create a new {@link AnalyzerType} with the same id and a new version number.
 *
 * {@link AnalyzerType}s should be instantiated as singletons. The application assumes that there is only one instance of a given {@link AnalyzerType}
 * in memory.
 *
 * @param <T>
 *            The {@link AnalyzerType} subclass.
 * @author mizitch
 */
public interface AnalyzerType<T extends AnalyzerType<T>> extends Describable {
	/**
	 * Returns the id of this {@link AnalyzerType}. Multiple versions of a given {@link AnalyzerType} will be represented as multiple instances which
	 * share an id, but have a different version number.
	 *
	 * @return The id of this {@link AnalyzerType}
	 * @see {@link #getVersion()}
	 */
	public String getId();

	/**
	 * Returns the version number of this {@link AnalyzerType}. Multiple versions of a given {@link AnalyzerType} will be represented as multiple
	 * instances which share an id, but have a different version number.
	 *
	 * @return The version number of this {@link AnalyzerType}.
	 * @see {@link #getId()}
	 */
	public int getVersion();

	/**
	 * Returns the list of parameters this {@link AnalyzerType} needs to create an {@link Analyzer}.
	 *
	 * @return The list of parameters this {@link AnalyzerType} needs to create an {@link Analyzer}.
	 * @see {@link ParameterSpec}
	 */
	public List<ParameterSpec<?>> getParameterSpecs();

	/**
	 * Returns whether analyzers of this type produce {@link Comment}s.
	 *
	 * @return Whether analyzers of this type produce {@link Comment}s.
	 */
	public boolean producesComments();

	/**
	 * Try to create an {@link Analyzer} of this {@link AnalyzerType} using the provided {@link AnalyzerSpec}.
	 *
	 * @param analyzerSpec
	 *            The {@link AnalyzerSpec} that contains details on how to create this
	 * @return
	 */
	public AnalyzerCreationResult<T> tryCreateAnalyzer(AnalyzerSpec<T> analyzerSpec);
}
