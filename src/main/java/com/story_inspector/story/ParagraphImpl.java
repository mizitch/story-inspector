package com.story_inspector.story;

import java.util.List;

/**
 * Default implementation of {@link Paragraph}
 *
 * @author mizitch
 *
 */
public class ParagraphImpl extends BaseParentalTextNode<SentenceImpl> implements Paragraph {

	/**
	 * Creates a new instance
	 * 
	 * @param range
	 *            The range of text this paragraph covers
	 * @param sentences
	 *            The sentences within this paragraph
	 */
	public ParagraphImpl(final TextRange range, final List<SentenceImpl> sentences) {
		super(range, SentenceImpl.class, sentences);
		for (final SentenceImpl sentence : sentences) {
			sentence.setParent(this);
		}
	}

}
