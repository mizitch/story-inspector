package com.story_inspector.ioProcessing;

import java.util.Collections;
import java.util.EnumMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.story_inspector.story.TextRange;

/**
 * Represents a file-type neutral version of a story document. Stores the text of the story and any information required to perform various parsing
 * and NLP activities on the story. Allows story parsing to be document file type independent.
 *
 * Not guaranteed to be immutable.
 *
 * @author mizitch
 *
 */
public interface ExtractedDocument {

	/**
	 * Class representing a file-type neutral paragraph. Contains the text of the paragraph, the paragraph "type" (based on its location within the
	 * document and other factors) and any formatting or other data that might be significant.
	 *
	 * @author mizitch
	 *
	 */
	public static class ExtractedParagraph {

		/**
		 * The type of paragraph this is within the story.
		 *
		 * @author mizitch
		 *
		 */
		public static enum ParagraphType {
			BLANK,
			SCENE_BREAK,
			TITLE,
			CHAPTER_TITLE,
			CONTACT_INFO,
			BY_LINE,
			TEXT
		};

		/**
		 * The type of formatting applied to the text.
		 *
		 * @author mizitch
		 *
		 */
		public static enum FormattingType {
			BOLD,
			ITALICS,
			UNDERLINE
		}

		private final String text;
		private final ParagraphType type;
		private final Map<FormattingType, Set<TextRange>> formattingData;
		private final Set<TextRange> quotedText;

		/**
		 * Creates a new instance.
		 *
		 * @param paragraphText
		 *            The text of the paragraph
		 * @param paragraphType
		 *            The {@link ParagraphType} of the paragraph.
		 * @param formattingData
		 *            Map containing a set of {@link TextRange}s under each {@link FormattingType}
		 */
		public ExtractedParagraph(final String paragraphText, final ParagraphType paragraphType,
				final Map<FormattingType, Set<TextRange>> formattingData) {
			this.text = paragraphText;
			this.type = paragraphType;
			this.formattingData = new EnumMap<>(FormattingType.class);
			formattingData.entrySet().stream().forEach(e -> this.formattingData.put(e.getKey(), new HashSet<TextRange>(e.getValue())));
			this.quotedText = determineQuotedText(this.text);
		}

		private Set<TextRange> determineQuotedText(final String text) {
			boolean inQuoteBlock = false;
			int startOfBlock = -1;
			final Set<TextRange> textRanges = new HashSet<TextRange>();

			for (int i = 0; i < text.length(); ++i) {
				if (isQuotationMark(text.charAt(i))) {
					if (!inQuoteBlock) {
						inQuoteBlock = true;
						startOfBlock = i;
					} else {
						textRanges.add(new TextRange(startOfBlock, i + 1));
						inQuoteBlock = false;
						startOfBlock = -1;
					}
				}
			}

			// Standard practice for multi-paragraph quotes is to end paragraph without closing quote
			// And for the next paragraph to open with another quotation mark
			// So if we end with an unclosed quote, we'll just quote to the end of the paragraph
			if (inQuoteBlock) {
				textRanges.add(new TextRange(startOfBlock, text.length()));
			}

			return textRanges;
		}

		private boolean isQuotationMark(final char charAt) {
			return "\"“”".contains(String.valueOf(charAt));
		}

		/**
		 * Returns the text of this paragraph.
		 *
		 * @return The text of this paragraph.
		 */
		public String getText() {
			return this.text;
		}

		/**
		 * Returns the {@link ParagraphType} of this paragraph.
		 *
		 * @return The {@link ParagraphType} of this paragraph.
		 */
		public ParagraphType getType() {
			return this.type;
		}

		/**
		 * Returns a set of {@link TextRange}s covered by the provided {@link FormattingType} within this paragraph.
		 *
		 * @param formattingType
		 *            The {@link FormattingType} to retrieve the covered range for.
		 * @return A set of {@link TextRange}s covered by the provided {@link FormattingType} within this paragraph.
		 */
		public Set<TextRange> getFormattedText(final FormattingType formattingType) {
			return Collections.unmodifiableSet(this.formattingData.get(formattingType));
		}

		/**
		 * Returns the set of {@link TextRange}s that are considered to be "quoted" within this paragraph.
		 *
		 * @return The set of {@link TextRange}s that are considered to be "quoted" within this paragraph.
		 */
		public Set<TextRange> getQuotedText() {
			return Collections.unmodifiableSet(this.quotedText);
		}
	}

	/**
	 * Returns a list of this document's {@link ExtractedParagraph}s
	 *
	 * @return A list of this document's {@link ExtractedParagraph}s
	 */
	public List<ExtractedParagraph> getParagraphs();
}
