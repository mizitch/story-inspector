package com.story_inspector.analysis.serialization;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;

import com.story_inspector.analysis.Analyzer;
import com.story_inspector.analysis.AnalyzerType;
import com.story_inspector.analysis.reports.ReportSpec;
import com.story_inspector.story.Story;

/**
 * Specification for module that handles serialization and deserialization of savable entities related to {@link Story} analysis. Currently this
 * includes {@link Analyzer}s and {@link ReportSpec}s.
 *
 * @author mizitch
 *
 */
public interface AnalysisSerializer {

	/**
	 * Write the provided analyzer to the provided stream.
	 *
	 * @param analyzer
	 *            The {@link Analyzer} to write.
	 * @param outputStream
	 *            The {@link OutputStream} to write to.
	 * @throws IOException
	 *             If there is an IO issue.
	 */
	public void writeAnalyzer(Analyzer<?> analyzer, OutputStream outputStream) throws IOException;

	/**
	 * Read an analyzer from the provided stream.
	 *
	 * @param inputStream
	 *            The {@link InputStream} to read an {@link Analyzer} from.
	 * @return The read {@link Analyzer}.
	 * @throws IOException
	 *             If there is an IO issue while reading the {@link Analyzer}, or the input stream does not contain a valid {@link Analyzer}.
	 */
	public <T extends AnalyzerType<T>> Analyzer<T> readAnalyzer(InputStream inputStream) throws IOException;

	/**
	 * Write a list of analyzers to the provided stream.
	 *
	 * @param analyzers
	 *            The {@link Analyzer}s to write.
	 * @param outputStream
	 *            The {@link OutputStream} to write to.
	 * @throws IOException
	 *             If there is an IO issue.
	 */
	public void writeAnalyzers(List<Analyzer<?>> analyzers, OutputStream outputStream) throws IOException;

	/**
	 * Reads a list of analyzers from the provided stream.
	 *
	 * @param inputStream
	 *            The {@link InputStream} to read an {@link Analyzer} from.
	 * @return The read {@link Analyzer}.
	 * @throws IOException
	 *             If there is an IO issue while reading the {@link Analyzer}s, or the input stream does not contain a valid list of {@link Analyzer}
	 *             s.
	 */
	public List<Analyzer<?>> readAnalyzers(InputStream inputStream) throws IOException;

	/**
	 * Write the provided report spec to the provided stream.
	 *
	 * @param analyzer
	 *            The {@link ReportSpec} to write.
	 * @param outputStream
	 *            The {@link OutputStream} to write to.
	 * @throws IOException
	 *             If there is an IO issue.
	 */
	public void writeReportSpec(ReportSpec spec, OutputStream outputStream) throws IOException;

	/**
	 * Read a report spec from the provided stream.
	 *
	 * @param inputStream
	 *            The {@link InputStream} to read an {@link ReportSpec} from.
	 * @return The read {@link ReportSpec}.
	 * @throws IOException
	 *             If there is an IO issue while reading the {@link ReportSpec}, or the input stream does not contain a valid {@link ReportSpec}.
	 */
	public ReportSpec readReportSpec(InputStream inputStream) throws IOException;
}
