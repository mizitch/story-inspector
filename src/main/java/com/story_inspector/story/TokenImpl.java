package com.story_inspector.story;

import java.io.IOException;
import java.io.Writer;
import java.util.List;

import org.apache.commons.lang.Validate;
import org.apache.commons.lang.builder.ToStringBuilder;

/**
 * Default implementation of {@link Token}
 *
 * @author mizitch
 *
 */
public class TokenImpl implements Token {
	private final TextRange range;
	private Sentence parent;
	private final String posTag;
	private final String wordStem;
	private final String word;
	private final boolean bold, italics, underline, quoted, allCaps, isWord;

	/**
	 * Creates a new instance
	 *
	 * @param range
	 *            The range this token covers
	 * @param text
	 *            The text of this token
	 * @param posTag
	 *            The part of speech tag of this token
	 * @param wordStem
	 *            The word stem of this token (if it is a word, null otherwise)
	 * @param bold
	 *            Whether this token is bold
	 * @param italics
	 *            Whether this token is italicized
	 * @param underline
	 *            Whether this token is underlined
	 * @param quoted
	 *            Whether this token is quoted
	 */
	public TokenImpl(final TextRange range, final String text, final String posTag, final String wordStem, final boolean bold, final boolean italics,
			final boolean underline, final boolean quoted) {
		this.range = range;
		this.posTag = posTag;
		this.wordStem = wordStem;
		this.bold = bold;
		this.italics = italics;
		this.underline = underline;
		this.quoted = quoted;

		// Initialize text based attributes
		this.word = extractWord(text);
		this.isWord = this.word != null;
		this.allCaps = this.isWord && this.word.chars().noneMatch(c -> Character.isLowerCase(c))
				&& this.word.chars().anyMatch(c -> Character.isAlphabetic(c));
	}

	/**
	 * Strips leading and trailing non-alphanumeric characters. Should get rid of spaces, quotes, etc.
	 *
	 * @param text
	 * @return
	 */
	private String extractWord(final String text) {
		int wordStart = text.length();
		for (int i = 0; i < text.length(); ++i) {
			if (Character.isLetterOrDigit(text.charAt(i))) {
				wordStart = i;
				break;
			}
		}

		int wordEnd = 0;
		for (int i = text.length() - 1; i >= 0; --i) {
			if (Character.isLetterOrDigit(text.charAt(i))) {
				wordEnd = i + 1;
				break;
			}
		}

		if (wordEnd > wordStart)
			return text.substring(wordStart, wordEnd);
		else
			return null;
	}

	@Override
	public void write(final Writer writer, final TextRange range) throws IOException {
		Validate.isTrue(this.range.contains(range), "Input range must fall within text node range");
		this.parent.write(writer, range);
	}

	@Override
	public TextNode getParent() {
		return this.parent;
	}

	@Override
	public TextRange getRange() {
		return this.range;
	}

	@Override
	public String getSelection(final TextRange range) {
		Validate.isTrue(this.range.contains(range), "Input range must fall within text node range");
		return this.parent.getSelection(range);
	}

	@Override
	public <T extends TextNode> List<T> getChildrenAtLevelIntersectingRange(final Class<T> levelClass, final TextRange range) {
		throw new IllegalArgumentException("No children at level: " + levelClass);
	}

	@Override
	public boolean isQuoted() {
		return this.quoted;
	}

	@Override
	public String getWordStem() {
		return this.wordStem;
	}

	@Override
	public String getWord() {
		return this.word;
	}

	@Override
	public String getPartOfSpeechTag() {
		return this.posTag;
	}

	void setParent(final Sentence parent) {
		this.parent = parent;
	}

	@Override
	public boolean isWord() {
		return this.isWord;
	}

	@Override
	public boolean isUnderlined() {
		return this.underline;
	}

	@Override
	public boolean isItalicized() {
		return this.italics;
	}

	@Override
	public boolean isBold() {
		return this.bold;
	}

	@Override
	public boolean isAllCaps() {
		return this.allCaps;
	}

	@Override
	public String toString() {
		return new ToStringBuilder(this).append("range", this.range).toString();
	}
}
