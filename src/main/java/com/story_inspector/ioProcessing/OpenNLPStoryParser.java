package com.story_inspector.ioProcessing;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

import javax.annotation.PostConstruct;

import org.springframework.stereotype.Component;

import com.story_inspector.ioProcessing.ExtractedDocument.ExtractedParagraph;
import com.story_inspector.ioProcessing.ExtractedDocument.ExtractedParagraph.FormattingType;
import com.story_inspector.ioProcessing.ExtractedDocument.ExtractedParagraph.ParagraphType;
import com.story_inspector.progress.ProgressMonitor;
import com.story_inspector.progress.TaskCanceledException;
import com.story_inspector.progress.TextProcessingProgressMonitor;
import com.story_inspector.story.ChapterImpl;
import com.story_inspector.story.ParagraphImpl;
import com.story_inspector.story.SceneImpl;
import com.story_inspector.story.SentenceImpl;
import com.story_inspector.story.Story;
import com.story_inspector.story.StoryImpl;
import com.story_inspector.story.TextRange;
import com.story_inspector.story.TokenImpl;

import opennlp.tools.postag.POSModel;
import opennlp.tools.postag.POSTagger;
import opennlp.tools.postag.POSTaggerME;
import opennlp.tools.sentdetect.SentenceDetectorME;
import opennlp.tools.sentdetect.SentenceModel;
import opennlp.tools.stemmer.PorterStemmer;
import opennlp.tools.stemmer.Stemmer;
import opennlp.tools.tokenize.Tokenizer;
import opennlp.tools.tokenize.TokenizerME;
import opennlp.tools.tokenize.TokenizerModel;
import opennlp.tools.util.InvalidFormatException;
import opennlp.tools.util.Span;

/**
 * Implementation of {@link StoryParser} that uses the Open NLP library.
 *
 * @author mizitch
 *
 */
@Component
public class OpenNLPStoryParser implements StoryParser {

	private SentenceDetectorME sentenceDetector;
	private Tokenizer tokenizer;
	private POSTagger posTagger;
	private final Stemmer stemmer = new PorterStemmer();

	/**
	 * Post-spring initialization. Creates Open NLP modules using model files.
	 *
	 * @throws Exception
	 */
	@PostConstruct
	public void initialize() throws Exception {
		initializeSentenceDetector();
		initializeTokenizer();
		initializePosTagger();
	}

	private void initializeTokenizer() throws InvalidFormatException, IOException {
		final InputStream modelIn = getClass().getResourceAsStream("/en-token.bin");
		final TokenizerModel model = new TokenizerModel(modelIn);
		this.tokenizer = new TokenizerME(model);
	}

	private void initializeSentenceDetector() throws InvalidFormatException, IOException {
		final InputStream modelIn = getClass().getResourceAsStream("/en-sent.bin");
		final SentenceModel model = new SentenceModel(modelIn);
		this.sentenceDetector = new SentenceDetectorME(model);
	}

	private void initializePosTagger() throws InvalidFormatException, IOException {
		final InputStream modelIn = getClass().getResourceAsStream("/en-pos-maxent.bin");
		final POSModel model = new POSModel(modelIn);
		this.posTagger = new POSTaggerME(model);
	}

	@Override
	public Story parseStory(final ExtractedDocument document, final ProgressMonitor progressMonitor) throws StoryIOException, TaskCanceledException {
		progressMonitor.reportProgress(0.0f, "Pre-processing paragraphs");
		// first filter out contact data, by line and blanks
		final List<ExtractedParagraph> filteredList = document.getParagraphs().stream()
				.filter(p -> !EnumSet.of(ParagraphType.BLANK, ParagraphType.BY_LINE, ParagraphType.CONTACT_INFO).contains(p.getType()))
				.collect(Collectors.toList());

		final StringBuilder title = new StringBuilder();
		int i = 0;
		while (i < filteredList.size() && filteredList.get(i).getType() == ParagraphType.TITLE) {
			title.append(filteredList.get(i).getText()).append(" ");
			++i;
		}

		progressMonitor.reportProgress(0.01f, "Extracting story text");
		final String storyText = generateStoryTextFromParagraphs(filteredList);

		final List<ChapterImpl> chapters = generateChaptersFromParagraphs(filteredList.subList(i, filteredList.size()),
				new TextProcessingProgressMonitor(progressMonitor.subMonitor(0.02f, 1.0f, "Generating text metadata"), storyText.length()));

		progressMonitor.reportProgress(1.0f, "Complete");
		return new StoryImpl(storyText, title.toString().trim(), chapters);
	}

	private String generateStoryTextFromParagraphs(final List<ExtractedParagraph> paragraphDataList) {
		final StringBuilder text = new StringBuilder();
		paragraphDataList.stream().filter(p -> p.getType() == ParagraphType.TEXT).forEach(p -> text.append(p.getText()));
		return text.toString();
	}

	private List<ChapterImpl> generateChaptersFromParagraphs(final List<ExtractedParagraph> paragraphs,
			final TextProcessingProgressMonitor progressMonitor) throws TaskCanceledException {
		return generateChaptersFromParagraphs(paragraphs, 0, progressMonitor);
	}

	/**
	 * Recursive method that returns a {@link LinkedList} of {@link ChapterImpl} from the provided paragraph list.
	 *
	 * Works by generating the first chapter within the paragraphs, then recursively calling itself with the remaining sublist.
	 *
	 * @param paragraphs
	 *            The list of paragraphs beginning at the provided starting character index within the text
	 * @param startingCharIndex
	 *            The story character index of the beginning of the first paragraph within the paragraph list. Note that only paragraphs of
	 *            {@link ParagraphType.TEXT} are counted when considering the overall story character index.
	 * @param progressMonitor
	 *            The {@link ProgressMonitor} to update
	 * @return The list of chapters represented by the provided paragraph list.
	 * @throws TaskCanceledException
	 *             If the user cancels the task during execution
	 */
	private LinkedList<ChapterImpl> generateChaptersFromParagraphs(final List<ExtractedParagraph> paragraphs, final int startingCharIndex,
			final TextProcessingProgressMonitor progressMonitor) throws TaskCanceledException {
		// Base case, we are out of paragraphs
		if (paragraphs.isEmpty()) {
			return new LinkedList<ChapterImpl>();
		}

		int currentParagraphIndex = 0;
		int currentCharIndex = startingCharIndex;

		// Generate the chapter title from any paragraphs with that type
		final StringBuilder titleBuilder = new StringBuilder();
		while (currentParagraphIndex < paragraphs.size() && paragraphs.get(currentParagraphIndex).getType() == ParagraphType.CHAPTER_TITLE) {
			titleBuilder.append(paragraphs.get(currentParagraphIndex).getText()).append("\n");
			++currentParagraphIndex;
		}

		// Note where the text of the chapter starts (after the title is finished)
		final int chapterStartParagraphIndex = currentParagraphIndex;

		// Find where the text of the chapter ends (everything between now and the next chapter title or the end of the text
		while (currentParagraphIndex < paragraphs.size() && paragraphs.get(currentParagraphIndex).getType() != ParagraphType.CHAPTER_TITLE) {
			// Titles, chapter titles, scene breaks, etc are not part of the story text, so the story character index is only incremented for "text"
			// paragraphs
			if (paragraphs.get(currentParagraphIndex).getType() == ParagraphType.TEXT) {
				currentCharIndex += paragraphs.get(currentParagraphIndex).getText().length();
			}
			++currentParagraphIndex;
		}

		// Get all scenes contained within the first chapter
		final List<SceneImpl> scenes = generateScenesFromParagraphs(paragraphs.subList(chapterStartParagraphIndex, currentParagraphIndex),
				startingCharIndex, progressMonitor);

		final TextRange chapterTextRange = new TextRange(startingCharIndex, currentCharIndex);

		// Create the first chapter within the provided paragraph list
		final ChapterImpl firstChapter = new ChapterImpl(chapterTextRange, scenes, titleBuilder.toString().trim());

		final LinkedList<ChapterImpl> chapters = generateChaptersFromParagraphs(paragraphs.subList(currentParagraphIndex, paragraphs.size()),
				currentCharIndex, progressMonitor);
		chapters.addFirst(firstChapter);
		return chapters;
	}

	/**
	 * Recursive method that returns a {@link LinkedList} of {@link SceneImpl} from the provided paragraph list.
	 *
	 * Works by generating the first scene within the paragraphs, then recursively calling itself with the remaining sublist.
	 *
	 * @param paragraphs
	 *            The list of paragraphs beginning at the provided starting character index within the text
	 * @param startingCharIndex
	 *            The story character index of the beginning of the first paragraph within the paragraph list. Note that only paragraphs of
	 *            {@link ParagraphType.TEXT} are counted when considering the overall story character index.
	 * @param progressMonitor
	 *            The {@link ProgressMonitor} to update
	 * @return The list of scenes represented by the provided paragraph list.
	 * @throws TaskCanceledException
	 *             If the user cancels the task during execution
	 */
	private LinkedList<SceneImpl> generateScenesFromParagraphs(final List<ExtractedParagraph> paragraphs, final int startingCharIndex,
			final TextProcessingProgressMonitor progressMonitor) throws TaskCanceledException {
		// Base case, out of paragraphs
		if (paragraphs.isEmpty()) {
			return new LinkedList<SceneImpl>();
		}

		int currentParagraphIndex = 0;
		int currentCharIndex = startingCharIndex;

		while (currentParagraphIndex < paragraphs.size() && paragraphs.get(currentParagraphIndex).getType() == ParagraphType.SCENE_BREAK) {
			// Skip over scene breaks at beginning of scene
			++currentParagraphIndex;
		}
		final int sceneStartParagraphIndex = currentParagraphIndex;
		while (currentParagraphIndex < paragraphs.size() && paragraphs.get(currentParagraphIndex).getType() != ParagraphType.SCENE_BREAK) {
			// Find next scene break or end of chapter
			if (paragraphs.get(currentParagraphIndex).getType() == ParagraphType.TEXT) {
				// Titles, chapter titles, scene breaks, etc are not part of the story text, so the story character index is only incremented for
				// "text" paragraphs
				currentCharIndex += paragraphs.get(currentParagraphIndex).getText().length();
			}
			++currentParagraphIndex;
		}
		// Generate story paragraphs for extracted paragraphs within the first scene
		final List<ParagraphImpl> generatedParagraphs = generateParagraphsFromParagraphSources(
				paragraphs.subList(sceneStartParagraphIndex, currentParagraphIndex), startingCharIndex, progressMonitor);

		final TextRange sceneTextRange = new TextRange(startingCharIndex, currentCharIndex);

		// Create the first scene within the provided paragraph list
		final SceneImpl firstScene = new SceneImpl(sceneTextRange, generatedParagraphs);

		final LinkedList<SceneImpl> scenes = generateScenesFromParagraphs(paragraphs.subList(currentParagraphIndex, paragraphs.size()),
				currentCharIndex, progressMonitor);
		scenes.addFirst(firstScene);
		return scenes;
	}

	/**
	 * Generate {@link ParagraphImpl}s for the provided list of {@link ExtractedParagraph}s.
	 *
	 * @param sourceParagraphs
	 *            The {@link ExtractedParagraph}s to generate {@link ParagraphImpl}s from.
	 * @param startingCharIndex
	 *            The story character index of the beginning of the first paragraph within the paragraph list. Note that only paragraphs of
	 *            {@link ParagraphType.TEXT} are counted when considering the overall story character index.
	 * @param progressMonitor
	 *            The {@link ProgressMonitor} to update
	 * @return The list of {@link ParagraphImpl}s represented by the provided {@link ExtractedParagraph} list.
	 * @throws TaskCanceledException
	 *             If the user cancels the task during execution
	 */
	private List<ParagraphImpl> generateParagraphsFromParagraphSources(final List<ExtractedParagraph> sourceParagraphs, final int startingCharIndex,
			final TextProcessingProgressMonitor progressMonitor) throws TaskCanceledException {
		int currentCharIndex = startingCharIndex;
		final List<ParagraphImpl> resultParagraphs = new ArrayList<ParagraphImpl>();

		for (final ExtractedParagraph sourceParagraph : sourceParagraphs) {
			resultParagraphs.add(generateParagraphFromSource(sourceParagraph, currentCharIndex, progressMonitor));
			currentCharIndex += sourceParagraph.getText().length();
		}

		return resultParagraphs;
	}

	/**
	 * Generate a {@link ParagraphImpl} for the provided {@link ExtractedParagraph}.
	 *
	 * @param paragraph
	 *            The {@link ExtractedParagraph}s to generate a {@link ParagraphImpl} from.
	 * @param startingCharIndex
	 *            The story character index of the beginning of the paragraph. Note that only paragraphs of {@link ParagraphType.TEXT} are counted
	 *            when considering the overall story character index.
	 * @param progressMonitor
	 *            The {@link ProgressMonitor} to update
	 * @return The {@link ParagraphImpl} represented by the provided {@link ExtractedParagraph}.
	 * @throws TaskCanceledException
	 *             If the user cancels the task during execution
	 */
	private ParagraphImpl generateParagraphFromSource(final ExtractedParagraph paragraph, final int startingCharIndex,
			final TextProcessingProgressMonitor progressMonitor) throws TaskCanceledException {

		progressMonitor.reportProgress(startingCharIndex);

		final Span[] sentenceSpans = this.sentenceDetector.sentPosDetect(paragraph.getText());
		// Generate the ranges the sentences cover. This will make the sentences cover any white space within the paragraph as well
		final List<TextRange> sentenceRanges = generateTextRangesFromSpans(sentenceSpans,
				new TextRange(startingCharIndex, startingCharIndex + paragraph.getText().length()));
		final List<SentenceImpl> sentences = sentenceRanges.stream()
				.map(r -> new SentenceImpl(r, generateTokensFromSentence(r, paragraph, startingCharIndex))).collect(Collectors.toList());
		return new ParagraphImpl(new TextRange(startingCharIndex, startingCharIndex + paragraph.getText().length()), sentences);
	}

	/**
	 * Generate a list of tokens for the specified sentence.
	 *
	 * @param sentenceRange
	 *            The {@link TextRange} of the sentence to tokenize
	 * @param paragraph
	 *            The {@link ExtractedParagraph} the sentence is within
	 * @param paragraphStartCharIndex
	 *            The story character index of the beginning of the paragraph. Note that only paragraphs of {@link ParagraphType.TEXT} are counted
	 *            when considering the overall story character index.
	 * @return
	 */
	private List<TokenImpl> generateTokensFromSentence(final TextRange sentenceRange, final ExtractedParagraph paragraph,
			final int paragraphStartCharIndex) {
		// Get sentence string and tokenize it
		final String sentence = paragraph.getText().substring(sentenceRange.getStartIndex() - paragraphStartCharIndex,
				sentenceRange.getEndIndex() - paragraphStartCharIndex);
		final Span[] tokenSpans = this.tokenizer.tokenizePos(sentence.toString());
		final String[] tokenTexts = Span.spansToStrings(tokenSpans, sentence);

		// Do part of speech tagging on token strings
		final String[] tokenPosTags = this.posTagger.tag(tokenTexts);

		// Create token objects
		// Generate the ranges the tokens cover. This will make the tokens cover any white space within the sentence as well
		final List<TextRange> ranges = generateTextRangesFromSpans(tokenSpans, sentenceRange);
		final List<TokenImpl> tokens = new ArrayList<>(ranges.size());
		for (int i = 0; i < ranges.size(); ++i) {
			tokens.add(constructToken(ranges.get(i), tokenTexts[i], tokenPosTags[i], paragraph, paragraphStartCharIndex));
		}
		return tokens;
	}

	/**
	 * Generate a {@link TokenImpl} from a given text, {@link TextRange} and {@link ExtractedParagraph}.
	 *
	 * @param tokenRange
	 *            The {@link TextRange} within the story this token covers.
	 * @param tokenText
	 *            The text of the token
	 * @param tokenPosTag
	 *            The part of speech tag of the token
	 * @param paragraph
	 *            The {@link ExtractedParagraph} that the token is within.
	 * @param paragraphStartCharIndex
	 *            The story character index of the beginning of the paragraph. Note that only paragraphs of {@link ParagraphType.TEXT} are counted
	 *            when considering the overall story character index.
	 * @return The generated {@link TokenImpl}
	 */
	private TokenImpl constructToken(final TextRange tokenRange, final String tokenText, final String tokenPosTag, final ExtractedParagraph paragraph,
			final int paragraphStartCharIndex) {
		// Localize the token text range to the extracted paragraph so we can extract formatting data and whether it is quoted
		final TextRange paragraphLocalTokenRange = tokenRange.translate(-1 * paragraphStartCharIndex);
		final boolean isBold = TextRange.intersectsRangeSet(paragraphLocalTokenRange, paragraph.getFormattedText(FormattingType.BOLD));
		final boolean isItalics = TextRange.intersectsRangeSet(paragraphLocalTokenRange, paragraph.getFormattedText(FormattingType.ITALICS));
		final boolean isUnderline = TextRange.intersectsRangeSet(paragraphLocalTokenRange, paragraph.getFormattedText(FormattingType.UNDERLINE));
		final boolean isQuoted = TextRange.intersectsRangeSet(paragraphLocalTokenRange, paragraph.getQuotedText());

		// Get the token stem
		final String tokenStem = this.stemmer.stem(tokenText).toString();

		// Return the new token
		return new TokenImpl(tokenRange, tokenText, tokenPosTag, tokenStem, isBold, isItalics, isUnderline, isQuoted);
	}

	/**
	 * Given an array of {@link Span}s localized to a text range, expand the spans as necessary to fully cover the range and convert to a list of
	 * {@link TextRange}s.
	 *
	 * Do this by expanding the beginning of each span if necessary and finally, the end of the last span if necessary.
	 *
	 * @param spans
	 *            The spans to expand
	 * @param range
	 *            The range to cover
	 * @return The list of {@link TextRange}s that cover the range.
	 */
	private List<TextRange> generateTextRangesFromSpans(final Span[] spans, final TextRange range) {
		int currentCharIndex = range.getStartIndex();
		final int endingCharIndex = range.getEndIndex();

		final List<TextRange> ranges = new ArrayList<>(spans.length);
		for (final Span tokenSpan : spans) {
			// Get the global position of the end of the current span
			final int rangeEnd = range.getStartIndex() + tokenSpan.getEnd();
			// Create a new range from the point we have covered so far to the end of the current span, might expand the beginning of the current span
			ranges.add(new TextRange(currentCharIndex, rangeEnd));
			currentCharIndex = rangeEnd;
		}

		// Extend end of last range if necessary to reach the end of the range we must cover
		final TextRange lastRange = ranges.get(ranges.size() - 1);
		if (lastRange.getEndIndex() < endingCharIndex) {
			ranges.set(ranges.size() - 1, new TextRange(lastRange.getStartIndex(), endingCharIndex));
		}

		return ranges;
	}
}
