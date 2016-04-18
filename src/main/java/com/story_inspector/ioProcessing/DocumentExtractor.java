package com.story_inspector.ioProcessing;

import java.io.InputStream;
import java.util.Set;

import com.story_inspector.progress.ProgressMonitor;
import com.story_inspector.progress.TaskCanceledException;

/**
 * Reads an input stream to create an {@link ExtractedDocument}.
 *
 * @author mizitch
 * @see {@link ExtractedDocument}
 */
public interface DocumentExtractor {
	/**
	 * Reads the input stream and creates an {@link ExtractedDocument}.
	 *
	 * @param inputStream
	 *            The input stream to read from.
	 * @param progressMonitor
	 *            The {@link ProgressMonitor} to update.
	 * @return The extracted document
	 * @throws StoryIOException
	 *             If there is an IO error when extracting the document.
	 * @throws TaskCanceledException
	 *             If the {@link ProgressMonitor} indicates the task is canceled by the user.
	 */
	public ExtractedDocument extractDocument(InputStream inputStream, ProgressMonitor progressMonitor) throws StoryIOException, TaskCanceledException;

	/**
	 * Returns the set of file types this extractor supports. Represented as the file extension without a leading "."
	 *
	 * @return The set of file types this extractor supports.
	 */
	public Set<String> getSupportedFileTypes();

}
