package com.story_inspector.fxmlUtil;

import javafx.scene.Parent;

/**
 * Loads JavaFX component from FXML location and automatically assigns appropriate controller.
 *
 * @author mizitch
 *
 */
public interface FxmlLoaderHelper {

	/**
	 * Loads JavaFX component from provided FXML location and automatically assigns appropriate controller.
	 *
	 * @param fxmlFileLocation
	 *            Location to load component from
	 * @return The loaded component
	 */
	public Parent load(String fxmlFileLocation);
}
