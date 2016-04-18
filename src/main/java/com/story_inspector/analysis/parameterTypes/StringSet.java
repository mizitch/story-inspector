package com.story_inspector.analysis.parameterTypes;

import java.util.Collection;
import java.util.HashSet;

import com.story_inspector.analysis.AnalyzerType;

/**
 * A set of strings. This type is created for parameters for {@link AnalyzerType}s so that the Story Inspector UI can automatically determine the
 * appropriate control to use.
 *
 * @author mizitch
 *
 */
public class StringSet extends HashSet<String> {
	private static final long serialVersionUID = 1L;

	public StringSet() {
		super();
	}

	public StringSet(final Collection<String> stringCollection) {
		super(stringCollection);
	}
}
