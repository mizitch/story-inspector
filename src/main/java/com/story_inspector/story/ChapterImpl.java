package com.story_inspector.story;

import java.util.List;

/**
 * Default implementation of {@link Chapter}.
 *
 * @author mizitch
 *
 */
public class ChapterImpl extends BaseParentalTextNode<SceneImpl> implements Chapter {
	private final String title;

	/**
	 * Creates a new instance
	 * 
	 * @param range
	 *            The range this node covers
	 * @param scenes
	 *            The scenes in this chapter
	 * @param title
	 *            The title of this chapter
	 */
	public ChapterImpl(final TextRange range, final List<SceneImpl> scenes, final String title) {
		super(range, SceneImpl.class, scenes);
		for (final SceneImpl scene : scenes) {
			scene.setParent(this);
		}
		this.title = title;
	}

	@Override
	public String getTitle() {
		return this.title;
	}

}
