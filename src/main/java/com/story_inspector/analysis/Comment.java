package com.story_inspector.analysis;

import org.apache.commons.lang.Validate;
import org.apache.commons.lang.builder.CompareToBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;

import com.story_inspector.story.Story;
import com.story_inspector.story.TextRange;

/**
 * A note regarding a selection of a {@link Story} made by an {@link Analyzer}.
 *
 * @author mizitch
 *
 */
public class Comment implements Comparable<Comment> {
	private final Analyzer<?> analyzer;
	private final String content;
	private final TextRange selection;

	/**
	 * Create a new instance.
	 *
	 * @param analyzer
	 *            The {@link Analyzer} that produced this comment.
	 * @param content
	 *            The {@link String} content of this comment.
	 * @param selection
	 *            The {@link TextRange} this comment is about.
	 */
	public Comment(final Analyzer<?> analyzer, final String content, final TextRange selection) {
		super();
		Validate.notNull(analyzer);
		Validate.notEmpty(content);
		Validate.notNull(selection);

		this.analyzer = analyzer;
		this.content = content;
		this.selection = selection;
	}

	/**
	 * Returns the {@link Analyzer} that produced this.
	 *
	 * @return The {@link Analyzer} that produced this.
	 */
	public Analyzer<?> getAnalyzer() {
		return this.analyzer;
	}

	/**
	 * Returns the {@link String} content of this comment.
	 *
	 * @return The {@link String} content of this comment.
	 */
	public String getContent() {
		return this.content;
	}

	/**
	 * Returns the {@link TextRange} this comment is about.
	 *
	 * @return The {@link TextRange} this comment is about.
	 */
	public TextRange getSelection() {
		return this.selection;
	}

	@Override
	public int compareTo(final Comment other) {
		return new CompareToBuilder().append(this.selection, other.selection).append(this.analyzer, other.analyzer)
				.append(this.content, other.content).toComparison();
	}

	@Override
	public boolean equals(final Object other) {
		if (!(other instanceof Comment))
			return false;
		final Comment otherComment = (Comment) other;

		return this.compareTo(otherComment) == 0;
	}

	@Override
	public int hashCode() {
		return new HashCodeBuilder().append(this.selection).append(this.content).append(this.analyzer.extractAnalyzerSpec()).toHashCode();
	}
}
