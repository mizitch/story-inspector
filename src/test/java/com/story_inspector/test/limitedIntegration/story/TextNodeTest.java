package com.story_inspector.test.limitedIntegration.story;

import java.io.StringWriter;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.junit.Before;
import org.junit.Test;

import com.story_inspector.story.Chapter;
import com.story_inspector.story.ChapterImpl;
import com.story_inspector.story.Paragraph;
import com.story_inspector.story.ParagraphImpl;
import com.story_inspector.story.Scene;
import com.story_inspector.story.SceneImpl;
import com.story_inspector.story.Sentence;
import com.story_inspector.story.SentenceImpl;
import com.story_inspector.story.StoryImpl;
import com.story_inspector.story.TextNode;
import com.story_inspector.story.TextRange;
import com.story_inspector.story.Token;
import com.story_inspector.story.TokenImpl;

import junit.framework.Assert;

/**
 * Unit testing for all types of {@link TextNode}
 *
 * Easier to lump this into one test than to do a ton of mocking and test each class in isolation, so these classes are tested here rather than via
 * unit test.
 *
 * @author mizitch
 *
 */
public class TextNodeTest {

	private String oneWordStoryText;

	private TextRange oneWordStoryTextRange;

	private String oneWordStoryTitle;

	private String oneWordStoryChapterTitle;

	private StoryImpl oneWordStory;

	private TokenImpl oneWordStoryToken;

	private SentenceImpl oneWordStorySentence;

	private ParagraphImpl oneWordStoryParagraph;

	private SceneImpl oneWordStoryScene;

	private ChapterImpl oneWordStoryChapter;

	private List<TokenImpl> longerStoryTokens;

	private List<SentenceImpl> longerStorySentences;

	private List<ParagraphImpl> longerStoryParagraphs;

	private List<SceneImpl> longerStoryScenes;

	private List<ChapterImpl> longerStoryChapters;

	private StoryImpl longerStory;

	private final String childRangeTestText = "Two words";

	@Before
	public void initializeStories() {
		initializeOneWordStory();
		initializeLongerStory();
	}

	private void initializeOneWordStory() {
		this.oneWordStoryText = "Word";
		this.oneWordStoryTextRange = new TextRange(0, 4);
		this.oneWordStoryTitle = "Best Story Ever";
		this.oneWordStoryChapterTitle = "Chapter 1";

		this.oneWordStoryToken = new TokenImpl(this.oneWordStoryTextRange, "Word", "N", "Word", false, false, false, false);
		this.oneWordStorySentence = new SentenceImpl(this.oneWordStoryTextRange, Arrays.asList(this.oneWordStoryToken));
		this.oneWordStoryParagraph = new ParagraphImpl(this.oneWordStoryTextRange, Arrays.asList(this.oneWordStorySentence));
		this.oneWordStoryScene = new SceneImpl(this.oneWordStoryTextRange, Arrays.asList(this.oneWordStoryParagraph));
		this.oneWordStoryChapter = new ChapterImpl(this.oneWordStoryTextRange, Arrays.asList(this.oneWordStoryScene), this.oneWordStoryChapterTitle);
		this.oneWordStory = new StoryImpl(this.oneWordStoryText, this.oneWordStoryTitle, Arrays.asList(this.oneWordStoryChapter));
	}

	private void initializeLongerStory() {
		final String storyText = "This is chapter 1. This is sentence 2. This is paragraph 2. This is scene 2. This is chapter 2.";

		// Okay, now let's generate the story structure out of this. Would be awesome if I just used the code I wrote to do this automagically, but
		// that would make this not a unit test...so
		final TextRange chapter1Range = new TextRange(0, 77);
		final TextRange scene1Range = new TextRange(0, 60);
		final TextRange paragraph1Range = new TextRange(0, 39);
		final TextRange sentence1Range = new TextRange(0, 19);
		final TextRange chapter2Range = new TextRange(77, 95);
		final TextRange scene2Range = new TextRange(60, 77);
		final TextRange paragraph2Range = new TextRange(39, 60);
		final TextRange sentence2Range = new TextRange(19, 39);
		final TextRange[] tokenRanges = new TextRange[] { new TextRange(0, 5), new TextRange(5, 8), new TextRange(8, 16), new TextRange(16, 17),
				new TextRange(17, 19), new TextRange(19, 24), new TextRange(24, 27), new TextRange(27, 36), new TextRange(36, 37),
				new TextRange(37, 39), new TextRange(39, 44), new TextRange(44, 47), new TextRange(47, 57), new TextRange(57, 58),
				new TextRange(58, 60), new TextRange(60, 65), new TextRange(65, 68), new TextRange(68, 74), new TextRange(74, 75),
				new TextRange(75, 77), new TextRange(77, 82), new TextRange(82, 85), new TextRange(85, 93), new TextRange(93, 94),
				new TextRange(94, 95) };

		this.longerStoryTokens = Arrays.stream(tokenRanges)
				.map(range -> new TokenImpl(range, range.getCoveredText(storyText), "?", range.getCoveredText(storyText), false, false, false, false))
				.collect(Collectors.toList());

		this.longerStorySentences = Arrays.asList(new SentenceImpl(sentence1Range, this.longerStoryTokens.subList(0, 5)),
				new SentenceImpl(sentence2Range, this.longerStoryTokens.subList(5, 10)),
				new SentenceImpl(paragraph2Range, this.longerStoryTokens.subList(10, 15)),
				new SentenceImpl(scene2Range, this.longerStoryTokens.subList(15, 20)),
				new SentenceImpl(chapter2Range, this.longerStoryTokens.subList(20, 25)));

		this.longerStoryParagraphs = Arrays.asList(new ParagraphImpl(paragraph1Range, this.longerStorySentences.subList(0, 2)),
				new ParagraphImpl(paragraph2Range, this.longerStorySentences.subList(2, 3)),
				new ParagraphImpl(scene2Range, this.longerStorySentences.subList(3, 4)),
				new ParagraphImpl(chapter2Range, this.longerStorySentences.subList(4, 5)));

		this.longerStoryScenes = Arrays.asList(new SceneImpl(scene1Range, this.longerStoryParagraphs.subList(0, 2)),
				new SceneImpl(scene2Range, this.longerStoryParagraphs.subList(2, 3)),
				new SceneImpl(chapter2Range, this.longerStoryParagraphs.subList(3, 4)));

		this.longerStoryChapters = Arrays.asList(new ChapterImpl(chapter1Range, this.longerStoryScenes.subList(0, 2), "Chapter 1"),
				new ChapterImpl(chapter2Range, this.longerStoryScenes.subList(2, 3), "Chapter 2"));

		this.longerStory = new StoryImpl(storyText, "A Long Story", this.longerStoryChapters);

	}

	@Test
	public void testOneWordStory() throws Exception {
		Assert.assertEquals(this.oneWordStoryText, this.oneWordStory.getText());
		Assert.assertEquals("o", this.oneWordStory.getSelection(new TextRange(1, 2)));
		Assert.assertEquals(this.oneWordStoryTitle, this.oneWordStory.getTitle());
		Assert.assertEquals(Arrays.asList(this.oneWordStoryChapter), this.oneWordStory.getChildrenAtLevel(Chapter.class));
		Assert.assertEquals(Arrays.asList(this.oneWordStoryParagraph), this.oneWordStory.getChildrenAtLevel(Paragraph.class));
		Assert.assertEquals(Arrays.asList(this.oneWordStoryToken), this.oneWordStory.getChildrenAtLevel(Token.class));
		Assert.assertEquals(Arrays.asList(this.oneWordStoryScene),
				this.oneWordStory.getChildrenAtLevelIntersectingRange(Scene.class, new TextRange(0, 1)));
		Assert.assertNull(this.oneWordStory.getParent());
		Assert.assertEquals(ChapterImpl.class, this.oneWordStory.getChildType());
		Assert.assertEquals(this.oneWordStoryTextRange, this.oneWordStory.getRange());
	}

	@Test
	public void testOneWordStoryChapter() throws Exception {
		Assert.assertEquals(this.oneWordStoryChapterTitle, this.oneWordStoryChapter.getTitle());
		Assert.assertEquals("o", this.oneWordStoryChapter.getSelection(new TextRange(1, 2)));
		Assert.assertEquals(this.oneWordStoryText, this.oneWordStoryChapter.getText());
		final StringWriter writer = new StringWriter();
		this.oneWordStoryChapter.write(writer, new TextRange(1, 3));
		Assert.assertEquals("or", writer.toString());
		Assert.assertTrue(this.oneWordStoryChapter.toString().toLowerCase().contains("chapter"));
	}

	@Test(expected = UnsupportedOperationException.class)
	public void testSentenceFragment() {
		// Reminder to actually test this when it is implemented
		this.oneWordStorySentence.isFragment();
	}

	@Test(expected = UnsupportedOperationException.class)
	public void testSentenceQuestion() {
		// Reminder to actually test this when it is implemented
		this.oneWordStorySentence.isQuestion();
	}

	@Test
	public void testOneWordStoryToken() throws Exception {
		Assert.assertEquals(this.oneWordStoryText, this.oneWordStoryToken.getWord());
		Assert.assertEquals("N", this.oneWordStoryToken.getPartOfSpeechTag());
		Assert.assertFalse(this.oneWordStoryToken.isAllCaps());
		Assert.assertFalse(this.oneWordStoryToken.isBold());
		Assert.assertFalse(this.oneWordStoryToken.isItalicized());
		Assert.assertFalse(this.oneWordStoryToken.isUnderlined());
		Assert.assertFalse(this.oneWordStoryToken.isQuoted());
		Assert.assertTrue(this.oneWordStoryToken.isWord());
		Assert.assertEquals("Word", this.oneWordStoryToken.getWordStem());
		Assert.assertTrue(this.oneWordStoryToken.toString().contains(this.oneWordStoryTextRange.toString()));
		Assert.assertEquals("or", this.oneWordStoryToken.getSelection(new TextRange(1, 3)));
		final StringWriter writer = new StringWriter();
		this.oneWordStoryToken.write(writer, new TextRange(1, 3));
		Assert.assertEquals("or", writer.toString());
	}

	@Test
	public void testLongerStory() throws Exception {

		// Check that getting all children at various levels works
		Assert.assertEquals(this.longerStoryChapters, this.longerStory.getChildrenAtLevel(Chapter.class));
		Assert.assertEquals(this.longerStoryScenes, this.longerStory.getChildrenAtLevel(Scene.class));
		Assert.assertEquals(this.longerStoryParagraphs, this.longerStory.getChildrenAtLevel(Paragraph.class));
		Assert.assertEquals(this.longerStorySentences, this.longerStory.getChildrenAtLevel(Sentence.class));
		Assert.assertEquals(this.longerStoryTokens, this.longerStory.getChildrenAtLevel(Token.class));

		// Check that getting children at a certain level intersecting a certain range works
		Assert.assertEquals(this.longerStorySentences.subList(1, 3),
				this.longerStory.getChildrenAtLevelIntersectingRange(Sentence.class, new TextRange(38, 50)));

		// Check that ". " is neither a word nor all caps
		Assert.assertFalse(this.longerStoryTokens.get(4).isWord());
		Assert.assertNull(this.longerStoryTokens.get(4).getWord());
		Assert.assertFalse(this.longerStoryTokens.get(4).isAllCaps());

		// Check that "This " is a word and is all caps and that the word portion is "This"
		Assert.assertTrue(this.longerStoryTokens.get(0).isWord());
		Assert.assertEquals(this.longerStoryTokens.get(0).getWord(), "This");
		Assert.assertFalse(this.longerStoryTokens.get(0).isAllCaps());

		// Check that some parents are set correctly
		Assert.assertEquals(this.longerStoryChapters.get(1).getParent(), this.longerStory);
		Assert.assertEquals(this.longerStoryParagraphs.get(2).getParent(), this.longerStoryScenes.get(1));
		Assert.assertEquals(this.longerStorySentences.get(1).getParent(), this.longerStoryParagraphs.get(0));
		Assert.assertEquals(this.longerStoryTokens.get(2).getParent(), this.longerStorySentences.get(0));

	}

	@Test
	public void testTokenWordExtraction() {
		// Test that leading and trailing space and punctuation are trimmed appropriately to determine the word for this token
		final String storyText = "   . \"Wordy,\"  ";
		final TokenImpl token = new TokenImpl(new TextRange(0, 17), storyText, "?", "Word", false, false, false, false);
		Assert.assertEquals("Wordy", token.getWord());
	}

	@Test(expected = IllegalArgumentException.class)
	public void testTokenGetChildren() {
		this.oneWordStoryToken.getChildrenAtLevel(Token.class);
	}

	@Test(expected = IllegalArgumentException.class)
	public void testSameLevelGetChildren() {
		this.oneWordStorySentence.getChildrenAtLevel(Sentence.class);
	}

	@Test(expected = IllegalArgumentException.class)
	public void testHigherLevelGetChildren() {
		this.oneWordStoryParagraph.getChildrenAtLevel(Scene.class);
	}

	// Test child range validation works as expected
	// Text ranges of child nodes should completely cover range of parent with no gaps or gluts and not extend past the range of the parent
	// They should also be in order

	@Test
	public void testCorrectChildRanges() {
		testChildRanges(Arrays.asList(new TextRange(0, 4), new TextRange(4, 9)), new TextRange(0, 9), this.childRangeTestText);
	}

	@Test(expected = IllegalArgumentException.class)
	public void testChildRangesOutOfOrder() {
		testChildRanges(Arrays.asList(new TextRange(4, 9), new TextRange(0, 4)), new TextRange(0, 9), this.childRangeTestText);
	}

	@Test(expected = IllegalArgumentException.class)
	public void testChildRangeGaps() {
		testChildRanges(Arrays.asList(new TextRange(0, 4), new TextRange(5, 9)), new TextRange(0, 9), this.childRangeTestText);
	}

	@Test(expected = IllegalArgumentException.class)
	public void testChildRangeGluts() {
		testChildRanges(Arrays.asList(new TextRange(0, 4), new TextRange(3, 9)), new TextRange(0, 9), this.childRangeTestText);
	}

	@Test(expected = IllegalArgumentException.class)
	public void testChildRangeStartGap() {
		testChildRanges(Arrays.asList(new TextRange(1, 4), new TextRange(4, 9)), new TextRange(0, 9), this.childRangeTestText);
	}

	@Test(expected = IllegalArgumentException.class)
	public void testChildRangeEndGap() {
		testChildRanges(Arrays.asList(new TextRange(0, 4), new TextRange(4, 8)), new TextRange(0, 9), this.childRangeTestText);
	}

	@Test(expected = IllegalArgumentException.class)
	public void testChildRangesBeforeStart() {
		testChildRanges(Arrays.asList(new TextRange(0, 4), new TextRange(4, 9)), new TextRange(1, 9), this.childRangeTestText);
	}

	@Test(expected = IllegalArgumentException.class)
	public void testChildRangesAfterEnd() {
		testChildRanges(Arrays.asList(new TextRange(0, 4), new TextRange(4, 9)), new TextRange(0, 8), this.childRangeTestText);
	}

	private void testChildRanges(final List<TextRange> childRanges, final TextRange parentRange, final String text) {
		final List<TokenImpl> tokens = childRanges.stream().map(range -> createToken(range, text)).collect(Collectors.toList());
		new SentenceImpl(parentRange, tokens);
	}

	private TokenImpl createToken(final TextRange range, final String text) {
		return new TokenImpl(range, range.getCoveredText(text), "?", range.getCoveredText(text), false, false, false, false);
	}
}
