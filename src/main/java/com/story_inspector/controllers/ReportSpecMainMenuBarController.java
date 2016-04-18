package com.story_inspector.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.story_inspector.controllers.misc.ControllerUtils;

import javafx.fxml.FXML;
import javafx.scene.control.MenuBar;

/**
 * Controller for the main menu bar for the report spec edit page.
 *
 * @author mizitch
 *
 */
@Component
public class ReportSpecMainMenuBarController {

	@FXML
	private MenuBar mainMenuBar;

	@Autowired
	private ReportSpecEditPaneController reportSpecEditPaneController;

	@Autowired
	private ReportSpecPageController reportSpecPageController;

	/**
	 * Triggered by new menu item.
	 */
	@FXML
	private void newReportSpec() {
		this.reportSpecEditPaneController.getSaveManager().newEntity(false,
				"Save changes to current report before creating a new one? Otherwise your current changes will be lost.");
	}

	/**
	 * Triggered by open menu item.
	 */
	@FXML
	private void openReportSpec() {
		this.reportSpecEditPaneController.getSaveManager().openFromFile();
	}

	/**
	 * Triggered by save menu item.
	 */
	@FXML
	private void saveReportSpec() {
		this.reportSpecEditPaneController.getSaveManager().save();
	}

	/**
	 * Triggered by save as menu item.
	 */
	@FXML
	private void saveReportSpecAs() {
		this.reportSpecEditPaneController.getSaveManager().saveAsFile();
	}

	/**
	 * Triggered by use report menu item.
	 */
	@FXML
	private void useReportSpec() {
		this.reportSpecPageController.useReport();
	}

	@FXML
	private void about() {
		// TODO
	}

	/**
	 * Triggered by exit menu item.
	 */
	@FXML
	private void exit() {
		ControllerUtils.closeWindow(this.mainMenuBar);
	}
}
