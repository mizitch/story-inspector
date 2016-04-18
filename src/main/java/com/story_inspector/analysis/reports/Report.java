package com.story_inspector.analysis.reports;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.commons.lang3.Validate;

import com.story_inspector.analysis.Analyzer;
import com.story_inspector.analysis.Describable;
import com.story_inspector.analysis.summary.ReportSummaryWriter;
import com.story_inspector.story.Story;

/**
 * A completed report. Contains details on the results of each {@link Analyzer} that was executed on the {@link Story}.
 *
 * Also provides the {@link #writeSummary(ReportSummaryWriter) writeSummary} method, which is used to write details of this report.
 *
 * Note that in the UI, the term "report" is usually used to refer to what is actually a {@link ReportSpec}.
 *
 * This class is designed to be immutable.
 *
 * @author mizitch
 *
 */
public class Report implements Describable, Cloneable {

	private final ReportSpec spec;
	private final List<ReportSection> reportSections;
	private final Story story;

	/**
	 * Creates a new instance.
	 *
	 * @param story
	 *            The {@link Story} this report is about.
	 * @param spec
	 *            The {@link ReportSpec} used to generate this report.
	 * @param reportTabs
	 *            The data of this report, organized into {@link ReportSection}s.
	 */
	Report(final Story story, final ReportSpec spec, final List<ReportSection> reportTabs) {
		super();
		Validate.notNull(story);
		Validate.notNull(spec);
		this.story = story;
		this.spec = spec;
		this.reportSections = new ArrayList<>(reportTabs);
		validateReportTabs();
	}

	private void validateReportTabs() {
		Validate.noNullElements(this.reportSections);
		Validate.isTrue(this.spec.getSectionSpecs().size() == this.reportSections.size());
		for (int i = 0; i < this.reportSections.size(); ++i) {
			Validate.isTrue(this.reportSections.get(i).getSpec().equals(this.spec.getSectionSpecs().get(i)));
		}
	}

	/**
	 * Returns the {@link Story} this report is about.
	 *
	 * @return The {@link Story} this report is about.
	 */
	public Story getStory() {
		return this.story;
	}

	/**
	 * Returns the {@link ReportSpec} used to generate this report.
	 *
	 * @return The {@link ReportSpec} used to generate this report.
	 */
	public ReportSpec getSpec() {
		return this.spec;
	}

	/**
	 * Return the data of this report, organized into {@link ReportSection}s.
	 *
	 * @return The data of this report, organized into {@link ReportSection}s.
	 */
	public List<ReportSection> getReportSections() {
		return Collections.unmodifiableList(this.reportSections);
	}

	/**
	 * Write the summary of this report to the provided {@link ReportSummaryWriter}.
	 *
	 * @param reportSummaryWriter
	 */
	public void writeSummary(final ReportSummaryWriter reportSummaryWriter) {
		reportSummaryWriter.writeHeading("\"" + this.story.getTitle() + "\"", 0);
		reportSummaryWriter.writeHeading(this.getName() + " Results", 0);
		reportSummaryWriter.writeText(this.getDescription());

		for (final ReportSection section : this.reportSections) {
			reportSummaryWriter.addPageBreak();
			section.writeSummary(reportSummaryWriter);
		}

		reportSummaryWriter.endReportSummary();
	}

	@Override
	public String getName() {
		return this.spec.getName();
	}

	@Override
	public String getDescription() {
		return this.spec.getDescription();
	}
}
