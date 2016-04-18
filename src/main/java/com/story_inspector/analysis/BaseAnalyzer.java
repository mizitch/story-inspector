package com.story_inspector.analysis;

import java.util.Map;

import org.apache.commons.lang3.builder.HashCodeBuilder;

/**
 * Provides a basic implementation of {@link Analyzer}
 *
 * @author mizitch
 *
 * @param <T>
 *            The {@link AnalyzerType} of this {@link Analyzer}.
 */
public abstract class BaseAnalyzer<T extends AnalyzerType<T>> extends BaseDescribable implements Analyzer<T> {
	private final T analyzerType;
	private final boolean commentRecordingSuppressed;

	/**
	 * Creates a new {@link BaseAnalyzer} using the provided {@link AnalyzerSpec}.
	 *
	 * @param spec
	 *            The {@link AnalyzerSpec} used to instantiate this {@link Analyzer}.
	 */
	public BaseAnalyzer(final AnalyzerSpec<T> spec) {
		super(spec.getName(), spec.getDescription());

		this.analyzerType = spec.getAnalyzerType();
		this.commentRecordingSuppressed = spec.isCommentRecordingSuppressed();
	}

	@Override
	public T getAnalyzerType() {
		return this.analyzerType;
	}

	@Override
	public boolean isCommentRecordingSuppressed() {
		return this.commentRecordingSuppressed;
	}

	@Override
	public AnalyzerSpec<T> extractAnalyzerSpec() {
		final Map<String, Object> parameterValues = retrieveParameterValues();
		return new AnalyzerSpec<T>(getName(), getDescription(), this.analyzerType, this.commentRecordingSuppressed, parameterValues);
	}

	/**
	 * Returns the parameter values used to create this {@link Analyzer}. Must be implemented by subclasses of {@link BaseAnalyzer} to support the
	 * recreation of the original {@link AnalyzerSpec} used to create this {@link Analyzer}.
	 *
	 * @return The parameter values used to create this {@link Analyzer}. Must be implemented by subclasses of {@link BaseAnalyzer} to support the
	 *         recreation of the original {@link AnalyzerSpec} used to create this {@link Analyzer}.
	 */
	protected abstract Map<String, Object> retrieveParameterValues();

	/**
	 * Checks for equality by checking for equality of associated {@link AnalyzerSpec}s.
	 *
	 * @param other
	 *            The object with which to compare.
	 */
	@Override
	public boolean equals(final Object other) {
		if (other instanceof BaseAnalyzer<?>) {
			final BaseAnalyzer<?> otherAnalyzer = (BaseAnalyzer<?>) other;
			return this.extractAnalyzerSpec().equals(otherAnalyzer.extractAnalyzerSpec());
		} else {
			return false;
		}
	}

	/**
	 * Determines a hash code using the hash code of this {@link Analyzer}'s {@link AnalyzerSpec}.
	 *
	 * @return A hash code based on the hash code of the associated {@link AnalyzerSpec}.
	 */
	@Override
	public int hashCode() {
		return new HashCodeBuilder().append(extractAnalyzerSpec()).toHashCode();
	}
}
