package com.story_inspector.story;

import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.util.List;

/**
 * Represents a node within a {@link Story} that contains text.
 *
 * @author mizitch
 *
 */
public interface TextNode {

	/**
	 * Writes the provided range of this text to the provided writer.
	 *
	 * @param writer
	 *            The {@link Writer} to write to
	 * @param range
	 *            The {@link TextRange} of text to write
	 * @throws IOException
	 *             If there is an IO error while writing
	 */
	public void write(Writer writer, TextRange range) throws IOException;

	/**
	 * Writes the text in this node to the provided {@link Writer}.
	 *
	 * @param writer
	 *            The {@link Writer} to write to
	 * @throws IOException
	 *             If there is an IO error while writing
	 */
	public default void write(final Writer writer) throws IOException {
		write(writer, getRange());
	}

	/**
	 * Returns the text this node contains
	 *
	 * @return The text this node contains
	 */
	public default String getText() {
		final StringWriter sw = new StringWriter(getRange().getLength());
		try {
			write(sw);
		} catch (final IOException e) {
			throw new RuntimeException(e);
		}
		return sw.toString();
	}

	/**
	 * Returns the parent of this node, null if it has no parent
	 *
	 * @return The parent of this node, null if it has no parent
	 */
	public TextNode getParent();

	/**
	 * Returns the {@link TextRange} this node covers
	 *
	 * @return The {@link TextRange} this node covers
	 */
	public TextRange getRange();

	/**
	 * Extracts the selection of this text that falls within the provided {@link TextRange}
	 *
	 * @param range
	 *            The range to extract
	 * @return The selection of this text that falls within the provided {@link TextRange}
	 */
	public String getSelection(TextRange range);

	/**
	 * Returns all children of this {@link TextNode} at the provided level.
	 *
	 * @throws IllegalArgumentException
	 *             If the provided class is not below the level of this {@link TextNode}.
	 */
	public default <T extends TextNode> List<T> getChildrenAtLevel(final Class<T> levelClass) {
		return getChildrenAtLevelIntersectingRange(levelClass, getRange());
	}

	/**
	 * Returns all children of this {@link TextNode} at the provided level which intersect the provided range.
	 *
	 * @throws IllegalArgumentException
	 *             If the provided class is not below the level of this {@link TextNode}.
	 */
	public <T extends TextNode> List<T> getChildrenAtLevelIntersectingRange(Class<T> levelClass, TextRange range);
}