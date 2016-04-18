package com.story_inspector.controllers.analyzerParameters;

import java.util.HashMap;
import java.util.Map;

import org.springframework.stereotype.Component;

import com.story_inspector.analysis.ParameterSpec;
import com.story_inspector.analysis.parameterTypes.DialogueSearchPattern;
import com.story_inspector.analysis.parameterTypes.StringSet;

import javafx.scene.Node;

/**
 * Default implementation of {@link AnalyzerParameterControlFactory}.
 *
 * @author mizitch
 *
 */
@Component
public class AnalyzerParameterControlFactoryImpl implements AnalyzerParameterControlFactory {

	@FunctionalInterface
	private interface ControlGenerator<T, V extends Node> {
		public V generate(final ParameterSpec<T> spec);
	}

	private final Map<Class<?>, ControlGenerator<?, ?>> controlGeneratorMap = new HashMap<>();

	public AnalyzerParameterControlFactoryImpl() {
		setupGeneratorMap();
	}

	private <T, V extends Node & AnalyzerParameterControl<T>> void registerGenerator(final Class<T> parameterType,
			final ControlGenerator<T, V> generator) {
		this.controlGeneratorMap.put(parameterType, generator);
	}

	/**
	 * Setup generators for {@link AnalyzerParameterControl}s for types we currently support.
	 */
	private void setupGeneratorMap() {
		// This is fun, a weird compilation bug related to lambdas and parameterized types means this code doesn't work as lambdas :( Interesting
		// thing is that the eclipse compiler seems to be able to handle it, but command line won't, even though (as far as I can tell) my local javac
		// is up to date...
		registerGenerator(StringSet.class, spec -> new StringSetControl(spec));
		registerGenerator(Boolean.class, spec -> new BooleanControl(spec));
		registerGenerator(DialogueSearchPattern.class, spec -> new DescribableEnumControl<DialogueSearchPattern>(spec));
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T, V extends Node & AnalyzerParameterControl<T>> V createAnalyzerParameterControl(final ParameterSpec<T> spec) {
		if (!this.controlGeneratorMap.containsKey(spec.getParameterType()))
			throw new IllegalArgumentException("No support for parameter spec of type " + spec.getParameterType());

		return ((ControlGenerator<T, V>) this.controlGeneratorMap.get(spec.getParameterType())).generate(spec);
	}
}
