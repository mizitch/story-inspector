package com.story_inspector.analysis;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.apache.commons.lang3.Validate;

/**
 * Provides a basic implementation of {@link AnalyzerType}.
 *
 * @author mizitch
 *
 * @param <T>
 *            The {@link AnalyzerType} subclass.
 */
public abstract class BaseAnalyzerType<T extends BaseAnalyzerType<T>> extends BaseDescribable implements AnalyzerType<T> {
	private final String id;
	private final int version;
	private final boolean producesComments;
	private final List<ParameterSpec<?>> parameterSpecs;

	/**
	 * Creates a new {@link BaseAnalyzerType}.
	 *
	 * @param name
	 *            The name for the new instance.
	 * @param description
	 *            The description for the new instance
	 * @param id
	 *            The id for the new instance. Cannot be null or blank
	 * @param version
	 *            The version for the new instance. Must be non-negative
	 * @param producesComments
	 *            Whether {@link Analyzer}s produced by this instance will produce comments.
	 * @param parameterSpecs
	 *            The specs for parameter values used to create new {@link Analyzer}s from the new instance.
	 */
	protected BaseAnalyzerType(final String name, final String description, final String id, final int version, final boolean producesComments,
			final List<ParameterSpec<?>> parameterSpecs) {
		super(name, description);
		this.id = id;
		this.version = version;
		this.producesComments = producesComments;
		this.parameterSpecs = parameterSpecs;
		Validate.notBlank(id);
		Validate.inclusiveBetween(1, Long.MAX_VALUE, version);
		Validate.noNullElements(this.parameterSpecs);
		Validate.isTrue(this.parameterSpecs.size() == this.parameterSpecs.stream().map(ParameterSpec::getId).distinct().count(),
				"Parameter specs must have unique ids");
	}

	@Override
	public int getVersion() {
		return this.version;
	}

	@Override
	public boolean producesComments() {
		return this.producesComments;
	}

	@Override
	public List<ParameterSpec<?>> getParameterSpecs() {
		return this.parameterSpecs;
	}

	@Override
	public String getId() {
		return this.id;
	}

	@Override
	public AnalyzerCreationResult<T> tryCreateAnalyzer(final AnalyzerSpec<T> analyzerSpec) {
		final ValidationResult globalResult = validateSpec(analyzerSpec);

		final Map<ParameterSpec<?>, ValidationResult> parameterResults = getParameterSpecs().stream().collect(
				Collectors.toMap(Function.identity(), spec -> validateParameter(spec, analyzerSpec.getAnalyzerParameterValues().get(spec.getId()))));

		if (globalResult.isValid() && parameterResults.values().stream().allMatch(ValidationResult::isValid)) {
			return new AnalyzerCreationResult<T>(createAnalyzer(analyzerSpec), globalResult, parameterResults);
		} else {
			return new AnalyzerCreationResult<T>(analyzerSpec.getAnalyzerType(), globalResult, parameterResults);
		}

	}

	@Override
	public boolean equals(final Object other) {
		return this == other;
	}

	@Override
	public int hashCode() {
		return System.identityHashCode(this);
	}

	/**
	 * Validates that the spec is for the correct analyzer type and does not contain extra parameters.
	 *
	 * @param analyzerSpec
	 * @return
	 */
	private ValidationResult validateSpec(final AnalyzerSpec<T> analyzerSpec) {
		// Check that spec has correct analyzer type
		final List<String> validationErrors = new ArrayList<>();
		if (analyzerSpec.getAnalyzerType() != this)
			validationErrors.add("Provided spec is for analyzer type " + analyzerSpec.getAnalyzerType().getId() + " not " + getId());

		// Check that analyzer spec doesn't have any extra parameters
		final Set<String> parameterSpecIds = getParameterSpecs().stream().map(ParameterSpec::getId).collect(Collectors.toSet());
		final Set<String> additionalParameters = new HashSet<>(analyzerSpec.getAnalyzerParameterValues().keySet());
		additionalParameters.removeIf(p -> parameterSpecIds.contains(p));

		if (!additionalParameters.isEmpty())
			validationErrors.add("Analyzer spec contains unknown parameters: " + additionalParameters);

		// Return appropriate result
		if (!validationErrors.isEmpty())
			return ValidationResult.invalidResult(validationErrors);
		else
			return ValidationResult.validResult();
	}

	/**
	 * Validates the provided parameter.
	 *
	 * @param spec
	 * @param value
	 * @return
	 */
	private <V> ValidationResult validateParameter(final ParameterSpec<V> spec, final Object value) {
		if (!spec.getParameterType().isInstance(value))
			return ValidationResult.invalidResult("Value " + value + " does not match expected type for parameter: " + spec.getName());
		else
			return spec.getParameterValidator().validateParameter(spec.getParameterType().cast(value));
	}

	/**
	 * Creates an analyzer using a pre-validated {@link AnalyzerSpec}. Must be overridden by subclasses.
	 *
	 * @param spec
	 *            The already validated {@link AnalyzerSpec}
	 * @return The result {@link Analyzer}
	 */
	protected abstract Analyzer<T> createAnalyzer(AnalyzerSpec<T> spec);
}
