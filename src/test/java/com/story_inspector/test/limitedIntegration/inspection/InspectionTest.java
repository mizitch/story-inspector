package com.story_inspector.test.limitedIntegration.inspection;

import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URISyntaxException;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

import org.junit.Before;
import org.springframework.beans.factory.annotation.Autowired;

import com.story_inspector.analysis.AnalyzerType;
import com.story_inspector.analysis.AnalyzerTypeRegistry;
import com.story_inspector.analysis.reports.Report;
import com.story_inspector.analysis.reports.ReportExecutor;
import com.story_inspector.analysis.reports.ReportSpec;
import com.story_inspector.ioProcessing.DocumentExtractor;
import com.story_inspector.ioProcessing.ExtractedDocument;
import com.story_inspector.ioProcessing.IoModuleRegistry;
import com.story_inspector.ioProcessing.ReportTranscriber;
import com.story_inspector.ioProcessing.StoryIOException;
import com.story_inspector.ioProcessing.StoryParser;
import com.story_inspector.progress.ProgressMonitor;
import com.story_inspector.progress.TaskCanceledException;
import com.story_inspector.story.Story;
import com.story_inspector.test.SpringBasedTest;

import javafx.embed.swing.JFXPanel;

/**
 * Superclass for tests which test end-to-end story inspection/report executions.
 *
 * @author mizitch
 *
 */
public abstract class InspectionTest extends SpringBasedTest {

	private static final String STORY_FILE_RESOURCE_SUBDIRECTORY = "/story_files/";

	@Autowired
	private IoModuleRegistry ioModuleRegistry;

	@Autowired
	private ReportExecutor reportExecutor;

	@Autowired
	private AnalyzerTypeRegistry analyzerTypeRegistry;

	private StoryParser storyParser;
	private ReportTranscriber reportTranscriber;
	private DocumentExtractor documentExtractor;

	@Before
	public void initializeIoModules() {
		this.documentExtractor = this.ioModuleRegistry.getDocumentExtractorsForFileType("docx").iterator().next();
		this.storyParser = this.ioModuleRegistry.getStoryParsers().iterator().next();
		this.reportTranscriber = this.ioModuleRegistry.getReportTranscribersForFileType("docx").iterator().next();

		// Need JavaFX to be running for report transcription chart generation to work. This will kick it off
		new JFXPanel();
	}

	Report executeInspectionTest(final InputStream storyInputStream, final ReportSpec reportSpec, final OutputStream outputStream,
			final ProgressMonitor monitor)
			throws InterruptedException, ExecutionException, FileNotFoundException, StoryIOException, TaskCanceledException {

		final ExtractedDocument extractedDoc = this.documentExtractor.extractDocument(storyInputStream,
				monitor.subMonitor(0.0f, 0.05f, "Document Extraction"));

		final Story story = this.storyParser.parseStory(extractedDoc, monitor.subMonitor(0.05f, 0.2f, "Story Parsing"));

		final Report report = this.reportExecutor.execute(reportSpec, story, monitor.subMonitor(0.2f, 0.9f, "Report Execution"));

		this.reportTranscriber.transcribeReport(report, extractedDoc, outputStream, monitor.subMonitor(0.9f, 1.0f, "Report Transcription"));

		return report;
	}

	/**
	 * Gets the {@link AnalyzerType} with the provided java type. If more than one exist with the provided type, throws RuntimeException.
	 *
	 * @param javaType
	 *            The type to search for.
	 * @throws RuntimeException
	 *             If more than one {@link AnalyzerType} exists with the provided type
	 */
	<T extends AnalyzerType<T>> T getAnalyzerTypeByJavaType(final Class<T> javaType) {
		final List<AnalyzerType<?>> matchingTypes = this.analyzerTypeRegistry.getCurrentAnalyzerTypes().stream()
				.filter(analyzerType -> javaType.isAssignableFrom(analyzerType.getClass())).collect(Collectors.toList());

		if (matchingTypes.isEmpty())
			return null;
		else if (matchingTypes.size() > 1)
			throw new RuntimeException("More than one AnalyzerType of " + javaType);
		else
			return javaType.cast(matchingTypes.get(0));
	}

	InputStream getStoryInputStream(final String storyFileName) throws URISyntaxException {
		return getClass().getResourceAsStream(STORY_FILE_RESOURCE_SUBDIRECTORY + storyFileName);
	}
}
