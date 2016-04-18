package com.story_inspector.story;

/**
 * Represents a story chapter.
 *
 * @author mizitch
 *
 */
public interface Chapter extends TextNode {
	/**
	 * Returns the title of the chapter, null if the chapter has no title
	 *
	 * @return The title of the chapter, null if the chapter has no title.
	 */
	public String getTitle();
}
