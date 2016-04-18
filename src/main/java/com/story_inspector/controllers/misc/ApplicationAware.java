package com.story_inspector.controllers.misc;

import javafx.application.Application;

/**
 * Interface for classes which are spring beans to implement if they need access to the JavaFX application. Will not be effective on any instance not
 * managed by spring.
 *
 * @author mizitch
 */
public interface ApplicationAware {

	/**
	 * Gives this instance a reference to the running JavaFX Application class.
	 * 
	 * @param application
	 *            The running JavaFX application.
	 */
	public void setApplication(Application application);
}
