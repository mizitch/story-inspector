package com.story_inspector.controllers;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.util.function.Consumer;

import javax.annotation.PostConstruct;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.story_inspector.analysis.reports.ReportExecutor;
import com.story_inspector.analysis.reports.ReportSpec;
import com.story_inspector.controllers.misc.ControllerUtils;
import com.story_inspector.controllers.misc.ReportExecutionTask;
import com.story_inspector.fxmlUtil.FxmlLoaderHelper;
import com.story_inspector.ioProcessing.DocumentExtractor;
import com.story_inspector.ioProcessing.IoModuleRegistry;
import com.story_inspector.ioProcessing.ReportTranscriber;
import com.story_inspector.ioProcessing.StoryParser;

import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.FileChooser;
import javafx.stage.FileChooser.ExtensionFilter;
import javafx.stage.Stage;

/**
 * Controller class for Story Inspector's main page.
 *
 * @author mizitch
 *
 */
@Component
public class MainPageController {
	private static final Logger log = LoggerFactory.getLogger(MainPageController.class);

	@FXML
	private Label reportNameLabel;

	@FXML
	private Label reportDescriptionLabel;

	@FXML
	private Label reportMetadataLabel;

	@FXML
	private Label documentNameLabel;

	@FXML
	private ImageView selectReportCheckboxImageView;

	@FXML
	private ImageView selectStoryCheckboxImageView;

	@FXML
	private Button editButton;

	@FXML
	private Button inspectButton;

	@Autowired
	private ReportSpecPageController reportSpecPageController;

	@Autowired
	private FxmlLoaderHelper fxmlLoaderHelper;

	@Autowired
	private IoModuleRegistry ioModuleRegistry;

	@Autowired
	private ReportExecutor reportExecutor;

	@Autowired
	private ReportExecutionProgressPageController reportExecutionProgressPageController;

	private DocumentExtractor extractor;
	private StoryParser storyParser;
	private ReportTranscriber reportTranscriber;

	private Image uncheckedCheckboxImage;

	private Image checkedCheckboxImage;

	private File selectedStoryFile = null;

	private ReportSpec selectedReportSpec = null;

	private File selectedReportSpecFile = null;

	@PostConstruct
	private void initializeIoModules() {
		// The interfaces support more sophisticated stuff...but for now we have exactly one of each of these, so just grabbing them
		this.extractor = this.ioModuleRegistry.getDocumentExtractorsForFileType("docx").iterator().next();
		this.storyParser = this.ioModuleRegistry.getStoryParsers().iterator().next();
		this.reportTranscriber = this.ioModuleRegistry.getReportTranscribersForFileType("docx").iterator().next();
	}

	// BUTTON/HYPERLINK ACTIONS

	/**
	 * Executed after FXML initialization completes. This is after spring initialization but before the stage is created and shown.
	 */
	@FXML
	private void initialize() {
		this.uncheckedCheckboxImage = new Image("/images/unchecked_check_box_transparent.png");
		this.checkedCheckboxImage = new Image("/images/checked_check_box_transparent.png");
		updateControls();
	}

	/**
	 * Triggered by new report button. Creates a new report and opens the report editing interface.
	 */
	@FXML
	private void newReport() {
		selectReportSpec(controller -> controller.newReportSpec(), true);
	}

	/**
	 * Triggered by open report button. Opens a file chooser dialogue. Does not open the report editing interface.
	 */
	@FXML
	private void openReport() {
		selectReportSpec(controller -> controller.openReportSpec(), false);
	}

	/**
	 * Triggered by edit report button. Opens the report editing interface for the currently selected report.
	 */
	@FXML
	private void editReport() {
		selectReportSpec(controller -> controller.editReportSpec(this.selectedReportSpecFile), true);
	}

	/**
	 * Triggered by "standard manuscript format" hyperlink. Opens info popup on proper story format.
	 */
	@FXML
	private void standardManuscriptFormat() {
		ControllerUtils.openFxmlInWindow(this.fxmlLoaderHelper, "/fxml/StandardManuscriptFormat.fxml", "Standard Manuscript Format");
	}

	/**
	 * Triggered by the inspect button. Opens a dialogue for the user to decide the output file for the report, then executes the report, writing the
	 * output to that file.
	 *
	 * @throws FileNotFoundException
	 *             If the story file or report spec file no longer exist.
	 */
	@FXML
	private void inspect() {
		final FileChooser fileChooser = new FileChooser();
		fileChooser.setTitle("Choose Report Output File");
		fileChooser.setInitialDirectory(this.selectedStoryFile.getParentFile());
		fileChooser.setInitialFileName(getDefaultReportOutputFile().getName());
		fileChooser.getExtensionFilters().add(new ExtensionFilter("Microsoft Word Open XML Format Document", "*.docx"));
		final File reportOutputFile = fileChooser.showSaveDialog(ControllerUtils.getStage(this.reportNameLabel));

		if (reportOutputFile != null) {
			try {
				executeReport(reportOutputFile);
			} catch (final FileNotFoundException e) {
				final Alert alert = new Alert(AlertType.ERROR, "File not found: " + e.getLocalizedMessage(), ButtonType.OK);
				alert.showAndWait();
			}
		}
	}

	// SELECTING REPORT AND STORY

	/**
	 * Helper method for selecting a report spec. Triggers an action on the report spec edit page and shows that page if specified.
	 *
	 * @param reportEditAction
	 *            Action to perform on report edit page
	 * @param openEditor
	 *            Whether to open the report edit page
	 */
	private void selectReportSpec(final Consumer<ReportSpecPageController> reportEditAction, final boolean openEditor) {

		final Stage stage = ControllerUtils.setupFxmlInWindow(this.fxmlLoaderHelper, "/fxml/ReportSpecPage.fxml", "Story Inspector Report Editor");
		reportEditAction.accept(this.reportSpecPageController);
		if (openEditor) {
			stage.showAndWait();
		} else {
			this.reportSpecPageController.useReport();
		}
		if (this.reportSpecPageController.getSelectedReportSpec() != null) {
			this.selectedReportSpec = this.reportSpecPageController.getSelectedReportSpec();
			this.selectedReportSpecFile = this.reportSpecPageController.getSelectedReportSpecFile();
		}
		updateControls();
	}

	/**
	 * Triggered by the select story button. Opens a file chooser dialogue for the user to select a story document.
	 */
	@FXML
	private void selectStory() {
		final FileChooser fileChooser = new FileChooser();
		fileChooser.setTitle("Select Story");
		fileChooser.getExtensionFilters().add(new ExtensionFilter("Microsoft Word Open XML Format Document", "*.docx"));
		// TODO keep track of initial directory changes across application restarts
		fileChooser.setInitialDirectory(new File(System.getProperty("user.home")));
		final File storyFile = fileChooser.showOpenDialog(ControllerUtils.getStage(this.reportNameLabel));
		if (storyFile != null) {
			this.selectedStoryFile = storyFile;
			updateControls();
		}
	}

	/**
	 * Is a story currently selected?
	 */
	private boolean storySelected() {
		return this.selectedStoryFile != null;
	}

	/**
	 * Is a report currently selected?
	 */
	private boolean reportSelected() {
		return this.selectedReportSpec != null;
	}

	/**
	 * Update the controls based on whether the user has selected a story, a report, both or neither.
	 */
	private void updateControls() {
		updateStoryControls();
		updateReportControls();
		this.inspectButton.setDisable(!storySelected() || !reportSelected());
	}

	/**
	 * Update the report controls based on whether a report is selected
	 */
	private void updateReportControls() {
		if (reportSelected()) {
			this.reportNameLabel.setText(this.selectedReportSpec.getName());
			this.reportDescriptionLabel.setText(this.selectedReportSpec.getDescription());
			this.reportMetadataLabel.setText("This report has " + this.selectedReportSpec.getNumAnalyzers() + " analyzers.");
			this.selectReportCheckboxImageView.setImage(this.checkedCheckboxImage);
			this.editButton.setDisable(false);
		} else {
			this.reportNameLabel.setText("");
			this.reportDescriptionLabel.setText("");
			this.reportMetadataLabel.setText("");
			this.selectReportCheckboxImageView.setImage(this.uncheckedCheckboxImage);
			this.editButton.setDisable(true);
		}
	}

	/**
	 * Update the story controls based on whether a story is selected
	 */
	private void updateStoryControls() {
		if (storySelected()) {
			this.documentNameLabel.setText(this.selectedStoryFile.getName());
			this.selectStoryCheckboxImageView.setImage(this.checkedCheckboxImage);
		} else {
			this.documentNameLabel.setText("");
			this.selectStoryCheckboxImageView.setImage(this.uncheckedCheckboxImage);
		}
	}

	// REPORT EXECUTION (INSPECTION)

	/**
	 * Executes the selected report on the selected story and writes the output to the provided file.
	 *
	 * @param reportOutputFile
	 *            The file to write the report to.
	 * @throws FileNotFoundException
	 *             If the story file or report spec file no longer exist.
	 */
	private void executeReport(final File reportOutputFile) throws FileNotFoundException {

		final ReportExecutionTask task = new ReportExecutionTask(this.selectedReportSpec, this.extractor, this.storyParser, this.reportExecutor,
				this.reportTranscriber, this.selectedStoryFile.getName(), new FileInputStream(this.selectedStoryFile),
				new FileOutputStream(reportOutputFile));

		final Stage executionProgressStage = ControllerUtils.setupFxmlInWindow(this.fxmlLoaderHelper, "/fxml/ReportExecutionProgressPage.fxml",
				"Executing Report");

		final Alert alert = new Alert(AlertType.ERROR, null, ButtonType.OK);

		task.setOnSucceeded(e -> { // TODO would be nice to open report in word processor, but recommended method just crashes the application
			ControllerUtils.closeWindow(executionProgressStage);
		});

		task.setOnCancelled(e -> {
			ControllerUtils.closeWindow(executionProgressStage);
		});

		task.setOnFailed(e -> { // TODO better handling than just getMessage
			ControllerUtils.closeWindow(executionProgressStage);
			log.error("Caught exception while executing report", task.getException());
			alert.setContentText(task.getException().getMessage());
		});

		final Thread thread = new Thread(task);
		thread.setDaemon(true);
		thread.start();
		this.reportExecutionProgressPageController.setReportExecutionTask(task);
		executionProgressStage.showAndWait();

		if (alert.getContentText() != null) {
			alert.showAndWait();
		}
	}

	/**
	 * Generates default/recommended report output file name based on story file and report spec name.
	 */
	private File getDefaultReportOutputFile() {
		String fileName = this.selectedStoryFile.getName();
		if (fileName.contains(".")) {
			final int extensionStart = fileName.lastIndexOf('.');
			fileName = fileName.substring(0, extensionStart);
			fileName = fileName + "_report_" + this.selectedReportSpec.getName() + ".docx";
		}
		return new File(this.selectedStoryFile.getParentFile().getPath() + File.separator + fileName);
	}
}
