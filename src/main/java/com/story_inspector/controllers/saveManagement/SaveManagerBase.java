package com.story_inspector.controllers.saveManagement;

import java.util.Optional;

import org.apache.commons.lang.ObjectUtils;

import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ButtonType;
import javafx.stage.Stage;

/**
 * Base implementation of {@link SaveManager}
 *
 * @author mizitch
 *
 * @param <T>
 *            Type of entity tracked.
 */
public abstract class SaveManagerBase<T> implements SaveManager<T> {

	private final SavableContainer<T> trackedContainer;

	private Stage stage;

	private T savedEntity;

	/**
	 * Creates a new instance.
	 *
	 * @param trackedContainer
	 *            The {@link SavableContainer} to track.
	 * @param entityTypeName
	 */
	protected SaveManagerBase(final SavableContainer<T> trackedContainer) {
		this.trackedContainer = trackedContainer;
	}

	@Override
	public void newEntity(final boolean countsAsUnsaved, final String message) {

		if (hasUnsavedChanges()) {
			final boolean safeToProceed = promptSave(message);

			if (!safeToProceed)
				return;
		}
		newEntity(countsAsUnsaved);
	}

	@Override
	public void newEntity(final boolean countsAsUnsaved) {
		this.trackedContainer.newEntity();
		this.savedEntity = countsAsUnsaved ? null : this.trackedContainer.getSavableEntity();
	}

	/**
	 * Asks if the user wants to save the entity before proceeding with that action (which will involve losing any changes to the entity).
	 *
	 * @param message
	 *            The message to show to the user. A yes response to this question means save first, a no response means proceed without saving, a
	 *            cancel response means abort the desired action.
	 * @return Whether to continue with the desired action. False if the user hits cancel or exit on the prompt or hits yes but fails to save.
	 */
	@Override
	public boolean promptSave(final String message) {
		if (hasUnsavedChanges()) {

			final Alert alert = new Alert(AlertType.CONFIRMATION, message, ButtonType.YES, ButtonType.NO, ButtonType.CANCEL);
			final Optional<ButtonType> response = alert.showAndWait();

			// No response or cancel, abort the action
			if (!response.isPresent() || response.get() == ButtonType.CANCEL)
				return false;

			// Save before proceeding, if save fails then abort
			if (response.get() == ButtonType.YES) {
				save();
				// User indicated they wanted to save, but save failed or they aborted the save, so don't exit
				if (hasUnsavedChanges())
					return false;
			}
		}
		return true;
	}

	@Override
	public boolean save() {
		if (hasUnsavedChanges()) {
			final boolean result = forceSave();
			if (result) {
				syncSavedEntity();
			}
			return result;
		} else {
			return true;
		}
	}

	protected Stage getStage() {
		return this.stage;
	}

	/**
	 * Force a save whether or not there are unsaved changes. Don't prompt the user.
	 *
	 * @return Whether this save was successful.
	 */
	protected abstract boolean forceSave();

	@Override
	public SavableContainer<T> getSavableContainer() {
		return this.trackedContainer;
	}

	@Override
	public boolean hasUnsavedChanges() {
		return !ObjectUtils.equals(this.savedEntity, this.trackedContainer.getSavableEntity());
	}

	@Override
	public void syncSavedEntity() {
		this.savedEntity = this.trackedContainer.getSavableEntity();
	}
}
