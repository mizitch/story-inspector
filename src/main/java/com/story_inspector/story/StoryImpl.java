package com.story_inspector.story;

import java.io.IOException;
import java.io.Writer;
import java.util.List;

/**
 * Default implementation of {@link Story}.
 *
 * @author mizitch
 *
 */
public class StoryImpl extends BaseParentalTextNode<ChapterImpl> implements Story {
	private final String title;
	private final String text;

	/**
	 * Creates a new instance
	 *
	 * @param text
	 *            The text of this story
	 * @param title
	 *            The title of this story
	 * @param chapters
	 *            The chapters of this story
	 */
	public StoryImpl(final String text, final String title, final List<ChapterImpl> chapters) {
		super(new TextRange(0, text.length()), ChapterImpl.class, chapters);
		this.text = text;
		this.title = title;
		for (final ChapterImpl chapter : chapters) {
			chapter.setParent(this);
		}
	}

	@Override
	public void write(final Writer writer, final TextRange range) throws IOException {
		writer.write(this.text.substring(range.getStartIndex(), range.getEndIndex()));
	}

	@Override
	public TextRange getRange() {
		return new TextRange(0, this.text.length());
	}

	@Override
	public String getSelection(final TextRange range) {
		return this.text.substring(range.getStartIndex(), range.getEndIndex());
	}

	@Override
	public String getTitle() {
		return this.title;
	}
}
