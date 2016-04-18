package com.story_inspector.controllers.misc;

import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.OutputStream;

import org.apache.commons.lang3.Validate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.story_inspector.analysis.reports.Report;
import com.story_inspector.analysis.reports.ReportExecutor;
import com.story_inspector.analysis.reports.ReportSpec;
import com.story_inspector.ioProcessing.DocumentExtractor;
import com.story_inspector.ioProcessing.ExtractedDocument;
import com.story_inspector.ioProcessing.ReportTranscriber;
import com.story_inspector.ioProcessing.StoryIOException;
import com.story_inspector.ioProcessing.StoryParser;
import com.story_inspector.progress.ProgressMonitor;
import com.story_inspector.progress.TaskCanceledException;
import com.story_inspector.story.Story;

import javafx.concurrent.Task;

/**
 * Task that executes a provided report spec on a provided story using a provided set of IO modules and a report executor. Set up as a JavaFX task to
 * be run on a separate thread (not the application thread).
 *
 * @author mizitch
 *
 */
public class ReportExecutionTask extends Task<Report> {
	private static final Logger log = LoggerFactory.getLogger(ReportExecutionTask.class);

	private final DocumentExtractor extractor;
	private final StoryParser parser;
	private final ReportExecutor reportExecutor;
	private final ReportTranscriber transcriber;
	private final ReportSpec reportSpec;
	private final String storyName;
	private final InputStream storyInputStream;
	private final OutputStream reportOutputStream;

	/**
	 * {@link ProgressMonitor} to pass into different components that execute the report. If they attempt to update the progress and this task has
	 * been canceled, it informs them by throwing a {@link TaskCanceledException}.
	 */
	private final ProgressMonitor monitor = (percentage, message) -> {
		Validate.inclusiveBetween(0, 1, percentage);
		Validate.notBlank(message);

		if (this.isCancelled()) {
			throw new TaskCanceledException("User canceled report execution");
		} else {
			this.updateProgress(percentage, 1.0);
			this.updateMessage(message);
		}
	};

	/**
	 * Creates a new instance.
	 *
	 * @param reportSpec
	 *            The {@link ReportSpec} to execute.
	 * @param extractor
	 *            The {@link DocumentExtractor} to use on the story file.
	 * @param parser
	 *            The {@link StoryParser} to use on the {@link ExtractedDocument}.
	 * @param reportExecutor
	 *            The {@link ReportExecutor} to use.
	 * @param transcriber
	 *            The {@link ReportTranscriber} to use to write to the report output file.
	 * @param storyInputFile
	 *            The file location of the story document
	 * @param reportOutputFile
	 *            The file location to write the report to.
	 */
	public ReportExecutionTask(final ReportSpec reportSpec, final DocumentExtractor extractor, final StoryParser parser,
			final ReportExecutor reportExecutor, final ReportTranscriber transcriber, final String storyName, final InputStream storyInputStream,
			final OutputStream reportOutputStream) {
		super();
		this.reportSpec = reportSpec;
		this.extractor = extractor;
		this.parser = parser;
		this.reportExecutor = reportExecutor;
		this.transcriber = transcriber;
		this.storyName = storyName;
		this.storyInputStream = storyInputStream;
		this.reportOutputStream = reportOutputStream;

		Validate.notNull(this.reportSpec);
		Validate.notNull(this.extractor);
		Validate.notNull(this.parser);
		Validate.notNull(this.reportExecutor);
		Validate.notNull(this.transcriber);
		Validate.notNull(this.storyName);
		Validate.notNull(this.storyInputStream);
		Validate.notNull(this.reportOutputStream);
	}

	@Override
	protected Report call() throws StoryIOException, FileNotFoundException {
		try {
			log.info("Beginning execution of report %s", this.reportSpec.getName());

			log.info("Beginning document extraction");
			final ExtractedDocument extractedDoc = this.extractor.extractDocument(this.storyInputStream,
					this.monitor.subMonitor(0.0f, 0.05f, "Document Extraction"));

			log.info("Beginning story parsing");
			final Story story = this.parser.parseStory(extractedDoc, this.monitor.subMonitor(0.05f, 0.2f, "Story Parsing"));

			log.info("Beginning report execution with report spec %s", this.reportSpec.getName());
			final Report report = this.reportExecutor.execute(this.reportSpec, story, this.monitor.subMonitor(0.2f, 0.9f, "Report Execution"));

			log.info("Transcribing report");
			this.transcriber.transcribeReport(report, extractedDoc, this.reportOutputStream,
					this.monitor.subMonitor(0.9f, 1.0f, "Report Transcription"));

			log.info("Report execution complete");
			return report;
		} catch (final TaskCanceledException e) {
			log.info("User canceled report execution task", e);
			return null;
		}
	}

	/**
	 * Returns the {@link ReportSpec} this task is executing.
	 *
	 * @return the {@link ReportSpec} this task is executing.
	 */
	public ReportSpec getReportSpec() {
		return this.reportSpec;
	}

	/**
	 * Returns the name of the story this task is executing on.
	 *
	 * @return The name of the story this task is executing on.
	 */
	public String getStoryName() {
		return this.storyName;
	}

}
