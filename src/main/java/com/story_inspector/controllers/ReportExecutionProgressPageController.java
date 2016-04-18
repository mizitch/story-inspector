package com.story_inspector.controllers;

import org.springframework.stereotype.Component;

import com.story_inspector.controllers.misc.ControllerUtils;
import com.story_inspector.controllers.misc.ReportExecutionTask;

import javafx.beans.binding.Bindings;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.Tooltip;

/**
 * Controller for report execution progress page. Shows the user where story inspector is in the process and gives them the ability to cancel the
 * report execution.
 *
 * This page does not close automatically when the task is complete or canceled or otherwise terminated.
 *
 * @author mizitch
 *
 */
@Component
public class ReportExecutionProgressPageController {

	@FXML
	private Label reportNameLabel;

	@FXML
	private Tooltip reportDescriptionTooltip;

	@FXML
	private Label storyNameLabel;

	@FXML
	private ProgressBar progressBar;

	@FXML
	private Label progressPercentageLabel;

	@FXML
	private Label progressDescriptionLabel;

	private ReportExecutionTask reportExecutionTask;

	/**
	 * Set the page to track the provided task. Sets labels appropriately and binds progress trackers. Sets it so that exiting this page cancels the
	 * task. This does not start the task and functions whether or not the task has already been started.
	 */
	public void setReportExecutionTask(final ReportExecutionTask reportExecutionTask) {
		this.reportExecutionTask = reportExecutionTask;

		this.progressDescriptionLabel.textProperty().bind(reportExecutionTask.messageProperty());
		this.progressPercentageLabel.textProperty().bind(Bindings.createStringBinding(
				() -> ((int) Math.round(reportExecutionTask.getProgress() * 100)) + "%", reportExecutionTask.progressProperty()));
		this.progressBar.progressProperty().bind(reportExecutionTask.progressProperty());

		this.reportNameLabel.setText(reportExecutionTask.getReportSpec().getName());
		this.reportDescriptionTooltip.setText(reportExecutionTask.getReportSpec().getDescription());
		this.storyNameLabel.setText(reportExecutionTask.getStoryName());

		ControllerUtils.performWithStage(this.progressBar, s -> s.setOnCloseRequest(e -> this.reportExecutionTask.cancel()));
	}

	/**
	 * Triggered by cancel button and closing the window. Sends a cancellation request to the task and in the meantime changes the progress
	 * description to inform the user the task is being canceled (in case it takes a while).
	 */
	@FXML
	private void cancel() {
		this.reportExecutionTask.cancel();
		this.progressDescriptionLabel.textProperty().unbind();
		this.progressDescriptionLabel.setText("Cancelling...");
	}
}
