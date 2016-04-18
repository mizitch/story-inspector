package com.story_inspector.ioProcessing;

import com.story_inspector.progress.ProgressMonitor;
import com.story_inspector.progress.TaskCanceledException;
import com.story_inspector.story.Story;

/**
 * Converts an {@link ExtractedDocument} into a {@link Story}.
 *
 * @author mizitch
 *
 */
public interface StoryParser {

	/**
	 * Converts provided {@link ExtractedDocument} into a {@link Story}. Generates structure of Story and children, also uses NLP to decorate story
	 * with appropriate data.
	 *
	 * @param document
	 *            Document to convert
	 * @param progressMonitor
	 *            {@link ProgressMonitor} that will be updated as the story is parsed.
	 * @return Converted {@link Story}
	 * @throws StoryIOException
	 *             If there is an IO error while parsing the story.
	 * @throws TaskCanceledException
	 *             If the user cancels the task while the story is being parsed.
	 */
	public Story parseStory(ExtractedDocument document, ProgressMonitor progressMonitor) throws StoryIOException, TaskCanceledException;
}
