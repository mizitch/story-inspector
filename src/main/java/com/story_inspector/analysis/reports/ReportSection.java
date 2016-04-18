package com.story_inspector.analysis.reports;

import java.util.List;

import org.apache.commons.lang3.Validate;

import com.story_inspector.analysis.Analyzer;
import com.story_inspector.analysis.AnalyzerResult;
import com.story_inspector.analysis.Describable;
import com.story_inspector.analysis.summary.ReportSummaryWriter;
import com.story_inspector.story.Story;

/**
 * One section of a completed report. Contains details on the results of each {@link Analyzer} from the section specification that was executed on the
 * {@link Story}.
 *
 * Also provides the {@link #writeSummary(ReportSummaryWriter) writeSummary} method, which is used to write details of this report section.
 *
 * Note that in the UI, the term "report section" is usually used to refer to what is actually a {@link ReportSectionSpec}.
 *
 * This class is designed to be immutable.
 *
 * @author mizitch
 *
 */
public class ReportSection implements Describable {
	private final ReportSectionSpec spec;
	private final List<AnalyzerResult<?>> analyzerResults;

	/**
	 * Creates a new instance.
	 *
	 * @param spec
	 *            The specification for this tab.
	 * @param analyzerResults
	 *            The results for each {@link Analyzer} executed.
	 */
	public ReportSection(final ReportSectionSpec spec, final List<AnalyzerResult<?>> analyzerResults) {
		super();
		this.spec = spec;
		this.analyzerResults = analyzerResults;

		Validate.notNull(spec);
		validateAnalyzerResults();
	}

	private void validateAnalyzerResults() {
		Validate.noNullElements(this.analyzerResults);
		Validate.isTrue(this.spec.getAnalyzers().size() == this.analyzerResults.size());
		for (int i = 0; i < this.analyzerResults.size(); ++i) {
			Validate.isTrue(this.analyzerResults.get(i).getAnalyzer().equals(this.spec.getAnalyzers().get(i)));
		}
	}

	@Override
	public String getName() {
		return this.spec.getName();
	}

	@Override
	public String getDescription() {
		return this.spec.getDescription();
	}

	/**
	 * Return the specification for this section.
	 *
	 * @return The specification for this section.
	 */
	public ReportSectionSpec getSpec() {
		return this.spec;
	}

	/**
	 * Return the list of {@link
	 *
	 * @return
	 */
	public List<AnalyzerResult<?>> getAnalyzerResults() {
		return this.analyzerResults;
	}

	/**
	 * Write a summary of this tab.
	 *
	 * @param reportSummaryWriter
	 *            The {@link ReportSummaryWriter} to write the summary to.
	 */
	public void writeSummary(final ReportSummaryWriter reportSummaryWriter) {
		reportSummaryWriter.writeHeading(this.getName(), 1);
		reportSummaryWriter.writeText(this.getDescription());
		for (final AnalyzerResult<?> results : this.analyzerResults) {
			results.writeSummary(reportSummaryWriter);
		}
	}
}
