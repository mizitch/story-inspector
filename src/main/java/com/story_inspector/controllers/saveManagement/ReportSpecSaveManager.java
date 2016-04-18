package com.story_inspector.controllers.saveManagement;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Arrays;

import com.story_inspector.analysis.reports.ReportSpec;
import com.story_inspector.analysis.serialization.AnalysisSerializer;

import javafx.stage.FileChooser.ExtensionFilter;

/**
 * {@link FileSystemSaveManager} implementation for {@link ReportSpec}s.
 *
 * @author mizitch
 *
 */
public class ReportSpecSaveManager extends FileSystemSaveManagerBase<ReportSpec> {

	private final AnalysisSerializer analysisSerializer;

	/**
	 * Creates a new instance
	 *
	 * @param trackedContainer
	 *            The {@link SavableContainer} to track
	 * @param analysisSerializer
	 *            Helper to serialize and deserialize {@link ReportSpec}s to and from files.
	 */
	public ReportSpecSaveManager(final SavableContainer<ReportSpec> trackedContainer, final AnalysisSerializer analysisSerializer) {
		super(trackedContainer, "Report", Arrays.asList(new ExtensionFilter("Story Inspector Report Specification Files", "*.sir")));
		this.analysisSerializer = analysisSerializer;
	}

	@Override
	public void writeEntity(final ReportSpec entity, final OutputStream out) throws IOException {
		this.analysisSerializer.writeReportSpec(entity, out);
	}

	@Override
	public ReportSpec readEntity(final InputStream in) throws IOException {
		return this.analysisSerializer.readReportSpec(in);
	}

}
