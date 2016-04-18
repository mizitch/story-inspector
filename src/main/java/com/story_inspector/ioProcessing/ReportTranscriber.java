package com.story_inspector.ioProcessing;

import java.io.OutputStream;
import java.util.Set;

import com.story_inspector.analysis.reports.Report;
import com.story_inspector.progress.ProgressMonitor;
import com.story_inspector.progress.TaskCanceledException;

/**
 * Writes a completed {@link Report}. This includes both the commented original document and any summary data in the report.
 *
 * @author mizitch
 *
 */
public interface ReportTranscriber {

	/**
	 * Write the provided {@link Report} to the provided {@link OutputStream}.
	 *
	 * @param report
	 *            The report to write
	 * @param extractedDocument
	 *            The original {@link ExtractedDocument}. May modify this object.
	 * @param destination
	 *            The {@link OutputStream} to write to
	 * @param progressMonitor
	 *            The {@link ProgressMonitor} that will be updated as the report is transcribed
	 * @throws StoryIOException
	 *             If there is an IO error while transcribing the report.
	 * @throws TaskCanceledException
	 *             If the user cancels the task while transcribing the report.
	 */
	public void transcribeReport(final Report report, final ExtractedDocument extractedDocument, final OutputStream destination,
			ProgressMonitor progressMonitor) throws StoryIOException, TaskCanceledException;

	public Set<String> getSupportedFileTypes();
}
