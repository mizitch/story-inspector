package com.story_inspector.analysis;

import org.apache.commons.lang.Validate;
import org.apache.commons.lang3.builder.ToStringBuilder;

/**
 * Provides details of a parameter required by an {@link AnalyzerType} to create an {@link Analyzer}. Includes a name and description, the type of the
 * parameter, a default value (if there is one) and any validation rules for the parameter.
 *
 * @author mizitch
 *
 * @param <T>
 *            The type of the parameter.
 */
public class ParameterSpec<T> extends BaseDescribable {
	private final String id;
	private final Class<T> parameterType;
	private final T defaultValue;
	private final ParameterValidator<T> parameterValidator;

	/**
	 * Creates a new instance.
	 *
	 * @param id
	 *            An id for this parameter that is unique within the {@link AnalyzerType}.
	 * @param parameterName
	 *            The name of the parameter.
	 * @param parameterDescription
	 *            A description for the parameter.
	 * @param parameterType
	 *            The type of the parameter.
	 * @param parameterValidator
	 *            The validator for the parameter.
	 * @param defaultValue
	 *            A default value for this parameter.
	 */
	public ParameterSpec(final String id, final String parameterName, final String parameterDescription, final Class<T> parameterType,
			final ParameterValidator<T> parameterValidator, final T defaultValue) {
		super(parameterName, parameterDescription);

		Validate.notEmpty(id);
		Validate.notNull(parameterType);
		Validate.notNull(parameterValidator);

		this.id = id;
		this.parameterType = parameterType;
		this.parameterValidator = parameterValidator;
		this.defaultValue = defaultValue;
	}

	/**
	 * Creates a new instance.
	 *
	 * @param id
	 *            An id for this parameter that is unique within the {@link AnalyzerType}.
	 * @param parameterName
	 *            The name of the parameter.
	 * @param parameterDescription
	 *            A description for the parameter.
	 * @param parameterType
	 *            The type of the parameter.
	 * @param parameterValidator
	 *            The validator for the parameter.
	 */
	public ParameterSpec(final String id, final String parameterName, final String parameterDescription, final Class<T> parameterType,
			final ParameterValidator<T> parameterValidator) {
		this(id, parameterName, parameterDescription, parameterType, parameterValidator, null);
	}

	/**
	 * Creates a new instance.
	 *
	 * @param id
	 *            An id for this parameter that is unique within the {@link AnalyzerType}.
	 * @param parameterName
	 *            The name of the parameter.
	 * @param parameterDescription
	 *            A description for the parameter.
	 * @param parameterType
	 *            The type of the parameter.
	 */
	public ParameterSpec(final String id, final String parameterName, final String parameterDescription, final Class<T> parameterType) {
		this(id, parameterName, parameterDescription, parameterType, ParameterValidator.alwaysValid());
	}

	/**
	 * Gets the default value for this parameter.
	 *
	 * @return The default value for this parameter.
	 */
	public T getDefaultValue() {
		return this.defaultValue;
	}

	/**
	 * Returns whether this parameter has a default value.
	 *
	 * @return Whether this parameter has a default value.
	 */
	public boolean hasDefaultValue() {
		return this.defaultValue != null;
	}

	/**
	 * Returns the type of this parameter.
	 *
	 * @return The type of this parameter.
	 */
	public Class<T> getParameterType() {
		return this.parameterType;
	}

	/**
	 * Returns the validator for this parameter.
	 *
	 * @return The validator for this parameter.
	 */
	public ParameterValidator<T> getParameterValidator() {
		return this.parameterValidator;
	}

	/**
	 * Returns the id of this parameter. Unique within the {@link AnalyzerType}.
	 *
	 * @return The id of this parameter. Unique within the {@link AnalyzerType}.
	 */
	public String getId() {
		return this.id;
	}

	@Override
	public boolean equals(final Object other) {
		return this == other;
	}

	@Override
	public int hashCode() {
		return System.identityHashCode(this);
	}

	@Override
	public String toString() {
		return new ToStringBuilder(this).append("id", this.id).append("parameterType", this.parameterType).append("defaultValue", this.defaultValue)
				.toString();
	}
}
