package com.story_inspector.controllers.analyzerParameters;

import com.story_inspector.analysis.ParameterSpec;

import javafx.scene.Node;

/**
 * Creates {@link AnalyzerParameterControl}s for provided {@link ParameterSpec}s.
 *
 * @author mizitch
 *
 */
public interface AnalyzerParameterControlFactory {

	/**
	 * Creates and returns an {@link AnalyzerParameterControl} for the provided {@link ParameterSpec}.
	 *
	 * @param spec
	 *            The specification for the parameter that a control must be created for.
	 * @return An {@link AnalyzerParameterControl} for the provided {@link ParameterSpec}.
	 * @param <T>
	 *            The type of the parameter value.
	 * @param <V>
	 *            The type of the {@link AnalyzerParameterControl} returned. Guaranteed to be a subclass of {@link Node}.
	 */
	public <T, V extends Node & AnalyzerParameterControl<T>> V createAnalyzerParameterControl(ParameterSpec<T> spec);
}
