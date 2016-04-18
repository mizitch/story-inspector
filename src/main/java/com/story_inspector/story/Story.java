package com.story_inspector.story;

/**
 * Represents a complete story. Root of the {@link TextNode} hierarchy
 *
 * @author mizitch
 *
 */
public interface Story extends TextNode {

	/**
	 * Returns the title of the story, null if there is no title
	 * 
	 * @return The title of the story, null if there is no title
	 */
	public String getTitle();
}