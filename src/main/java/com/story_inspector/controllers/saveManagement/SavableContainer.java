package com.story_inspector.controllers.saveManagement;

/**
 * A container that contains a savable entity. Meant to be tracked by a {@link SaveManager}.
 *
 * @author mizitch
 *
 * @param <T>
 *            The type of the savable entity.
 */
public interface SavableContainer<T> {

	/**
	 * Returns the current value of the savable entity.
	 *
	 * @return The current value of the savable entity.
	 */
	public T getSavableEntity();

	/**
	 * Set the container's entity to the provided value.
	 *
	 * @param newValue
	 *            The value to set the container's entity to.
	 */
	public void setSavableEntity(T newValue);

	/**
	 * Clear the container's current entity and set it to a default new entity.
	 */
	public void newEntity();
}
