package com.story_inspector.analysis.reports;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Component;

import com.story_inspector.analysis.Analyzer;
import com.story_inspector.analysis.AnalyzerResult;
import com.story_inspector.progress.ProgressMonitor;
import com.story_inspector.progress.TaskCanceledException;
import com.story_inspector.story.Story;

/**
 * Default implementation of {@link ReportExecutor}.
 *
 * @author mizitch
 *
 */
@Component
public class ReportExecutorImpl implements ReportExecutor {

	@Override
	public Report execute(final ReportSpec spec, final Story story, final ProgressMonitor progressMonitor) throws TaskCanceledException {
		progressMonitor.reportProgress(0.0f, "Starting");
		final int numAnalyzers = spec.getNumAnalyzers();

		final List<ReportSection> sections = new ArrayList<>();

		int analyzerIndex = 0;
		for (final ReportSectionSpec sectionSpec : spec.getSectionSpecs()) {
			final List<AnalyzerResult<?>> results = new ArrayList<>();
			for (final Analyzer<?> analyzer : sectionSpec.getAnalyzers()) {
				progressMonitor.reportProgress(analyzerIndex * 1.0f / numAnalyzers, "Executing Analyzer: " + analyzer.getName());
				results.add(analyzer.execute(story));
				analyzerIndex++;
			}
			sections.add(new ReportSection(sectionSpec, results));
		}
		return new Report(story, spec, sections);
	}

}
