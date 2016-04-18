package com.story_inspector.controllers;

import java.io.IOException;
import java.net.URISyntaxException;

import org.springframework.stereotype.Component;

import com.story_inspector.controllers.misc.ApplicationAware;
import com.story_inspector.controllers.misc.ControllerUtils;

import javafx.application.Application;
import javafx.fxml.FXML;
import javafx.scene.control.Button;

/**
 * Controller for standard manuscript format popup.
 *
 * @author mizitch
 *
 */
@Component
public class StandardManuscriptFormatController implements ApplicationAware {

	private static final String MANUSCRIPT_FORMAT_URL = "http://www.shunn.net/format/story.html";

	@FXML
	private Button okayButton;

	private Application application;

	/**
	 * Opens the standard manuscript format link in the user's browser. Triggered by the hyperlink in the popup.
	 */
	@FXML
	private void standardManuscriptFormat() throws IOException, URISyntaxException {
		this.application.getHostServices().showDocument(MANUSCRIPT_FORMAT_URL);
	}

	/**
	 * Closes the popup, triggered by the close button.
	 */
	@FXML
	private void closeWindow() {
		ControllerUtils.closeWindow(this.okayButton);
	}

	@Override
	public void setApplication(final Application application) {
		this.application = application;
	}
}
