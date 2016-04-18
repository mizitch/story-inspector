package com.story_inspector.controllers.saveManagement;

import java.io.File;

/**
 * Interface for {@link SaveManager}s that use the file system to store saved entities. Also has support for opening entities (from the file system).
 *
 * @author mizitch
 *
 * @param <T>
 *            The type of entity saved
 */
public interface FileSystemSaveManager<T> extends SaveManager<T> {

	/**
	 * Open a "save as" dialog for the entity. Allows an entity that is already saved to be saved to a new file location.
	 *
	 * @return Whether the file was successfully saved.
	 */
	boolean saveAsFile();

	/**
	 * Opens an open dialog to let the user select a file to load the entity from.
	 *
	 * @return Whether the file was opened successfully
	 */
	boolean openFromFile();

	/**
	 * Directly opens the provided file, loading the entity.
	 *
	 * @param file
	 *            The file to load the entity from
	 * @return Whether it was successfully opened
	 */
	boolean openFromFile(File file);

	/**
	 * Returns the current/default save location of the file. If the file has been saved, this is its last save location. If this file was opened from
	 * a file, this is the file it was opened from. Otherwise this is unset.
	 *
	 * @return The current/default save location of the file.
	 */
	File getCurrentSaveLocation();
}
