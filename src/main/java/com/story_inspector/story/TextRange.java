package com.story_inspector.story;

import java.util.Set;

import org.apache.commons.lang.Validate;
import org.apache.commons.lang.builder.CompareToBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;

/**
 * Represents a range of text within the story text.
 *
 * @author mizitch
 *
 */
public class TextRange implements Comparable<TextRange> {

	private final int startIndex;
	private final int endIndex;

	/**
	 * Creates a new instance
	 *
	 * @param startIndex
	 *            Start index of the range (inclusive). Must be greater than zero and less than or equal to end index.
	 * @param endIndex
	 *            End index of the range (exclusive). Must be greater than zero and greater than or equal to start index.
	 */
	public TextRange(final int startIndex, final int endIndex) {
		super();
		Validate.isTrue(startIndex >= 0, "startIndex must be greater than zero");
		Validate.isTrue(endIndex >= startIndex, "endIndex must be greater than or equal to startIndex"); // also confirms end>0
		this.startIndex = startIndex;
		this.endIndex = endIndex;
	}

	/**
	 * Returns start index of the range (inclusive)
	 *
	 * @return Start index of the range (inclusive)
	 */
	public int getStartIndex() {
		return this.startIndex;
	}

	/**
	 * Returns end index of the range (exclusive)
	 *
	 * @return End index of the range (exclusive)
	 */
	public int getEndIndex() {
		return this.endIndex;
	}

	/**
	 * Returns length of this range
	 *
	 * @return Length of this range
	 */
	public int getLength() {
		return this.endIndex - this.startIndex;
	}

	/**
	 * Returns whether the provided position is within this range
	 *
	 * @param characterPosition
	 *            The character position to theck
	 * @return Whether the provided position is within this range
	 */
	public boolean contains(final int characterPosition) {
		return characterPosition >= this.startIndex && characterPosition < this.endIndex;
	}

	/**
	 * Returns whether the provided range is contained entirely within this range
	 *
	 * @param otherRange
	 *            The range to check
	 * @return Whether the provided range is entirely within this range
	 */
	public boolean contains(final TextRange otherRange) {
		return this.startIndex <= otherRange.startIndex && this.endIndex >= otherRange.endIndex;
	}

	/**
	 * Whether the provided range intersects this range
	 *
	 * @param otherRange
	 *            The range to check
	 * @return Whether the provided range intersects this range
	 */
	public boolean intersects(final TextRange otherRange) {
		return this.contains(otherRange.startIndex) || otherRange.contains(this.startIndex);
	}

	/**
	 * Gets selection of text covered by this range within the provided text. Throws an exception if the corpus is not long enough
	 *
	 * @param text
	 *            The text to retrieve a selection from
	 * @return The selection of text covered by this range within the provided text.
	 */
	public String getCoveredText(final String text) {
		return text.substring(this.startIndex, this.endIndex);
	}

	/**
	 * Translates the provided text range by the provided int. Will throw an exception if the range is translated so that it extends into negative
	 * numbers
	 *
	 * @param translateBy
	 *            The distance to translate this range by
	 * @return The translated range
	 */
	public TextRange translate(final int translateBy) {
		return new TextRange(this.startIndex + translateBy, this.endIndex + translateBy);
	}

	/**
	 * Returns whether the provided range intersects any of the ranges within the provided set of ranges
	 *
	 * @param range
	 *            The range to check
	 * @param rangeSet
	 *            The set of ranges to check
	 * @return Whether the provided range intersects any of the ranges within the provided set of ranges
	 */
	public static boolean intersectsRangeSet(final TextRange range, final Set<TextRange> rangeSet) {
		return rangeSet.stream().anyMatch(r -> r.intersects(range));
	}

	@Override
	public String toString() {
		return new ToStringBuilder(this).append("startIndex", this.startIndex).append("endIndex", this.endIndex).toString();
	}

	@Override
	public boolean equals(final Object other) {
		if (!(other instanceof TextRange))
			return false;
		final TextRange otherRange = (TextRange) other;

		return this.compareTo(otherRange) == 0;
	}

	@Override
	public int hashCode() {
		return new HashCodeBuilder().append(this.startIndex).append(this.endIndex).toHashCode();
	}

	@Override
	public int compareTo(final TextRange other) {
		return new CompareToBuilder().append(this.startIndex, other.startIndex).append(this.endIndex, other.endIndex).toComparison();
	}
}
