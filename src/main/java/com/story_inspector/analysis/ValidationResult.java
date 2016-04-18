package com.story_inspector.analysis;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.apache.commons.lang.Validate;
import org.apache.commons.lang3.builder.ToStringBuilder;

/**
 * The result produced by a {@link ParameterValidator}.
 *
 * @author mizitch
 *
 */
public class ValidationResult {
	// This class is immutable, so make all valid results the same valid result.
	private static ValidationResult validResult = new ValidationResult(true, new ArrayList<String>());

	private final boolean valid;
	private final List<String> validationErrors;

	private ValidationResult(final boolean valid, final List<String> validationErrors) {
		this.valid = valid;

		Validate.noNullElements(validationErrors);
		this.validationErrors = validationErrors;
	}

	/**
	 * Returns whether the result is valid.
	 *
	 * @return Whether the result is valid.
	 */
	public boolean isValid() {
		return this.valid;
	}

	/**
	 * Returns the validation errors if there are any.
	 *
	 * @return The validation errors if there are any.
	 */
	public List<String> getValidationErrors() {
		return Collections.unmodifiableList(this.validationErrors);
	}

	/**
	 * Returns a valid result.
	 *
	 * @return A valid result.
	 */
	public static ValidationResult validResult() {
		return validResult;
	}

	/**
	 * Returns an invalid result with the provided errors.
	 *
	 * @param validationErrors
	 *            The errors for the new invalid result. Must be non-empty. Can't include nulls
	 * @return An invalid result with the provided errors.
	 */
	public static ValidationResult invalidResult(final List<String> validationErrors) {
		Validate.notEmpty(validationErrors);
		Validate.noNullElements(validationErrors);
		return new ValidationResult(false, new ArrayList<String>(validationErrors));
	}

	/**
	 * Returns an invalid result with the provided errors.
	 *
	 * @param validationErrors
	 *            The errors for the new invalid result. Must be non-empty. Can't include nulls
	 * @return An invalid result with the provided errors.
	 */
	public static ValidationResult invalidResult(final String... validationErrors) {
		return invalidResult(Arrays.asList(validationErrors));
	}

	@Override
	public String toString() {
		return new ToStringBuilder(this).append("valid", this.valid).append("validationErrors", this.validationErrors).toString();
	}
}
