package com.story_inspector.story;

/**
 * Represents a sentence within a story
 *
 * @author mizitch
 *
 */
public interface Sentence extends TextNode {
	/**
	 * Returns whether this sentence is a fragment
	 *
	 * @return Whether this sentence is a fragment
	 */
	boolean isFragment();

	/**
	 * Returns whether this sentence is a question
	 * 
	 * @return Whether this sentence is a question
	 */
	boolean isQuestion();
}
