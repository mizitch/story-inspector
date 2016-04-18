package com.story_inspector.controllers.saveManagement;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.Validate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ButtonType;
import javafx.stage.FileChooser;
import javafx.stage.FileChooser.ExtensionFilter;

/**
 * Base implementation of {@link FileSystemSaveManager}
 *
 * @author mizitch
 *
 * @param <T>
 *            The type of entity saved.
 */
public abstract class FileSystemSaveManagerBase<T> extends SaveManagerBase<T> implements FileSystemSaveManager<T> {
	private static final Logger log = LoggerFactory.getLogger(FileSystemSaveManagerBase.class);

	// TODO keep track of initial directory changes across application restarts
	private File startingDirectory = new File(System.getProperty("user.home"));

	private File currentSaveLocation = null;

	private final List<ExtensionFilter> extensionFilters;

	private final String entityTypeName;

	protected FileSystemSaveManagerBase(final SavableContainer<T> trackedContainer, final String entityTypeName,
			final List<ExtensionFilter> extensionFilters) {
		super(trackedContainer);
		Validate.notBlank(entityTypeName);
		Validate.notEmpty(extensionFilters);
		this.entityTypeName = entityTypeName;
		this.extensionFilters = new ArrayList<>(extensionFilters);
	}

	@Override
	public void newEntity(final boolean countsAsUnsaved, final String message) {
		super.newEntity(countsAsUnsaved, message);
		this.currentSaveLocation = null;
	}

	@Override
	public void newEntity(final boolean countsAsUnsaved) {
		super.newEntity(countsAsUnsaved);
		this.currentSaveLocation = null;
	}

	@Override
	public boolean forceSave() {
		if (this.currentSaveLocation == null) {
			return saveAsFile();
		} else {
			return save(getSavableContainer().getSavableEntity(), this.currentSaveLocation);
		}
	}

	@Override
	public boolean saveAsFile() {
		final FileChooser fileChooser = new FileChooser();
		fileChooser.setInitialDirectory(this.startingDirectory);
		fileChooser.setTitle("Save " + this.entityTypeName + " As");
		fileChooser.getExtensionFilters().addAll(this.extensionFilters);
		File chosenFile = fileChooser.showSaveDialog(this.getStage());

		if (chosenFile != null) {

			// JavaFX filechooser doesn't automatically add an extension, but if we do it ourselves, we dodge the filechooser's file overwrite
			// validation, so we have to make up that gap ourselves
			// Incredibly annoying... >:(
			if (!endsWithExtension(chosenFile)) {
				// Our constructor guarantees at least one filter, filter constructor guarantees at least one extension in a filter
				chosenFile = new File(chosenFile.getPath() + extractActualExtension(this.extensionFilters.get(0).getExtensions().get(0)));
				if (chosenFile.exists()) {
					final Alert overwriteFileAlert = new Alert(AlertType.CONFIRMATION, "This file already exists, do you want to overwrite it?");
					if (!overwriteFileAlert.showAndWait().filter(response -> response == ButtonType.OK).isPresent()) {
						return false;
					}
				}
			}
			return save(this.getSavableContainer().getSavableEntity(), chosenFile);
		}
		return false;
	}

	private boolean endsWithExtension(final File chosenFile) {
		for (final ExtensionFilter filter : this.extensionFilters) {
			for (final String extension : filter.getExtensions()) {
				if (chosenFile.getName().toLowerCase().endsWith(extractActualExtension(extension).toLowerCase())) {
					return true;
				}
			}
		}
		return false;
	}

	private String extractActualExtension(final String extensionFilterExtension) {
		return extensionFilterExtension.substring(1);
	}

	/**
	 * Saves the entity to the provided location. Updates the saved entity so it is now marked as "saved." Prompts the user if the save failed.
	 *
	 * @return Whether the save was successful
	 */
	private boolean save(final T entity, final File saveLocation) {
		try {
			writeEntity(entity, new FileOutputStream(saveLocation));

			this.startingDirectory = saveLocation.getParentFile();
			this.currentSaveLocation = saveLocation;
		} catch (final IOException e) {
			log.error("Caught exception while saving entity", e);
			final Alert alert = new Alert(AlertType.ERROR, "Failed to save entity.");
			alert.showAndWait();
			return false;
		}

		this.syncSavedEntity();
		return true;
	}

	@Override
	public boolean openFromFile() {
		if (this.hasUnsavedChanges()) {
			final boolean safeToProceed = promptSave("Save changes to current " + this.entityTypeName.toLowerCase() + " before opening a different "
					+ this.entityTypeName.toLowerCase() + "? Otherwise your current changes will be lost.");

			if (!safeToProceed)
				return false;
		}

		final FileChooser fileChooser = new FileChooser();
		fileChooser.setInitialDirectory(this.startingDirectory);
		fileChooser.setTitle("Open " + this.entityTypeName);
		fileChooser.getExtensionFilters().addAll(this.extensionFilters);
		final File file = fileChooser.showOpenDialog(this.getStage());

		if (file == null) {
			return false;
		} else {
			return openFromFile(file);
		}
	}

	@Override
	public boolean openFromFile(final File file) {
		T loadedEntity;
		try {
			loadedEntity = readEntity(new FileInputStream(file));
			this.getSavableContainer().setSavableEntity(loadedEntity);

			this.startingDirectory = file.getParentFile();
			this.currentSaveLocation = file;
			this.syncSavedEntity();
			return true;
		} catch (final IOException e) {
			log.error("Caught exception while opening entity", e);
			final Alert alert = new Alert(AlertType.ERROR, this.entityTypeName + " file is corrupt.");
			alert.showAndWait();
			return false;
		}
	}

	@Override
	public File getCurrentSaveLocation() {
		return this.currentSaveLocation;
	}

	/**
	 * Writes the entity to an output stream. Used by {@link FileSystemSaveManagerBase} when saving the entity to a file.
	 *
	 * @param entity
	 *            The entity to save
	 * @param out
	 *            The output stream to save the entity to.
	 * @throws IOException
	 *             If there is an IO error while writing the entity.
	 */
	protected abstract void writeEntity(T entity, OutputStream out) throws IOException;

	/**
	 * Reads the entity from an input stream. Used by {@link FileSystemSaveManagerBase} when opening the entity from a file.
	 *
	 * @param in
	 *            The input stream to retrieve the entity from.
	 * @return The retrieved entity
	 * @throws IOException
	 *             If there is an IO error while reading the entity
	 */
	protected abstract T readEntity(InputStream in) throws IOException;
}
