package com.story_inspector.progress;

import org.apache.commons.lang3.Validate;

/**
 * {@link ProgressMonitor} that provides utility method for reporting progress through a long text.
 *
 * @author mizitch
 *
 */
public class TextProcessingProgressMonitor implements ProgressMonitor {
	private final int textLength;
	private final ProgressMonitor delegate;

	/**
	 * Creates a new instance.
	 *
	 * @param parent
	 *            {@link ProgressMonitor} to delegate updates to.
	 * @param textLength
	 *            The total text length
	 */
	public TextProcessingProgressMonitor(final ProgressMonitor parent, final int textLength) {
		Validate.notNull(parent);
		Validate.inclusiveBetween(0, Long.MAX_VALUE, textLength);
		this.delegate = parent;
		this.textLength = textLength;
	}

	/**
	 * Reports progress through the text.
	 *
	 * Determines the percentage based on the text length and current text position. Displays the message as a fraction followed by the unit
	 * "characters." For example, if the current position is 10 and the text length is 20, the message passed will be: "10 / 20 characters"
	 *
	 * @param currentTextPosition
	 *            The current text position
	 * @throws TaskCanceledException
	 *             If the user cancels the currently running text
	 */
	public void reportProgress(final int currentTextPosition) throws TaskCanceledException {
		Validate.inclusiveBetween(0, this.textLength - 1, currentTextPosition);
		this.delegate.reportProgress((1.0f * currentTextPosition) / (1.0f * this.textLength),
				"" + currentTextPosition + " / " + this.textLength + " characters");
	}

	@Override
	public void reportProgress(final float progressPercentage, final String progressMessage) throws TaskCanceledException {
		this.delegate.reportProgress(progressPercentage, progressMessage);
	}
}
