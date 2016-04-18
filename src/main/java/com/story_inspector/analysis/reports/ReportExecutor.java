package com.story_inspector.analysis.reports;

import com.story_inspector.progress.ProgressMonitor;
import com.story_inspector.progress.TaskCanceledException;
import com.story_inspector.story.Story;

/**
 * Executes {@link ReportSpec}s on {@link Story}s, returning the generated {@link Report}s.
 *
 * @author mizitch
 * @see {@link ReportSpec}
 * @see {@link Story}
 * @see {@link Report}
 */
public interface ReportExecutor {

	/**
	 * Executes the provided {@link ReportSpec} on the provided {@link Story}, returning the generated {@link Report}.
	 *
	 * @param spec
	 *            The specification used to generate a report.
	 * @param story
	 *            The story on which to generate a report.
	 * @param progressMonitor
	 *            The progress
	 * @return
	 * @throws TaskCanceledException
	 */
	public Report execute(ReportSpec spec, Story story, final ProgressMonitor progressMonitor) throws TaskCanceledException;
}
