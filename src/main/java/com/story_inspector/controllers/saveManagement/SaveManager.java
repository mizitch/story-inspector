package com.story_inspector.controllers.saveManagement;

/**
 * A module which handles saving an entity within an {@link SavableContainer}.
 *
 * Tracks whether there are unsaved changes in the entity, won't save if there aren't any. Supports prompting the user if they attempt an action that
 * will cause them to lose any unsaved changes.
 *
 * @author mizitch
 *
 * @param <T>
 *            The type of entity that is tracked/saved.
 */
public interface SaveManager<T> {

	/**
	 * Returns the {@link SavableContainer} this {@link SaveManager} is tracking.
	 *
	 * @return The {@link SavableContainer} this {@link SaveManager} is tracking.
	 */
	SavableContainer<T> getSavableContainer();

	/**
	 * Clears the tracked {@link SavableContainer} and has it represent a new entity. Will prompt the user to save if the current entity has unsaved
	 * changes.
	 *
	 * @param countsAsUnsaved
	 *            Whether the initial new entity should be marked as having unsaved changes.
	 * @param promptMessage
	 *            Message to display to the user if the current entity has unsaved changes.
	 */
	void newEntity(boolean countsAsUnsaved, String promptMessage);

	/**
	 * Clears the tracked {@link SavableContainer} and has it represent a new entity. Will not prompt the user to save even if the current entity has
	 * unsaved changes.
	 *
	 * @param countsAsUnsaved
	 *            Whether the initial new entity should be marked as having unsaved changes.
	 */
	void newEntity(boolean countsAsUnsaved);

	/**
	 * If there are no unsaved changes, this returns true.
	 *
	 * If there are unsaved changes, it informs the user that the action they are taking will result in unsaved changes being lost. The user can
	 * decide to continue without saving, save first, or abort the action. If they either successfully save, or indicate they wish to continue without
	 * saving, this method returns true to indicate that the attempted action should be executed. Otherwise it returns false.
	 *
	 * @param message
	 *            The message to display to the user if a prompt appears.
	 * @return Whether the action that required a save prompt should be executed.
	 */
	boolean promptSave(String message);

	/**
	 * Save the entity tracked by this {@link SaveManager}. If there are no unsaved changes this does nothing.
	 *
	 * @return Whether the save was successful.
	 */
	boolean save();

	/**
	 * Returns whether there are unsaved changes in the tracked entity.
	 *
	 * @return Whether there are unsaved changes in the tracked entity.
	 */
	boolean hasUnsavedChanges();

	/**
	 * Indicate that the current value in the tracked {@link SavableContainer} is saved appropriately.
	 */
	void syncSavedEntity();
}
