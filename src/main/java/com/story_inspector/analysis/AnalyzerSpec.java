package com.story_inspector.analysis;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang.Validate;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.EqualsBuilder;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * A specification for an {@link Analyzer} that is easily serializable. Used by an {@link AnalyzerType} to create a new {@link Analyzer}.
 *
 * This class is designed to be immutable.
 *
 * @author mizitch
 *
 * @param <T>
 *            The {@link AnalyzerType} of the {@link Analyzer} produced by this spec.
 */
public class AnalyzerSpec<T extends AnalyzerType<T>> extends BaseDescribable {
	private final T analyzerType;
	private final Map<String, Object> analyzerParameterValues;
	private final boolean commentRecordingSuppressed;

	/**
	 * Creates a new instance. Requires all data necessary to create an {@link Analyzer}.
	 *
	 * @param name
	 *            Name of the {@link Analyzer} this specifies. See {@link Analyzer#getName()}.
	 * @param description
	 *            Description of {@link Analyzer} this specifies. See {@link Analyzer#getDescription()}.
	 * @param analyzerType
	 *            {@link AnalyzerType} of {@link Analyzer} this specifies. See {@link Analyzer#getAnalyzerType()}.
	 * @param commentRecordingSuppressed
	 *            Whether the {@link Analyzer} this specifies will suppress comment recording. See {@link Analyzer#isCommentRecordingSuppressed()}.
	 * @param analyzerParameterValues
	 *            The parameter values used to create the {@link Analyzer} this specifies. See {@link AnalyzerType#getParameterSpecs()}.
	 */
	@JsonCreator
	public AnalyzerSpec(@JsonProperty("name") final String name, @JsonProperty("description") final String description,
			@JsonProperty("analyzerType") final T analyzerType, @JsonProperty("commentRecordingSuppressed") final boolean commentRecordingSuppressed,
			@JsonProperty("analyzerParameterValues") final Map<String, Object> analyzerParameterValues) {
		super(name, description);
		Validate.notNull(analyzerType);
		Validate.notNull(analyzerParameterValues);

		this.analyzerType = analyzerType;
		this.commentRecordingSuppressed = commentRecordingSuppressed;
		this.analyzerParameterValues = new HashMap<>(analyzerParameterValues);
	}

	/**
	 * Returns {@link AnalyzerType} of {@link Analyzer} this specifies. Guaranteed to not be null.
	 *
	 * @return {@link AnalyzerType} of {@link Analyzer} this specifies. Guaranteed to not be null.
	 * @see {@link Analyzer#getAnalyzerType()}
	 */
	public T getAnalyzerType() {
		return this.analyzerType;
	}

	/**
	 * Returns whether the {@link Analyzer} this specifies will suppress comment recording.
	 *
	 * @return Whether the {@link Analyzer} this specifies will suppress comment recording.
	 * @see {@link Analyzer#isCommentRecordingSuppressed()}
	 */
	public boolean isCommentRecordingSuppressed() {
		return this.commentRecordingSuppressed;
	}

	/**
	 * Returns the parameter values used to create the {@link Analyzer} this specifies. Guaranteed to not be null.
	 *
	 * @return The parameter values used to create the {@link Analyzer} this specifies. Guaranteed to not be null.
	 * @see {@link ParameterSpec}
	 */
	public Map<String, Object> getAnalyzerParameterValues() {
		return Collections.unmodifiableMap(this.analyzerParameterValues);
	}

	/**
	 * Returns the value this spec associates with the provided parameter.
	 *
	 * @param spec
	 *            The parameter to look up the value for.
	 * @return The value associated with the provided parameter.
	 */
	public <V> V getParameterValue(final ParameterSpec<V> spec) {
		return spec.getParameterType().cast(this.analyzerParameterValues.get(spec.getId()));
	}

	@Override
	public boolean equals(final Object other) {
		if (!(other instanceof AnalyzerSpec))
			return false;
		final AnalyzerSpec<?> otherSpec = (AnalyzerSpec<?>) other;

		// Since AnalyzerTypes are required to be singletons, checking for equality here should be fine, even if they don't have a custom equals
		// method
		return new EqualsBuilder().appendSuper(super.equals(otherSpec)).append(this.analyzerType, otherSpec.analyzerType)
				.append(this.analyzerParameterValues, otherSpec.analyzerParameterValues)
				.append(this.commentRecordingSuppressed, otherSpec.commentRecordingSuppressed).isEquals();
	}

	@Override
	public int hashCode() {
		// Since AnalyzerTypes are required to be singletons, using their hashcode here should be reliable.
		return new HashCodeBuilder().appendSuper(super.hashCode()).append(this.analyzerType).append(this.analyzerParameterValues)
				.append(this.commentRecordingSuppressed).toHashCode();
	}
}
