package com.story_inspector.story;

/**
 * Represents a token (word or other smallest text unit within a story)
 *
 * @author mizitch
 *
 */
public interface Token extends TextNode {
	/**
	 * Returns whether this token is quoted
	 *
	 * @return Whether this token is quoted
	 */
	public boolean isQuoted();

	/**
	 * Returns whether this token is a word
	 *
	 * @return Whether this token is a word
	 */
	public boolean isWord();

	/**
	 * Returns whether this token is underlined
	 *
	 * @return Whether this token is underlined
	 */
	public boolean isUnderlined();

	/**
	 * Returns whether this token is italicized
	 *
	 * @return Whether this token is italicized
	 */
	public boolean isItalicized();

	/**
	 * Returns whether this token is bold
	 *
	 * @return Whether this token is bold
	 */
	public boolean isBold();

	/**
	 * Returns whether this token is in all caps
	 *
	 * @return Whether this token is in all caps
	 */
	public boolean isAllCaps();

	/**
	 * Returns the stem of this word (if it is a word, otherwise null)
	 *
	 * @return The stem of this word (if it is a word, otherwise null)
	 */
	public String getWordStem();

	/**
	 * Returns the word component of this token (if this token is a word, otherwise null)
	 *
	 * @return The word component of this token (if this token is a word, otherwise null)
	 */
	public String getWord();

	/**
	 * Returns the part of speech tag for this token
	 * 
	 * @return The part of speech tag for this token
	 */
	public String getPartOfSpeechTag();
}
