package com.story_inspector.story;

import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.lang.Validate;
import org.apache.commons.lang.builder.ToStringBuilder;

/**
 * Base implementation of a {@link TextNode} that has children
 *
 * @author mizitch
 *
 * @param <ChildType>
 *            Type of this node type's direct children
 */
abstract class BaseParentalTextNode<ChildType extends TextNode> implements TextNode {
	private final List<ChildType> children;
	private final Class<ChildType> childType;
	private TextNode parent;
	private final TextRange range;

	/**
	 * Creates a new instance
	 *
	 * @param range
	 *            Range this node covers
	 * @param childType
	 *            The type of this node type's direct children
	 * @param children
	 *            The direct children of this node
	 */
	BaseParentalTextNode(final TextRange range, final Class<ChildType> childType, final List<ChildType> children) {
		validateChildren(range, children);

		this.childType = childType;
		this.range = range;
		this.children = new ArrayList<ChildType>(children);
	}

	/**
	 * Gets the type of this node's direct children
	 *
	 * @return
	 */
	public Class<ChildType> getChildType() {
		return this.childType;
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

	/**
	 * Returns all children of this {@link TextNode} at the provided level.
	 *
	 * @throws IllegalArgumentException
	 *             If the provided class is not below the level of this {@link TextNode}.
	 */
	@Override
	@SuppressWarnings("unchecked")
	public <T extends TextNode> List<T> getChildrenAtLevelIntersectingRange(final Class<T> levelClass, final TextRange range) {
		if (levelClass.isAssignableFrom(this.childType)) {
			return (List<T>) Collections
					.unmodifiableList(this.children.stream().filter(c -> c.getRange().intersects(range)).collect(Collectors.toList()));
		} else {
			return Collections.unmodifiableList(this.children.stream()
					.flatMap(child -> child.getChildrenAtLevelIntersectingRange(levelClass, range).stream()).collect(Collectors.toList()));
		}
	}

	/**
	 * Sets the parent of this node
	 *
	 * @param parent
	 *            The parent of this node
	 */
	void setParent(final TextNode parent) {
		this.parent = parent;
	}

	/**
	 * Validates that the provided direct children have ranges that completely cover the provided range and are in the correct order.
	 */
	private void validateChildren(final TextRange range, final List<ChildType> immediateChildren) {
		Validate.noNullElements(immediateChildren);
		int position = range.getStartIndex();
		for (final ChildType c : immediateChildren) {
			Validate.isTrue(position == c.getRange().getStartIndex(), "children of text node must cover range of text node exactly");
			position = c.getRange().getEndIndex();
		}
		Validate.isTrue(range.getEndIndex() == position, "children of text node must cover range of entire text node");
	}

	@Override
	public String toString() {
		return new ToStringBuilder(this).append("range", this.range).toString();
	}
}
