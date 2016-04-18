package com.story_inspector.progress;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;

/**
 * Object which handles reporting progress to user during long-running asynchronous task. Also supports breaking execution if the user cancels the
 * task (by throwing a {@link TaskCanceledException}.
 *
 * @author mizitch
 *
 */
@FunctionalInterface
public interface ProgressMonitor {

	/**
	 * Inform the user of task progress
	 *
	 * @param progressPercentage
	 *            The current task completion percentage. Must be between 0 and 1 inclusive
	 * @param progressMessage
	 *            The current progress message
	 * @throws TaskCanceledException
	 *             If the user has canceled the task
	 */
	void reportProgress(float progressPercentage, String progressMessage) throws TaskCanceledException;

	/**
	 * Creates a sub-monitor for this {@link ProgressMonitor} given a sub-range and a title message. When progress is reported it will translate the
	 * provided progress percentage to lie within the sub-range and the provided message to be prefixed with the title.
	 *
	 * For example, a submonitor with a range of 0.5 to 0.6 and a title of "Reticulating Splines", if supplied with a progress report of 0.5 and
	 * "Some splines are stubborn" will report the following progress to its parent monitor.
	 *
	 * Percentage: 0.55 Message: "Reticulating Splines: Some splines are stubborn"
	 *
	 * @param start
	 *            Start of the sub-range. Must be between 0 and 1 inclusive and less than end
	 * @param end
	 *            End of the sub-range. Must be between 0 and 1 inclusive and greater than start
	 * @param title
	 *            The title to prefix progress messages with.
	 * @return The sub monitor
	 */
	default ProgressMonitor subMonitor(final float start, final float end, final String title) {
		Validate.inclusiveBetween(0, 1, start);
		Validate.inclusiveBetween(0, 1, end);
		Validate.isTrue(start < end, "start must be less than end");
		Validate.isTrue(!StringUtils.isBlank(title), "title must not be blank");

		return (progressPercentage, progressMessage) -> {
			Validate.inclusiveBetween(0, 1, progressPercentage);
			Validate.notBlank(progressMessage);

			final float parentProgressPercentage = (end - start) * progressPercentage + start;
			final String parentProgressString = title + ": " + progressMessage;
			reportProgress(parentProgressPercentage, parentProgressString);
		};
	}
}
