package com.story_inspector.analysis.summary;

import com.story_inspector.analysis.Analyzer;

/**
 * A component of a summary produced by an {@link Analyzer}.
 *
 * @author mizitch
 *
 */
public interface AnalyzerSummaryComponent {

	/**
	 * Write this component.
	 *
	 * @param writer
	 *            The {@link ReportSummaryWriter} to write to.
	 */
	public void write(ReportSummaryWriter writer);

}
