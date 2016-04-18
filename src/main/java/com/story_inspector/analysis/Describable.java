package com.story_inspector.analysis;

/**
 * An entity which has a name and a description.
 *
 * @author mizitch
 *
 */
public interface Describable {

	/**
	 * Returns the name of the entity.
	 *
	 * @return The name of the entity.
	 */
	public String getName();

	/**
	 * Returns the description of the entity.
	 *
	 * @return The description of the entity.
	 */
	public String getDescription();
}
