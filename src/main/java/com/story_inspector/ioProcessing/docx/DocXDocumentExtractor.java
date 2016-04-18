package com.story_inspector.ioProcessing.docx;

import java.io.InputStream;
import java.util.Arrays;
import java.util.Collections;
import java.util.EnumMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import javax.xml.bind.JAXBElement;

import org.apache.commons.lang.StringUtils;
import org.docx4j.openpackaging.exceptions.Docx4JException;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.openpackaging.parts.WordprocessingML.MainDocumentPart;
import org.docx4j.wml.BooleanDefaultTrue;
import org.docx4j.wml.CTTrackChange;
import org.docx4j.wml.CommentRangeEnd;
import org.docx4j.wml.CommentRangeStart;
import org.docx4j.wml.ContentAccessor;
import org.docx4j.wml.DelText;
import org.docx4j.wml.Jc;
import org.docx4j.wml.JcEnumeration;
import org.docx4j.wml.P;
import org.docx4j.wml.PPr;
import org.docx4j.wml.R;
import org.docx4j.wml.RPr;
import org.docx4j.wml.Style;
import org.docx4j.wml.Text;
import org.docx4j.wml.U;
import org.docx4j.wml.UnderlineEnumeration;
import org.springframework.stereotype.Component;

import com.story_inspector.ioProcessing.DocumentExtractor;
import com.story_inspector.ioProcessing.ExtractedDocument;
import com.story_inspector.ioProcessing.ExtractedDocument.ExtractedParagraph;
import com.story_inspector.ioProcessing.ExtractedDocument.ExtractedParagraph.FormattingType;
import com.story_inspector.ioProcessing.ExtractedDocument.ExtractedParagraph.ParagraphType;
import com.story_inspector.ioProcessing.StoryIOException;
import com.story_inspector.ioProcessing.StoryIOException.ProcessingExceptionType;
import com.story_inspector.progress.ProgressMonitor;
import com.story_inspector.progress.TaskCanceledException;
import com.story_inspector.story.TextRange;

/**
 * {@link DocumentExtractor} implementation for DOCX files. Uses docx4j
 *
 * @author mizitch
 *
 */
@Component
public class DocXDocumentExtractor implements DocumentExtractor {

	/**
	 * Stages of document extraction.
	 *
	 * @author mizitch
	 *
	 */
	private static enum DocumentStage {
		NOT_STARTED(0),
		CONTACT_INFO(1),
		PRE_TITLE_BLANK(2),
		TITLE(3),
		BY_LINE(4),
		POST_TITLE_BLANK(5),
		TEXT(6),
		PRE_CHAPTER_TITLE_BLANK(7),
		CHAPTER_TITLE(8),
		POST_CHAPTER_TITLE_BLANK(9);

		private final int priority;

		private DocumentStage(final int priority) {
			this.priority = priority;
		}
	};

	/**
	 * Represents the current state of the extraction.
	 *
	 * @author mizitch
	 *
	 */
	private class DocumentStageHolder {
		private DocumentStage currentStage;

		DocumentStageHolder() {
			this.currentStage = DocumentStage.NOT_STARTED;
		}
	}

	@Override
	public ExtractedDocument extractDocument(final InputStream inputStream, final ProgressMonitor progressReporter)
			throws StoryIOException, TaskCanceledException {

		progressReporter.reportProgress(0.0f, "Parsing document file");

		WordprocessingMLPackage docPackage;
		// Get main document and raw paragraphs
		try {
			docPackage = WordprocessingMLPackage.load(inputStream);
		} catch (final Docx4JException e) {
			throw new StoryIOException(ProcessingExceptionType.CORRUPT_DOCUMENT, "File is not a valid .docx document", e);
		}

		final MainDocumentPart mainDoc = docPackage.getMainDocumentPart();
		final List<Object> paragraphs = mainDoc.getContent();

		progressReporter.reportProgress(0.85f, "Validating parsed file");
		// Validate there is not already revision data in the document
		validateNoRevisionData(mainDoc);

		progressReporter.reportProgress(0.9f, "Analyzing paragraphs");
		// Initialize a map of the document's styles
		final Map<String, Style> styleMap = generateStyleMap(mainDoc);

		// Initialize an initial metadata object to track the progress of the extraction
		final DocumentStageHolder stageHolder = new DocumentStageHolder();
		stageHolder.currentStage = DocumentStage.NOT_STARTED;

		final List<ExtractedParagraph> paragraphDataList = paragraphs.stream().map(o -> {
			final P paragraph = (P) o;

			// Get paragraph text
			final StringBuilder paragraphTextBuilder = new StringBuilder();
			final Map<FormattingType, Set<TextRange>> formattingData = new EnumMap<>(FormattingType.class);
			Arrays.stream(FormattingType.values()).forEach(ft -> formattingData.put(ft, new HashSet<>()));
			extractTextFromParagraph(paragraphTextBuilder, formattingData, paragraph, paragraph.getPPr(), styleMap, mainDoc);
			final String paragraphText = paragraphTextBuilder.toString();

			// Get paragraph type
			final ParagraphType type = getParagraphType(paragraph, paragraphText, stageHolder, styleMap, mainDoc);

			// Append extra space if necessary to end of text paragraphs
			if (type == ParagraphType.TEXT && paragraphTextBuilder.charAt(paragraphTextBuilder.length() - 1) != ' ')
				paragraphTextBuilder.append(' ');

			// Return tuple of source paragraph element, text and type
			return new ExtractedParagraph(paragraphTextBuilder.toString(), type, formattingData);
		}).collect(Collectors.toList());

		progressReporter.reportProgress(1.0f, "Complete");
		return new DocXExtractedDocument(docPackage, paragraphDataList);
	}

	/**
	 * Validates that document does not contain any revision data. This includes comments, text additions or text additions.
	 */
	private void validateNoRevisionData(final MainDocumentPart mainDoc) throws StoryIOException {
		if (mainDoc.getCommentsPart() != null && mainDoc.getCommentsPart().getJaxbElement() != null
				&& mainDoc.getCommentsPart().getJaxbElement().getComment() != null
				&& !mainDoc.getCommentsPart().getJaxbElement().getComment().isEmpty()) {
			throw new StoryIOException(ProcessingExceptionType.INCLUDES_REVISION_DATA, "File already contains comments");
		}
		for (final Object node : mainDoc.getContent()) {
			if (containsNodeTypes(node, DelText.class, CTTrackChange.class, CommentRangeStart.class, CommentRangeEnd.class)) {
				throw new StoryIOException(ProcessingExceptionType.INCLUDES_REVISION_DATA, "File already contains revision data");
			}
		}
	}

	/**
	 * Checks whether the given node contains any of the provided node types in its tree.
	 */
	private boolean containsNodeTypes(final Object node, final Class<?>... nodeTypes) {
		for (final Class<?> nodeType : nodeTypes)
			if (nodeType.isInstance(node))
				return false;

		if (node instanceof ContentAccessor)
			return ((ContentAccessor) node).getContent().stream().anyMatch(n -> containsNodeTypes(n, nodeTypes));

		return false;
	}

	/**
	 * Gets the {@link ParagraphType} of the provided paragraph.
	 *
	 * @param paragraph
	 *            The paragraph to get the type of.
	 * @param paragraphText
	 *            The text of the paragraph
	 * @param stageHolder
	 *            Holder of the current {@link DocumentStage}. Updated by this method
	 * @param styleMap
	 *            Extracted document styles
	 * @param mainDocument
	 *            Main document object
	 * @return The {@link ParagraphType} of this paragraph
	 */
	private ParagraphType getParagraphType(final P paragraph, final String paragraphText, final DocumentStageHolder stageHolder,
			final Map<String, Style> styleMap, final MainDocumentPart mainDocument) {
		if (StringUtils.isBlank(paragraphText.trim())) {
			// Blank text, just need to decide what stage of blank text...
			if (stageHolder.currentStage.priority <= DocumentStage.PRE_TITLE_BLANK.priority)
				stageHolder.currentStage = DocumentStage.PRE_TITLE_BLANK;
			else if (stageHolder.currentStage.priority <= DocumentStage.POST_TITLE_BLANK.priority)
				stageHolder.currentStage = DocumentStage.POST_TITLE_BLANK;
			else if (stageHolder.currentStage.priority <= DocumentStage.PRE_CHAPTER_TITLE_BLANK.priority)
				stageHolder.currentStage = DocumentStage.PRE_CHAPTER_TITLE_BLANK;
			else if (stageHolder.currentStage.priority <= DocumentStage.POST_CHAPTER_TITLE_BLANK.priority)
				stageHolder.currentStage = DocumentStage.POST_CHAPTER_TITLE_BLANK;
			return ParagraphType.BLANK;
		} else if (paragraphText.trim().equals("#")) {
			// This is definitely a scene break, which means we are going right to text stage, no matter what we've seen so far
			stageHolder.currentStage = DocumentStage.TEXT;
			return ParagraphType.SCENE_BREAK;
		} else if (isParagraphCentered(paragraph, mainDocument, styleMap)) {
			// Centered, so we are going to assume some kind of title rather
			// than text.
			if (stageHolder.currentStage.priority < DocumentStage.TITLE.priority) {
				// Haven't seen a main title yet, so this is the main title!
				stageHolder.currentStage = DocumentStage.TITLE;
				return ParagraphType.TITLE;
			} else if (stageHolder.currentStage == DocumentStage.TITLE) {
				// We've just seen part of the main title, so is this the author's by line?
				if (paragraphText.toLowerCase().contains("by ")) {
					stageHolder.currentStage = DocumentStage.BY_LINE;
					return ParagraphType.BY_LINE;
				}
				// Or is it a multi-line title?
				else {
					stageHolder.currentStage = DocumentStage.TITLE;
					return ParagraphType.TITLE;
				}
			} else { // We've already finished the main title, so this must be some kind of chapter title
				stageHolder.currentStage = DocumentStage.CHAPTER_TITLE;
				return ParagraphType.CHAPTER_TITLE;
			}
		} else if (stageHolder.currentStage.priority < DocumentStage.TITLE.priority) {
			// This is non-blank, not centered and not a scene break. If we
			// haven't yet seen the title, going to assume it is contact info
			// TODO: if doc doesn't include a title, entire story will be
			// classified as contact info
			stageHolder.currentStage = DocumentStage.CONTACT_INFO;
			return ParagraphType.CONTACT_INFO;
		} else {
			// We have seen the title already and this is not centered, so going to assume it is text!
			stageHolder.currentStage = DocumentStage.TEXT;
			return ParagraphType.TEXT;
		}
	}

	/**
	 * Extract text from the paragraph and append it to the provided {@link StringBuilder}.
	 *
	 * @param stringBuilder
	 *            The {@link StringBuilder} to append text to.
	 * @param formattingData
	 *            Formatting data map to update with ranges of paragraph that are formatted.
	 * @param paragraph
	 *            The docx4j paragraph object
	 * @param paragraphProperties
	 *            The docx4j paragraph properties
	 * @param styleMap
	 *            The extracted styles from the main document
	 * @param mainDoc
	 *            The main document object
	 */
	private void extractTextFromParagraph(final StringBuilder stringBuilder, final Map<FormattingType, Set<TextRange>> formattingData,
			final P paragraph, final PPr paragraphProperties, final Map<String, Style> styleMap, final MainDocumentPart mainDoc) {
		for (final Object paragraphChild : paragraph.getContent()) {
			if (paragraphChild instanceof R) {
				final R run = (R) paragraphChild;
				for (final Object runChild : run.getContent()) {
					if (runChild instanceof JAXBElement && ((JAXBElement<?>) runChild).getDeclaredType() == Text.class) {
						final String childText = ((Text) ((JAXBElement<?>) runChild).getValue()).getValue();
						final TextRange childRange = new TextRange(stringBuilder.length(), stringBuilder.length() + childText.length());

						stringBuilder.append(childText);
						extractFormattingData(run, childRange, formattingData, paragraphProperties, styleMap, mainDoc);
					}
				}
			}
		}
	}

	/**
	 * Updates the provided formatting data map based on the provided data about the text run.
	 *
	 * @param run
	 *            The provided text run
	 * @param childRange
	 *            The text range this run covers
	 * @param formattingData
	 *            The formatting data map to update
	 * @param paragraphProperties
	 *            The docx4j paragraph properties object
	 * @param styleMap
	 *            The extracted styles of the main document.
	 * @param mainDoc
	 *            The main document object
	 */
	private void extractFormattingData(final R run, final TextRange childRange, final Map<FormattingType, Set<TextRange>> formattingData,
			final PPr paragraphProperties, final Map<String, Style> styleMap, final MainDocumentPart mainDoc) {
		final RPr runProperties = run.getRPr();

		final BooleanDefaultTrue isBold = getRunProperty(runProperties, paragraphProperties, RPr::getB, styleMap, mainDoc);
		final BooleanDefaultTrue isItalics = getRunProperty(runProperties, paragraphProperties, RPr::getI, styleMap, mainDoc);
		final U underlining = getRunProperty(runProperties, paragraphProperties, RPr::getU, styleMap, mainDoc);

		if (isBold != null && isBold.isVal()) {
			formattingData.get(FormattingType.BOLD).add(childRange);
		}

		if (isItalics != null && isItalics.isVal()) {
			formattingData.get(FormattingType.ITALICS).add(childRange);
		}

		if (underlining != null && underlining.getVal() != UnderlineEnumeration.NONE) {
			formattingData.get(FormattingType.UNDERLINE).add(childRange);
		}
	}

	/**
	 * Returns whether this paragraph is center aligned.
	 */
	private boolean isParagraphCentered(final P paragraph, final MainDocumentPart mainDoc, final Map<String, Style> styleMap) {
		final Jc alignment = getParagraphProperty(paragraph.getPPr(), PPr::getJc, styleMap, mainDoc);
		if (alignment == null)
			return false;
		else
			return alignment.getVal() == JcEnumeration.CENTER;
	}

	/**
	 * Extracts styles from main document and returns a map from style id to style object.
	 */
	private Map<String, Style> generateStyleMap(final MainDocumentPart mainDoc) {
		return mainDoc.getStyleDefinitionsPart().getJaxbElement().getStyle().stream().collect(Collectors.toMap(s -> s.getStyleId(), s -> s));
	}

	/**
	 * Retrieves a property for a paragraph. Checks local paragraph properties, then falls back on docx style hierarchy.
	 *
	 * @param paragraphProperties
	 *            Local paragraph properties
	 * @param propertyGetter
	 *            Function to retrieve the desired paragraph property
	 * @param styleMap
	 *            Styles extracted from the main document
	 * @param mainDoc
	 *            Main document object
	 * @return The retrieved property
	 */
	private <T> T getParagraphProperty(final PPr paragraphProperties, final Function<PPr, T> propertyGetter, final Map<String, Style> styleMap,
			final MainDocumentPart mainDoc) {

		// check local paragraph properties
		final T localResult = propertyGetter.apply(paragraphProperties);
		if (localResult != null) {
			return localResult;
		}

		// check paragraph style
		if (paragraphProperties.getPStyle() != null) {
			final Style paragraphStyle = styleMap.get(paragraphProperties.getPStyle().getVal());
			final T pStyleResult = getParagraphPropertyFromStyle(paragraphStyle, propertyGetter, styleMap);
			if (pStyleResult != null) {
				return pStyleResult;
			}
		}

		// Check main document default paragraph style
		if (mainDoc.getStyleDefinitionsPart().getDefaultParagraphStyle() != null
				&& mainDoc.getStyleDefinitionsPart().getDefaultParagraphStyle().getPPr() != null) {
			final T paragraphStyleResult = propertyGetter.apply(mainDoc.getStyleDefinitionsPart().getDefaultParagraphStyle().getPPr());
			if (paragraphStyleResult != null) {
				return paragraphStyleResult;
			}
		}

		// Well, no choices left so...
		return null;
	}

	/**
	 * Retrieves a property for a run. Checks local run properties, paragraph run properties, then falls back on docx style hierarchy.
	 *
	 * @param runProperties
	 *            Local run properties
	 * @param paragraphProperties
	 *            Local paragraph properties
	 * @param propertyGetter
	 *            Function to retrieve the desired run property
	 * @param styleMap
	 *            Styles extracted from the main document
	 * @param mainDoc
	 *            Main document object
	 * @return The retrieved property
	 */
	private <T> T getRunProperty(final RPr runProperties, final PPr paragraphProperties, final Function<RPr, T> propertyGetter,
			final Map<String, Style> styleMap, final MainDocumentPart mainDoc) {
		// Check local run properties
		if (runProperties != null) {
			final T localRunResult = propertyGetter.apply(runProperties);
			if (localRunResult != null) {
				return localRunResult;
			}

			// Check style set on run element
			if (runProperties.getRStyle() != null) {
				final Style runStyle = styleMap.get(runProperties.getRStyle().getVal());
				final T runStyleResult = getRunPropertyFromStyle(runStyle, propertyGetter, styleMap);
				if (runStyleResult != null) {
					return runStyleResult;
				}
			}
		}

		// Check style set on paragraph element
		if (paragraphProperties != null && paragraphProperties.getPStyle() != null) {
			final Style paragraphStyle = styleMap.get(paragraphProperties.getPStyle().getVal());
			final T pStyleResult = getRunPropertyFromStyle(paragraphStyle, propertyGetter, styleMap);
			if (pStyleResult != null) {
				return pStyleResult;
			}
		}

		// Check main document character style
		if (mainDoc.getStyleDefinitionsPart().getDefaultCharacterStyle() != null
				&& mainDoc.getStyleDefinitionsPart().getDefaultCharacterStyle().getRPr() != null) {
			final T characterStyleResult = propertyGetter.apply(mainDoc.getStyleDefinitionsPart().getDefaultCharacterStyle().getRPr());
			if (characterStyleResult != null) {
				return characterStyleResult;
			}
		}

		// Check main document paragraph style
		if (mainDoc.getStyleDefinitionsPart().getDefaultParagraphStyle() != null
				&& mainDoc.getStyleDefinitionsPart().getDefaultParagraphStyle().getRPr() != null) {
			final T paragraphStyleResult = propertyGetter.apply(mainDoc.getStyleDefinitionsPart().getDefaultParagraphStyle().getRPr());
			if (paragraphStyleResult != null) {
				return paragraphStyleResult;
			}
		}

		// Welp, guess it's really not defined
		return null;
	}

	/**
	 * Gets paragraph property from provided style object.
	 */
	private <T> T getParagraphPropertyFromStyle(final Style paragraphStyle, final Function<PPr, T> propertyGetter,
			final Map<String, Style> styleMap) {
		if (paragraphStyle.getPPr() != null && propertyGetter.apply(paragraphStyle.getPPr()) != null) {
			return propertyGetter.apply(paragraphStyle.getPPr());
		} else if (paragraphStyle.getBasedOn() != null) {
			return getParagraphPropertyFromStyle(styleMap.get(paragraphStyle.getBasedOn().getVal()), propertyGetter, styleMap);
		} else {
			return null;
		}
	}

	/**
	 * Gets run property from provided style object.
	 */
	private <T> T getRunPropertyFromStyle(final Style runStyle, final Function<RPr, T> propertyGetter, final Map<String, Style> styleMap) {
		if (runStyle.getRPr() != null && propertyGetter.apply(runStyle.getRPr()) != null) {
			return propertyGetter.apply(runStyle.getRPr());
		} else if (runStyle.getBasedOn() != null) {
			return getRunPropertyFromStyle(styleMap.get(runStyle.getBasedOn().getVal()), propertyGetter, styleMap);
		} else {
			return null;
		}
	}

	@Override
	public Set<String> getSupportedFileTypes() {
		return Collections.singleton("DOCX");
	}

}
