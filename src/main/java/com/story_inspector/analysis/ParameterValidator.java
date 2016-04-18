package com.story_inspector.analysis;

import java.util.Arrays;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import org.apache.commons.lang.Validate;

/**
 * Validates a parameter value for an {@link AnalyzerType}.
 *
 * @author mizitch
 *
 * @param <T>
 *            The type of the parameter.
 */
@FunctionalInterface
public interface ParameterValidator<T> {

	/**
	 * Validate the provided parameter.
	 *
	 * @param param
	 *            The provided parameter
	 * @return The result of the validation.
	 */
	public ValidationResult validateParameter(T param);

	/**
	 * Returns a validator that always says the parameter value is valid.
	 *
	 * @return A validator that always says the parameter value is valid.
	 */
	public static <T> ParameterValidator<T> alwaysValid() {
		return p -> ValidationResult.validResult();
	}

	/**
	 * Returns a validator that verifies the parameter is not null.
	 *
	 * @param parameterName
	 *            Parameter name, for validation message generation
	 * @return A validator that verifies the parameter is not null.
	 */
	public static <T> ParameterValidator<T> notNull(final String parameterName) {
		return p -> p != null ? ValidationResult.validResult() : ValidationResult.invalidResult(parameterName + " must be specified");
	}

	/**
	 * Creates a validator using the provided predicate and error. If the predicate does not apply, the validator will return an invalid result and
	 * the provided error string.
	 *
	 * @param predicate
	 *            The predicate to use to validate the parameter value.
	 * @param errorString
	 *            The error string to use if the parameter value is not valid.
	 * @return
	 */
	public static <T> ParameterValidator<T> createValidator(final Predicate<? super T> predicate, final String errorString) {
		return v -> {
			if (predicate.test(v))
				return ValidationResult.validResult();
			else
				return ValidationResult.invalidResult(errorString);
		};
	}

	/**
	 * Concatenates multiple validators into a single validator. If any validator returns invalid, the concatenated validator will return invalid.
	 *
	 * @param validators
	 *            The validators to concatenate.
	 * @return The concatenated validator.
	 */
	@SafeVarargs
	public static <T> ParameterValidator<T> concatenateValidators(final ParameterValidator<T>... validators) {
		Validate.notEmpty(validators);
		return p -> {
			final List<ValidationResult> results = Arrays.stream(validators).map(v -> v.validateParameter(p)).collect(Collectors.toList());
			final boolean isValid = results.stream().allMatch(ValidationResult::isValid);
			if (isValid)
				return ValidationResult.validResult();
			else
				return ValidationResult.invalidResult(results.stream().flatMap(r -> r.getValidationErrors().stream()).collect(Collectors.toList()));
		};

	}
}
