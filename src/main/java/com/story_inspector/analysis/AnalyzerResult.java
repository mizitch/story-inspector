package com.story_inspector.analysis;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.apache.commons.lang.Validate;

import com.story_inspector.analysis.summary.AnalyzerSummaryComponent;
import com.story_inspector.analysis.summary.ReportSummaryWriter;
import com.story_inspector.story.Story;

/**
 * The results of executing an {@link Analyzer} on a {@link Story}. Contains a collection of {@link Comment}s and an {@link AnalyzerSummary}.
 *
 * @author mizitch
 *
 */
public class AnalyzerResult<T extends AnalyzerType<T>> {
	private final Analyzer<T> analyzer;
	private final Collection<Comment> comments;
	private final List<AnalyzerSummaryComponent> summaryComponents;

	/**
	 * Creates a new instance given the {@link Analyzer} and the {@link Comment}s and {@link AnalyzerSummary} it produced.
	 *
	 * @param analyzer
	 *            The {@link Analyzer} that was executed.
	 * @param comments
	 *            The comments produced by the {@link Analyzer}
	 * @param summary
	 *            The summary produced by the {@link AnalyzerSummary}
	 */
	public AnalyzerResult(final Analyzer<T> analyzer, final Collection<Comment> comments, final List<AnalyzerSummaryComponent> summaryComponents) {
		super();
		Validate.notNull(analyzer);
		Validate.noNullElements(comments);
		Validate.noNullElements(summaryComponents);
		Validate.isTrue(analyzer.getAnalyzerType().producesComments() || comments.isEmpty(),
				"Analyzer type " + analyzer.getAnalyzerType() + " produced comments when it advertises that it does not.");

		// TODO: defensive clone of analyzer necessary? probably not?

		this.analyzer = analyzer;
		this.comments = new ArrayList<Comment>(comments);
		this.summaryComponents = new ArrayList<>(summaryComponents);
	}

	/**
	 * Returns the {@link Analyzer} that was executed.
	 *
	 * @return The {@link Analyzer} that was executed.
	 */
	public Analyzer<T> getAnalyzer() {
		return this.analyzer;
	}

	/**
	 * Returns the comments generated.
	 *
	 * @return The comments generated.
	 */
	public Collection<Comment> getComments() {
		if (this.analyzer.isCommentRecordingSuppressed())
			return Collections.emptyList();
		else
			return Collections.unmodifiableCollection(this.comments);
	}

	/**
	 * Returns the components of the result summary.
	 *
	 * @return The components of the result summary.
	 */
	public List<AnalyzerSummaryComponent> getSummaryComponents() {
		return this.summaryComponents;
	}

	/**
	 * Writes this {@link AnalyzerResult} into the report summary.
	 *
	 * @param reportSummaryWriter
	 *            The {@link ReportSummaryWriter} to write to.
	 */
	public void writeSummary(final ReportSummaryWriter reportSummaryWriter) {
		reportSummaryWriter.writeHeading(this.analyzer.getName(), 2);
		reportSummaryWriter.writeText(this.analyzer.getDescription());
		for (final AnalyzerSummaryComponent component : this.summaryComponents) {
			component.write(reportSummaryWriter);
		}
	}

}
