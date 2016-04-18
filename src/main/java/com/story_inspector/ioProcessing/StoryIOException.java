package com.story_inspector.ioProcessing;

import com.story_inspector.analysis.reports.Report;
import com.story_inspector.story.Story;

/**
 * Thrown when an error occurs while reading {@link Story}s or writing {@link Report}s.
 *
 * @author mizitch
 *
 */
public class StoryIOException extends Exception {

	private static final long serialVersionUID = 1L;

	/**
	 * Type of exception that occurred.
	 *
	 * @author mizitch
	 *
	 */
	public static enum ProcessingExceptionType {
		CORRUPT_DOCUMENT,
		INCLUDES_REVISION_DATA,
		UNKNOWN_ERROR
	};

	private final ProcessingExceptionType type;

	/**
	 * Creates a new instance.
	 *
	 * @param type
	 * @param message
	 */
	public StoryIOException(final ProcessingExceptionType type, final String message) {
		super(message);
		this.type = type;
	}

	/**
	 * Creates a new instance.
	 *
	 * @param type
	 * @param message
	 * @param cause
	 */
	public StoryIOException(final ProcessingExceptionType type, final String message, final Throwable cause) {
		super(message, cause);
		this.type = type;
	}

	/**
	 * Returns the type of exception that occurred.
	 *
	 * @return The type of exception that occurred.
	 */
	public ProcessingExceptionType getProcessingExceptionType() {
		return this.type;
	}
}
