package com.story_inspector.analysis.analyzers;

import static com.story_inspector.analysis.ParameterValidator.concatenateValidators;
import static com.story_inspector.analysis.ParameterValidator.createValidator;
import static com.story_inspector.analysis.ParameterValidator.notNull;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import com.story_inspector.analysis.Analyzer;
import com.story_inspector.analysis.AnalyzerResult;
import com.story_inspector.analysis.AnalyzerSpec;
import com.story_inspector.analysis.AnalyzerType;
import com.story_inspector.analysis.BaseAnalyzer;
import com.story_inspector.analysis.BaseAnalyzerType;
import com.story_inspector.analysis.Comment;
import com.story_inspector.analysis.ParameterSpec;
import com.story_inspector.analysis.parameterTypes.DialogueSearchPattern;
import com.story_inspector.analysis.parameterTypes.StringSet;
import com.story_inspector.analysis.summary.SummaryGenerators;
import com.story_inspector.story.Story;
import com.story_inspector.story.TextRange;
import com.story_inspector.story.Token;

import opennlp.tools.stemmer.PorterStemmer;
import opennlp.tools.stemmer.Stemmer;

/**
 * {@link AnalyzerType} which searches for words.
 *
 * @author mizitch
 *
 */
@Component
public class WordSearchAnalyzerType extends BaseAnalyzerType<WordSearchAnalyzerType> {

	private static final String name = "Word Search Analyzer";
	private static final String description = "Searches for words! Not case-sensitive"; // TODO: better
	private static final String id = "WordSearchAnalyzer";
	private static final int version = 1;
	private static final boolean producesComments = true;

	// @formatter:off
	private static final ParameterSpec<StringSet> searchWordsSpec = new ParameterSpec<>(
			"searchWords",
			"Search words",
			"Words to search for",
			StringSet.class,
			concatenateValidators(
					createValidator(
							wl -> !wl.isEmpty(),
							"Search list cannot be empty"),
					createValidator(
							wl -> !wl.stream().anyMatch(w -> StringUtils.isBlank(w)),
							"Blanks not allowed in search list"),
					createValidator(
							wl -> wl.stream().allMatch(w -> !w.trim().chars().anyMatch(c -> Character.isWhitespace(c))),
							"Phrases not allowed in search list")));

	private static final ParameterSpec<Boolean> searchByStemSpec =
			new ParameterSpec<>(
					"searchByStem",
					"Search by stem",
					"Search by word stem rather than exact match. For example, a search for 'whisper' would match 'whisper', 'whispers', 'whispered', 'whispering', etc.",
					Boolean.class,
					notNull("Search by stem"),
					true);

	private static final ParameterSpec<DialogueSearchPattern> dialogueSearchPatternSpec =
			new ParameterSpec<>(
					"dialogueSearchPattern",
					"Dialogue search pattern",
					"Whether to search in dialogue, non-dialogue or both",
					DialogueSearchPattern.class,
					notNull("Dialogue search pattern"),
					DialogueSearchPattern.ALL_TEXT);

	// @formatter:on
	private static final List<ParameterSpec<?>> parameterSpecs = Arrays.asList(searchWordsSpec, searchByStemSpec, dialogueSearchPatternSpec);

	private final Stemmer stemmer = new PorterStemmer();

	public WordSearchAnalyzerType() {
		super(name, description, id, version, producesComments, parameterSpecs);
	}

	private class WordSearchAnalyzer extends BaseAnalyzer<WordSearchAnalyzerType> {
		private final StringSet searchWords;
		private final boolean searchByStem;
		private final DialogueSearchPattern dialogueSearchPattern;

		private WordSearchAnalyzer(final AnalyzerSpec<WordSearchAnalyzerType> spec) {
			super(spec);
			this.searchWords = spec.getParameterValue(searchWordsSpec);
			this.searchByStem = spec.getParameterValue(searchByStemSpec);
			this.dialogueSearchPattern = spec.getParameterValue(dialogueSearchPatternSpec);
		}

		@Override
		public AnalyzerResult<WordSearchAnalyzerType> execute(final Story story) {
			final List<Token> tokens = story.getChildrenAtLevel(Token.class);
			final Map<TextRange, String> matches = new HashMap<>();
			final Map<String, String> searchTermMap = new HashMap<>();
			for (final String word : this.searchWords) {
				if (this.searchByStem)
					searchTermMap.put(WordSearchAnalyzerType.this.stemmer.stem(word).toString().toLowerCase(), word);
				else
					searchTermMap.put(word, word.toLowerCase());
			}

			for (final Token token : tokens) {
				if (token.isQuoted() && WordSearchAnalyzer.this.dialogueSearchPattern == DialogueSearchPattern.ALL_BUT_DIALOGUE)
					continue;
				if (!token.isQuoted() && WordSearchAnalyzer.this.dialogueSearchPattern == DialogueSearchPattern.DIALOGUE_ONLY)
					continue;

				if (WordSearchAnalyzer.this.searchByStem && searchTermMap.keySet().contains(token.getWordStem().toLowerCase()))
					matches.put(token.getRange(), searchTermMap.get(token.getWordStem()));
				else if (!this.searchByStem && searchTermMap.keySet().contains(token.getWord().toLowerCase()))
					matches.put(token.getRange(), searchTermMap.get(token.getWord()));
			}

			final List<Comment> comments = matches.entrySet().stream().map(e -> new Comment(this, "Matches " + e.getValue(), e.getKey()))
					.collect(Collectors.toList());
			return new AnalyzerResult<WordSearchAnalyzerType>(this, comments,
					SummaryGenerators.generateBasicFrequencySummary(story, matches.keySet()));
		}

		@Override
		public Map<String, Object> retrieveParameterValues() {
			final Map<String, Object> result = new HashMap<>();
			result.put(searchWordsSpec.getId(), this.searchWords);
			result.put(searchByStemSpec.getId(), this.searchByStem);
			result.put(dialogueSearchPatternSpec.getId(), this.dialogueSearchPattern);
			return result;
		}
	}

	@Override
	protected Analyzer<WordSearchAnalyzerType> createAnalyzer(final AnalyzerSpec<WordSearchAnalyzerType> spec) {
		return new WordSearchAnalyzer(spec);
	}
}
