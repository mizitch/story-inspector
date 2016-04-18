package com.story_inspector.story;

import java.util.List;

/**
 * Default implementation of {@link Scene}
 *
 * @author mizitch
 *
 */
public class SceneImpl extends BaseParentalTextNode<ParagraphImpl> implements Scene {

	/**
	 * Creates a new instance
	 * 
	 * @param range
	 *            The range of text this node covers
	 * @param paragraphs
	 *            The paragraphs within this scene
	 */
	public SceneImpl(final TextRange range, final List<ParagraphImpl> paragraphs) {
		super(range, ParagraphImpl.class, paragraphs);
		for (final ParagraphImpl paragraph : paragraphs) {
			paragraph.setParent(this);
		}
	}

}
