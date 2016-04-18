package com.story_inspector.controllers;

import java.io.File;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.story_inspector.analysis.reports.ReportSpec;
import com.story_inspector.controllers.misc.ControllerUtils;

import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.SplitPane;

/**
 * Controller for {@link ReportSpec} edit page. Contains analyzer pane and report spec edit pane. Contains a member variable for the selected report
 * spec that is set when the user confirms their report spec choice.
 *
 * @author mizitch
 *
 */
@Component
public class ReportSpecPageController {

	@Autowired
	private ReportSpecEditPaneController reportSpecEditPaneController;

	@FXML
	private SplitPane splitPane;

	private ReportSpec selectedSpec = null;

	private File selectedReportSpecFile = null;

	/**
	 * Choose this report as the end result of the edit page action, closing the page. First it attempts to save the report and aborts if that does
	 * not succeed (alerting the user).
	 */
	public void useReport() {
		if (this.reportSpecEditPaneController.getSaveManager().save()) {
			this.selectedSpec = this.reportSpecEditPaneController.getReportSpec();
			this.selectedReportSpecFile = this.reportSpecEditPaneController.getSaveManager().getCurrentSaveLocation();
			ControllerUtils.closeWindow(this.splitPane);
		} else {
			final Alert alert = new Alert(AlertType.INFORMATION, "Report must be saved before it can be used.");
			alert.showAndWait();
		}
	}

	/**
	 * Return the report spec that is the final result of this page. Returns null if the user has not confirmed by pressing "use report"
	 *
	 * @return The report spec that is the final result of this page. Null if the user has not confirmed by pressing "use report"
	 */
	public ReportSpec getSelectedReportSpec() {
		return this.selectedSpec;
	}

	/**
	 * Return the report spec file that is the final result of this page. Returns null if the user has not confirmed by pressing "use report"
	 *
	 * @return The report spec file that is the final result of this page. Null if the user has not confirmed by pressing "use report"
	 */
	public File getSelectedReportSpecFile() {
		return this.selectedReportSpecFile;
	}

	/**
	 * Set the report spec page to edit the spec contained in the provided file.
	 *
	 * @param reportSpecFile
	 *            The file of the spec to edit.
	 */
	public void editReportSpec(final File reportSpecFile) {
		clearSelectedSpec();
		this.reportSpecEditPaneController.getSaveManager().openFromFile(reportSpecFile);
	}

	/**
	 * Open an existing report spec from the file system.
	 */
	public void openReportSpec() {
		clearSelectedSpec();
		this.reportSpecEditPaneController.getSaveManager().openFromFile();
	}

	/**
	 * Create a new, empty report spec.
	 */
	public void newReportSpec() {
		clearSelectedSpec();
		this.reportSpecEditPaneController.getSaveManager().newEntity(false);
	}

	/**
	 * Clear the result report spec if it is currently set.
	 */
	private void clearSelectedSpec() {
		this.selectedSpec = null;
		this.selectedReportSpecFile = null;
	}
}