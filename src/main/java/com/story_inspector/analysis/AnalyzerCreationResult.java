package com.story_inspector.analysis;

import java.util.Collections;
import java.util.HashSet;
import java.util.Map;

import org.apache.commons.lang.Validate;
import org.apache.commons.lang3.builder.ToStringBuilder;

/**
 * Result of attempting to create an {@link Analyzer}.
 *
 * @author mizitch
 *
 * @param <T>
 *            The type of {@link Analyzer}.
 */
public class AnalyzerCreationResult<T extends AnalyzerType<T>> {

	private final ValidationResult globalResult;
	private final Map<ParameterSpec<?>, ValidationResult> parameterResults;
	private final Analyzer<T> analyzer;

	private AnalyzerCreationResult(final T analyzerType, final Analyzer<T> analyzer, final ValidationResult globalResult,
			final Map<ParameterSpec<?>, ValidationResult> parameterResults) {
		super();
		Validate.notNull(globalResult);
		Validate.notNull(parameterResults);
		Validate.isTrue(parameterResults.keySet().equals(new HashSet<>(analyzerType.getParameterSpecs())),
				"Parameter results must contain exactly the analyzer type's declared parameters");

		final boolean allValid = globalResult.isValid() && parameterResults.values().stream().allMatch(ValidationResult::isValid);
		if (analyzer == null) {
			Validate.isTrue(!allValid, "If no analyzer is provided, must provide at least one invalid validation result");
		} else {
			Validate.isTrue(allValid, "If an analyzer is provided, cannot provide any invalid validation results");
		}

		this.globalResult = globalResult;
		this.parameterResults = parameterResults;
		this.analyzer = analyzer;
	}

	/**
	 * Creates a new instance. If creation was successful, {@code analyzer} must be non-null and all {@link ValidationResult}s must be valid. If it
	 * was not successful, {@code analyzer} must be null and at least one {@ValidationResult} must be invalid.
	 *
	 * @param analyzer
	 *            The analyzer created, if creation was not successful, should be null.
	 * @param globalResult
	 *            The result of any global validation performed on the creation attempt.
	 * @param parameterResults
	 *            The results of validation of the analyzer parameters. Must have a key-value pair for every {@link ParameterSpec} associated with the
	 *            {@link Analyzer}'s {@link AnalyzerType}.
	 */
	public AnalyzerCreationResult(final Analyzer<T> analyzer, final ValidationResult globalResult,
			final Map<ParameterSpec<?>, ValidationResult> parameterResults) {
		this(analyzer.getAnalyzerType(), analyzer, globalResult, parameterResults);
	}

	/**
	 * Creates a new instance. If creation was successful, {@code analyzer} must be non-null and all {@link ValidationResult}s must be valid. If it
	 * was not successful, {@code analyzer} must be null and at least one {@ValidationResult} must be invalid.
	 *
	 * @param analyzer
	 *            The analyzer created, if creation was not successful, should be null.
	 * @param globalResult
	 *            The result of any global validation performed on the creation attempt.
	 * @param parameterResults
	 *            The results of validation of the analyzer parameters. Must have a key-value pair for every {@link ParameterSpec} associated with the
	 *            {@link Analyzer}'s {@link AnalyzerType}.
	 */
	public AnalyzerCreationResult(final T analyzerType, final ValidationResult globalResult,
			final Map<ParameterSpec<?>, ValidationResult> parameterResults) {
		this(analyzerType, null, globalResult, parameterResults);
	}

	/**
	 * Returns the result of any global validation done on the {@link Analyzer} creation attempt.
	 *
	 * @return The result of any global validation done on the {@link Analyzer} creation attempt.
	 */
	public ValidationResult getGlobalResult() {
		return this.globalResult;
	}

	/**
	 * Returns the results of parameter validations.
	 *
	 * @return The results of parameter validations.
	 */
	public Map<ParameterSpec<?>, ValidationResult> getParameterResults() {
		return Collections.unmodifiableMap(this.parameterResults);
	}

	/**
	 * Returns the created {@link Analyzer}. Null if the attempt was unsuccessful.
	 *
	 * @return The created {@link Analyzer}. Null if the attempt was unsuccessful.
	 */
	public Analyzer<T> getAnalyzer() {
		return this.analyzer;
	}

	/**
	 * Whether the creation attempt was successful.
	 *
	 * @return Whether the creation attempt was successful.
	 */
	public boolean wasSuccessful() {
		return this.analyzer != null;
	}

	@Override
	public String toString() {
		return new ToStringBuilder(this).append("globalResult", this.globalResult).append("parameterResults", this.parameterResults)
				.append("analyzer", this.analyzer).build();
	}
}
